package com.mybank.balance.transaction.service.impl;

import com.mybank.balance.transaction.common.Constants;
import com.mybank.balance.transaction.cache.DistributedLockService;
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
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    private AccountServiceImpl accountService;

    private static final Duration LOCK_EXPIRE = Duration.ofSeconds(10);

    @Override
    public Mono<GetTransactionResponse> getTransaction(String bizId) {
        return transactionRepository.findByBizId(bizId)
                .map(txn -> GetTransactionResponse.from(txn))
                .switchIfEmpty(Mono.error(new BizException(ErrorCode.TRANSACTION_NOT_FOUND)));
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
                                                .thenReturn(ProcessTransactionResponse.from(response))
                                )
                );
    }

    private Mono<Transaction> processInternal(ProcessTransactionRequest request) {
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
                    // 校验 source 和 target 不能一致
                    if (request.getSourceAccount().equals(Long.parseLong(txn.getTargetAccount().toString()))) {
                        txn.setStatus(TransactionStatus.FAILED);
                        txn.setError("Source and target accounts cannot be the same");
                        txn.setUpdatedAt(LocalDateTime.now());
                        return transactionRepository.save(txn)
                                .then(Mono.error(new BizException(ErrorCode.SOURCE_TARGET_ACCOUNT_SAME)));
                    }
                    // 一次性查询 source 和 target 账户
                    List<Long> accountIds = List.of(request.getSourceAccount(), Long.parseLong(txn.getTargetAccount().toString()));
                    // 查询 source ,target账户是否符合业务要求：
                    /**
                     *   source 和 target 不能一致
                     *   查询 source 账户是否存在，且账户余额是否>= amount,如果不存在或者不足，返回异常原因，记录该transactions记录为FAIL，并写入失败原因
                     *   查询 target 账户是否存在，且currency和AccountType是否一致，如果不一致，返回异常原因，记录该transactions记录为FAIL，并写入失败原因
                     *   检查 Transaction 请求的 currency和source账户是否一致
                     */
                    return accountRepository.findAllByAccountIdIn(accountIds)
                            .collectList()
                            .flatMap(accounts -> {
                                Account sourceAccount = null;
                                Account targetAccount = null;
                                for (Account account : accounts) {
                                    if (account.getAccountId().equals(request.getSourceAccount())) {
                                        sourceAccount = account;
                                    } else if (account.getAccountId().equals(request.getTargetAccount())) {
                                        targetAccount = account;
                                    }
                                }
                                // 校验 source 账户
                                if (sourceAccount == null) {
                                    txn.setStatus(TransactionStatus.FAILED);
                                    txn.setError("Source account not found");
                                    txn.setUpdatedAt(LocalDateTime.now());
                                    return transactionRepository.save(txn)
                                            .then(Mono.error(new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "source account "+request.getSourceAccount()+" not found")));
                                }
                                if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
                                    txn.setStatus(TransactionStatus.FAILED);
                                    txn.setError("Insufficient funds ，less then:" + request.getCurrency() + " " + request.getAmount());
                                    txn.setUpdatedAt(LocalDateTime.now());
                                    return transactionRepository.save(txn)
                                            .then(Mono.error(new BizException(ErrorCode.INSUFFICIENT_FUNDS, "balance is not enough")));
                                }
                                // 校验 target 账户
                                if (targetAccount == null) {
                                    txn.setStatus(TransactionStatus.FAILED);
                                    txn.setError("Target account not found");
                                    txn.setUpdatedAt(LocalDateTime.now());
                                    return transactionRepository.save(txn)
                                            .then(Mono.error(new BizException(ErrorCode.ACCOUNT_NOT_FOUND, "target account "+request.getTargetAccount()+" not found")));
                                }
                                //校验货币是否一致
                                if (!targetAccount.getCurrency().equals(sourceAccount.getCurrency())) {
                                    txn.setStatus(TransactionStatus.FAILED);
                                    txn.setError("Target account currency mismatch");
                                    txn.setUpdatedAt(LocalDateTime.now());
                                    return transactionRepository.save(txn)
                                            .then(Mono.error(new BizException(ErrorCode.CURRENCY_MISMATCH, "target and source account currency mismatch")));
                                }
                                // 检查 Transaction 请求的 currency 和 source 账户是否一致
                                if (!sourceAccount.getCurrency().equals(request.getCurrency())) {
                                    txn.setStatus(TransactionStatus.FAILED);
                                    txn.setError("Transaction currency:"+request.getCurrency()+" does not match source account currency:"+sourceAccount.getCurrency());
                                    txn.setUpdatedAt(LocalDateTime.now());
                                    return transactionRepository.save(txn)
                                            .then(Mono.error(new BizException(ErrorCode.CURRENCY_MISMATCH, "transaction currency mismatch with source account")));
                                }
                                //校验账户类型是否一致
                                if (!targetAccount.getAccountType().equals(sourceAccount.getAccountType())) {
                                    txn.setStatus(TransactionStatus.FAILED);
                                    txn.setError("Target account type mismatch");
                                    txn.setUpdatedAt(LocalDateTime.now());
                                    return transactionRepository.save(txn)
                                            .then(Mono.error(new BizException(ErrorCode.ACCOUNT_TYPE_MISMATCH, "target and source account type mismatch")));
                                }
                                // 进入乐观锁更新阶段（重试机制，最多重试 3 次）
                                return transactionalOperator.transactional(
                                        attemptOptimisticUpdate(txn, sourceAccount, request.getAmount(), 0)
                                );
                            });
                })
                .doOnSuccess(response -> {
                    //缓存失效
                    redisTemplate.delete(response.getSourceAccount().toString(),response.getTransactionId().toString());
                });
    }
    /**
     * 尝试乐观锁更新 source 与 target 账户余额，重试最多 3 次
     */
    private Mono<Transaction> attemptOptimisticUpdate(Transaction txn, Account sourceAccount,
                                                                     BigDecimal amount, int retryCount) {
        if (retryCount > 3) {
            txn.setStatus(TransactionStatus.FAILED);
            txn.setError("Retry count:" + retryCount +" exceeded 3");
            txn.setUpdatedAt(LocalDateTime.now());
            return transactionRepository.save(txn)
                    .then(Mono.just(txn));
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
        Mono<Account> updateTarget = accountRepository.findByAccountId(txn.getTargetAccount())
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
                            .thenReturn(txn);
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
                        txn.setStatus(TransactionStatus.FAILED);
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
