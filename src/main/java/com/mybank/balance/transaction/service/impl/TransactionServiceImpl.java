package com.mybank.balance.transaction.service.impl;

import com.mybank.balance.transaction.common.Constants;
import com.mybank.balance.transaction.common.DistributedLockService;
import com.mybank.balance.transaction.common.TransactionStatus;
import com.mybank.balance.transaction.dao.AccountRepository;
import com.mybank.balance.transaction.dao.TransactionRepository;
import com.mybank.balance.transaction.dto.ProcessTransactionRequest;
import com.mybank.balance.transaction.dto.ProcessTransactionResponse;
import com.mybank.balance.transaction.dto.GetTransactionResponse;
import com.mybank.balance.transaction.exception.BizException;
import com.mybank.balance.transaction.exception.ErrorCode;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.model.Transaction;
import com.mybank.balance.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 
 * 
 * @time 2025/2/16 21:40
 * @author zhangdaochuan
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DistributedLockService lockService;
    @Autowired
    private TransactionalOperator transactionalOperator;

    private static final Duration LOCK_EXPIRE = Duration.ofSeconds(10);

    @Override
    public Mono<GetTransactionResponse> getTransaction(String bizId) {
        return transactionRepository.findByBizId(bizId)
                .map(txn -> GetTransactionResponse.from(txn));
    }

    @Override
    public Mono<ProcessTransactionResponse> processTransaction(ProcessTransactionRequest request) {
        // 使用 bizId 作为锁 key
        String lockKey = "lock:txn:" + request.getBizId();
        return lockService.acquireLock(lockKey, LOCK_EXPIRE)
                .switchIfEmpty(Mono.error(new BizException(ErrorCode.LOCK_ACQUIRE_FAILED)))
                .flatMap(lockValue ->
                        // 调用内部处理方法
                        processInternal(request)
                                .flatMap(response ->
                                        // 释放锁后返回结果
                                        lockService.releaseLock(lockKey, lockValue)
                                                .thenReturn(response)
                                )
                );
    }

    private Mono<ProcessTransactionResponse> processInternal(ProcessTransactionRequest request) {
        // 检查业务幂等：如果已存在则直接返回状态
        return transactionRepository.findByBizId(request.getBizId())
                .flatMap(existing -> Mono.error(new BizException(ErrorCode.DUPLICATE_TRANSACTION)))
                .switchIfEmpty(Mono.defer(() -> {
                    // 新增交易记录，初始状态为 PENDING
                    Transaction txn = request.to();
                    txn.setStatus(TransactionStatus.PENDING);
                    return transactionRepository.save(txn);
                }))
                .flatMap(txnObj -> {
                    //transactionRepository.save(txn)的返回值是Mono<Object>实际是Mono<Transaction>
                    Transaction txn = (Transaction)txnObj;
                    // 查询 source 账户
                    return accountRepository.findByAccountId(request.getSourceAccount())
                            .switchIfEmpty(Mono.error(new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "source account not found")))
                            .flatMap(sourceAccount -> {
                                // 校验币种
                                if (!sourceAccount.getCurrency().equals(request.getCurrency())) {
                                    return Mono.error(new BizException(ErrorCode.CURRENCY_MISMATCH));
                                }
                                // 检查余额是否充足
                                if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
                                    txn.setStatus(TransactionStatus.FAIL);
                                    txn.setError("Insufficient funds ，less then:"+request.getCurrency() + " "+request.getAmount());
                                    txn.setUpdatedAt(LocalDateTime.now());
                                    return transactionRepository.save(txn)
                                            .then(Mono.error(new BizException(ErrorCode.INSUFFICIENT_FUNDS, "balance is not enough")));
                                }
                                // 进入乐观锁更新阶段（重试机制，最多重试 3 次）
                                return transactionalOperator.transactional(
                                        attemptOptimisticUpdate(txn, sourceAccount, request.getAmount(), 0)
                                );
                            });
                });
    }

    /**
     * 尝试乐观锁更新 source 与 target 账户余额，重试最多 3 次
     */
    private Mono<ProcessTransactionResponse> attemptOptimisticUpdate(Transaction txn, Account sourceAccount,
                                                                     BigDecimal amount, int retryCount) {
        if (retryCount > 3) {
            txn.setStatus(TransactionStatus.FAIL);
            txn.setUpdatedAt(LocalDateTime.now());
            return transactionRepository.save(txn)
                    .then(Mono.just(ProcessTransactionResponse.from(txn)));
        }
        // 更新 source 账户：扣款（带乐观锁判断版本）
        Mono<Account> updateSource = accountRepository.findByAccountId(sourceAccount.getAccountId())
                .flatMap(acc -> {
                    if (acc.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new BizException(ErrorCode.INSUFFICIENT_FUNDS));
                    }
                    acc.setBalance(acc.getBalance().subtract(amount));
                    // 乐观锁更新，由 R2DBC 的 @Version 自动判断
                    return accountRepository.save(acc);
                });
        // 更新 target 账户：加款
        Mono<Account> updateTarget = accountRepository.findByAccountId(Long.parseLong(txn.getTargetAccount().toString()))
                .flatMap(acc -> {
                    if (!acc.getCurrency().equals(txn.getCurrency())) {
                        return Mono.error(new BizException(ErrorCode.CURRENCY_MISMATCH));
                    }
                    acc.setBalance(acc.getBalance().add(amount));
                    return accountRepository.save(acc);
                });
        // 执行更新并更新交易状态
        return updateSource.then(updateTarget)
                .flatMap(updated -> {
                    txn.setStatus(TransactionStatus.SUCCESS);
                    txn.setUpdatedAt(LocalDateTime.now());
                    return transactionRepository.save(txn)
                            .thenReturn(ProcessTransactionResponse.from(txn));
                })
                .onErrorResume(ex -> {
                    // 若出现乐观锁更新失败或其它并发问题，则重试
                    txn.setRetry(txn.getRetry() + 1);
                    return Mono.delay(Duration.ofMillis(50L * (retryCount + 1)))
                            .then(attemptOptimisticUpdate(txn, sourceAccount, amount, retryCount + 1));
                });
    }

    @Override
    public Mono<Void> reprocessTransaction(String bizId) {
        // 用于异步重试任务处理
        return transactionRepository.findByBizId(bizId)
                .flatMap(txn -> {
                    // 如果重试次数超过6，则置为 FAIL
                    if (txn.getRetry() != null && txn.getRetry() > Constants.MAX_TX_RETRY) {
                        txn.setStatus(TransactionStatus.FAIL);
                        txn.setError("retry times exceed 6,need manual process");
                        txn.setUpdatedAt(LocalDateTime.now());
                        //TODO send email/ sms  to admin
                        return transactionRepository.save(txn).then();
                    }
                    // 否则，重复调用 processTransaction 内部逻辑
                    ProcessTransactionRequest req = new ProcessTransactionRequest();
                    req.setBizId(txn.getBizId());
                    req.setSourceAccount(txn.getSourceAccount());
                    req.setTargetAccount(txn.getTargetAccount());
                    req.setCurrency(txn.getCurrency());
                    req.setAmount(txn.getAmount());
                    return processInternal(req).then();
                });
    }
}
