package com.mybank.balance.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.balance.transaction.cache.CacheKey;
import com.mybank.balance.transaction.cache.DistributedLockService;
import com.mybank.balance.transaction.common.AccountType;
import com.mybank.balance.transaction.common.Constants;
import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.common.TransactionStatus;
import com.mybank.balance.transaction.dao.AccountRepository;
import com.mybank.balance.transaction.dao.TransactionRepository;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.dto.GetTransactionResponse;
import com.mybank.balance.transaction.dto.ProcessTransactionRequest;
import com.mybank.balance.transaction.dto.ProcessTransactionResponse;
import com.mybank.balance.transaction.exception.BizException;
import com.mybank.balance.transaction.exception.ErrorCode;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.model.Transaction;
import com.mybank.balance.transaction.service.TransactionService;
import com.mybank.balance.transaction.service.impl.TransactionServiceImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author zhangdaochuan
 * @time 2025/2/17 22:23
 */
@SpringBootTest()
public class TransactionServiceImplTest {

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private DistributedLockService lockService;

    @MockitoBean
    private ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @MockitoBean
    private TransactionalOperator transactionalOperator;

    @Autowired
    private TransactionServiceImpl transactionService;


    private Account sourceAccount;
    private Account targetAccount;
    private Transaction transaction;
    private ProcessTransactionRequest req;
    private ProcessTransactionResponse resp;

    @BeforeEach
    public void setUp() {
        sourceAccount = new Account(1L, 1, new BigDecimal("1000.00"), Currency.CNY, 1, LocalDateTime.of(2025, 2, 17, 1, 26), LocalDateTime.of(2025, 2, 17, 1, 26));
        targetAccount = new Account(2L, 1, new BigDecimal("1500.00"), Currency.CNY, 1, LocalDateTime.of(2025, 2, 17, 1, 26), LocalDateTime.of(2025, 2, 17, 1, 26));
        req = new ProcessTransactionRequest();
        req.setBizId("testBizId1");
        req.setSourceAccount(sourceAccount.getAccountId());
        req.setTargetAccount(targetAccount.getAccountId());
        req.setCurrency(Currency.CNY);
        req.setAmount(new BigDecimal("100.00"));
        transaction = req.to();
        resp = ProcessTransactionResponse.from(transaction);
    }

    // 反射调用私有方法的工具方法
    private <T> Mono<T> invokePrivateMethod(String methodName, Object... args) throws Exception {
        Class<?>[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        Method method = TransactionServiceImpl.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return (Mono<T>) method.invoke(transactionService, args);
    }

    @SneakyThrows
    @Test
    public void testGetTransaction() {
        // 模拟 Redis 中存在交易记录
        String bizId = "testBizId";
        GetTransactionResponse expectedResponse = new GetTransactionResponse();
        when(redisTemplate.opsForValue().get(CacheKey.getTxnKey(bizId))).thenReturn(Mono.just("jsonResponse"));
        when(objectMapper.readValue("jsonResponse", GetTransactionResponse.class)).thenReturn(expectedResponse);

        Mono<GetTransactionResponse> actualResponse = transactionService.getTransaction(bizId);

        StepVerifier.create(actualResponse)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @SneakyThrows
    @Test
    public void testGetTransactionNotFoundInRedis() {
        // 模拟 Redis 中不存在交易记录，但数据库中存在
        String bizId = "testBizId";
        Transaction transaction = new Transaction();
        when(redisTemplate.opsForValue().get(CacheKey.getTxnKey(bizId))).thenReturn(Mono.empty());
        when(transactionRepository.findByBizId(bizId)).thenReturn(Mono.just(transaction));
        when(objectMapper.writeValueAsString(any())).thenReturn("jsonResponse");

        Mono<GetTransactionResponse> actualResponse = transactionService.getTransaction(bizId);

        StepVerifier.create(actualResponse)
                .expectNext(GetTransactionResponse.from(transaction))
                .verifyComplete();
    }

    @Test
    public void testGetTransactionNotFoundInBoth() {
        // 模拟 Redis 和数据库中都不存在交易记录
        String bizId = "testBizId";
        when(redisTemplate.opsForValue().get(CacheKey.getTxnKey(bizId))).thenReturn(Mono.empty());
        when(transactionRepository.findByBizId(bizId)).thenReturn(Mono.empty());

        Mono<GetTransactionResponse> actualResponse = transactionService.getTransaction(bizId);

        StepVerifier.create(actualResponse)
                .expectError(BizException.class)
                .verify();
    }

//    @SneakyThrows
//    @Test
//    public void testProcessTransaction() {
//        // 模拟获取锁成功
//        String bizId = req.getBizId();
//        String lockValue = "lockValue";
//        List<Transaction> transactions = Arrays.asList(transaction);
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        when(lockService.acquireLock(Constants.LOCK_KEY + bizId, Constants.LOCK_EXPIRE)).thenReturn(Mono.just(lockValue));
//        when(transactionRepository.findAllByStatusOrStatusInAndUpdatedAtBefore(any(String.class), any(List.class), any(LocalDateTime.class)))
//                .thenAnswer(invocation -> Mono.just(transactions));
////                .thenAnswer(new Answer<Mono<List<Transaction>>>() {
////                    @Override
////                    public Mono<List<Transaction>> answer(InvocationOnMock invocation) throws Throwable {
////                        return Mono.just(Arrays.asList(transaction));
////                    }
////                });
//        // 模拟内部处理成功
//        Mono<Transaction> internalResult = Mono.just(transaction);
//        Mono<Transaction> internalCall = invokePrivateMethod("processInternal", request, false);
//        when(internalCall).thenReturn(internalResult);
//
//        Mono<ProcessTransactionResponse> actualResponse = transactionService.processTransaction(request);
//
//        StepVerifier.create(actualResponse)
//                .expectNext(ProcessTransactionResponse.from(transaction))
//                .verifyComplete();
//    }

//    @Test
//    public void testProcessTransactionLockAcquireFailed() {
//        // 模拟获取锁失败
//        String bizId = "testBizId";
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        when(lockService.acquireLock(Constants.LOCK_KEY + bizId, Constants.LOCK_EXPIRE)).thenReturn(Mono.error(new BizException(ErrorCode.LOCK_ACQUIRE_FAILED)));
//
//        Mono<ProcessTransactionResponse> actualResponse = transactionService.processTransaction(request);
//
//        StepVerifier.create(actualResponse)
//                .expectError(BizException.class)
//                .verify();
//    }
//
//    @Test
//    public void testProcessInternalNewTransaction() throws Exception {
//        // 模拟新的交易任务
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        Transaction transaction = new Transaction();
//        when(transactionRepository.findByBizId(request.getBizId())).thenReturn(Mono.empty());
//        when(transactionRepository.save(any())).thenReturn(Mono.just(transaction));
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", request, false);
//
//        StepVerifier.create(actualResponse)
//                .expectNext(transaction)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testProcessInternalExistingTransaction() throws Exception {
//        // 模拟已存在的交易任务
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        Transaction existingTransaction = new Transaction();
//        existingTransaction.setNeedProcess(false);
//        when(transactionRepository.findByBizId(request.getBizId())).thenReturn(Mono.just(existingTransaction));
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", request, false);
//
//        StepVerifier.create(actualResponse)
//                .expectNext(existingTransaction)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testProcessInternalSourceTargetAccountSame() throws Exception {
//        // 模拟源账户和目标账户相同
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        request.setSourceAccount(1L);
//        request.setTargetAccount(1L);
//        Transaction transaction = new Transaction();
//        when(transactionRepository.findByBizId(request.getBizId())).thenReturn(Mono.just(transaction));
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", request, false);
//
//        StepVerifier.create(actualResponse)
//                .expectError(BizException.class)
//                .verify();
//    }
//
//    @Test
//    public void testProcessInternalSourceAccountNotFound() throws Exception {
//        // 模拟源账户不存在
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        request.setSourceAccount(1L);
//        Transaction transaction = new Transaction();
//        when(transactionRepository.findByBizId(request.getBizId())).thenReturn(Mono.just(transaction));
//        when(accountRepository.findAllByAccountIdIn(Arrays.asList(request.getSourceAccount(), transaction.getTargetAccount())))
//                .thenAnswer(new Answer<Mono<List<Account>>>() {
//            @Override
//            public Mono<List<Account>> answer(InvocationOnMock invocation) throws Throwable {
//                return Mono.just(Arrays.asList());
//            }
//        });
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", request, false);
//
//        StepVerifier.create(actualResponse)
//                .expectError(BizException.class)
//                .verify();
//    }
//
//    @Test
//    public void testProcessInternalInsufficientFunds() throws Exception {
//        // 模拟余额不足
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        request.setSourceAccount(1L);
//        request.setAmount(BigDecimal.TEN);
//        Transaction transaction = new Transaction();
//        transaction.setStatus(TransactionStatus.PENDING);
//        Account sourceAccount = new Account();
//        sourceAccount.setBalance(BigDecimal.ONE);
//        when(transactionRepository.findByBizId(request.getBizId())).thenReturn(Mono.just(transaction));
//        when(accountRepository.findAllByAccountIdIn(Arrays.asList(request.getSourceAccount(), transaction.getTargetAccount())))
//                .thenAnswer(new Answer<Mono<List<Account>>>() {
//                    @Override
//                    public Mono<List<Account>> answer(InvocationOnMock invocation) throws Throwable {
//                        return Mono.just(Arrays.asList());
//                    }
//                });
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", request, false);
//
//        StepVerifier.create(actualResponse)
//                .expectError(BizException.class)
//                .verify();
//    }
//
//    @Test
//    public void testProcessInternalTargetAccountNotFound() throws Exception {
//        // 模拟目标账户不存在
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        request.setSourceAccount(1L);
//        request.setTargetAccount(2L);
//        Transaction transaction = new Transaction();
//        transaction.setStatus(TransactionStatus.PENDING);
//        Account sourceAccount = new Account();
//        sourceAccount.setBalance(BigDecimal.TEN);
//        when(transactionRepository.findByBizId(request.getBizId())).thenReturn(Mono.just(transaction));
//        when(accountRepository.findAllByAccountIdIn(Arrays.asList(request.getSourceAccount(), transaction.getTargetAccount()))).thenAnswer(new Answer<Mono<List<Account>>>() {
//            @Override
//            public Mono<List<Account>> answer(InvocationOnMock invocation) throws Throwable {
//                return Mono.just(Arrays.asList());
//            }
//        });
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", request, false);
//
//        StepVerifier.create(actualResponse)
//                .expectError(BizException.class)
//                .verify();
//    }
//
//    @Test
//    public void testProcessInternalCurrencyMismatch() throws Exception {
//        // 模拟货币不匹配
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        request.setSourceAccount(1L);
//        request.setTargetAccount(2L);
//        request.setCurrency(Currency.CNY);
//        Transaction transaction = new Transaction();
//        transaction.setStatus(TransactionStatus.PENDING);
//        Account sourceAccount = new Account();
//        sourceAccount.setBalance(BigDecimal.TEN);
//        sourceAccount.setCurrency(Currency.EUR);
//        when(transactionRepository.findByBizId(request.getBizId())).thenReturn(Mono.just(transaction));
//        when(accountRepository.findAllByAccountIdIn(Arrays.asList(request.getSourceAccount(), transaction.getTargetAccount())))
//                .thenAnswer(new Answer<Mono<List<Account>>>() {
//            @Override
//            public Mono<List<Account>> answer(InvocationOnMock invocation) throws Throwable {
//                return Mono.just(Arrays.asList());
//            }
//        });
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", request, false);
//
//        StepVerifier.create(actualResponse)
//                .expectError(BizException.class)
//                .verify();
//    }
//
//    @Test
//    public void testProcessInternalAccountTypeMismatch() throws Exception {
//        // 模拟账户类型不匹配
//        ProcessTransactionRequest request = new ProcessTransactionRequest();
//        request.setSourceAccount(1L);
//        request.setTargetAccount(2L);
//        request.setCurrency(Currency.USD);
//        Transaction transaction = new Transaction();
//        transaction.setStatus(TransactionStatus.PENDING);
//        Account sourceAccount = new Account();
//        sourceAccount.setBalance(BigDecimal.TEN);
//        sourceAccount.setCurrency(Currency.USD);
//        sourceAccount.setAccountType(AccountType.DEBIT.getValue());
//        Account targetAccount = new Account();
//        targetAccount.setBalance(BigDecimal.TEN);
//        targetAccount.setCurrency(Currency.USD);
//        targetAccount.setAccountType(AccountType.CREDIT.getValue());
//        when(transactionRepository.findByBizId(request.getBizId())).thenReturn(Mono.just(transaction));
//        when(accountRepository.findAllByAccountIdIn(Arrays.asList(request.getSourceAccount(), transaction.getTargetAccount()))).thenAnswer(new Answer<Mono<List<Account>>>() {
//            @Override
//            public Mono<List<Account>> answer(InvocationOnMock invocation) throws Throwable {
//                return Mono.just(Arrays.asList());
//            }
//        });
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", request, false);
//
//        StepVerifier.create(actualResponse)
//                .expectError(BizException.class)
//                .verify();
//    }
//
//    @Test
//    public void testProcessInternalOptimisticUpdateSuccess() throws Exception {
//        // 模拟乐观锁更新成功
//        Transaction transaction = new Transaction();
//        Account sourceAccount = new Account();
//        sourceAccount.setBalance(BigDecimal.TEN);
//        BigDecimal amount = BigDecimal.ONE;
//
//        Mono<Transaction> optimisticUpdateResult = Mono.just(transaction);
//        Mono<Transaction> optimisticUpdateCall = invokePrivateMethod("attemptOptimisticUpdate", transaction, sourceAccount, amount, 0);
//        when(optimisticUpdateCall).thenReturn(optimisticUpdateResult);
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", new ProcessTransactionRequest(), false);
//
//        StepVerifier.create(actualResponse)
//                .expectNext(transaction)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testProcessInternalOptimisticUpdateRetry() throws Exception {
//        // 模拟乐观锁更新需要重试
//        Transaction transaction = new Transaction();
//        Account sourceAccount = new Account();
//        sourceAccount.setBalance(BigDecimal.TEN);
//        BigDecimal amount = BigDecimal.ONE;
//
//        Mono<Transaction> optimisticUpdateResult = Mono.error(new RuntimeException());
//        Mono<Transaction> optimisticUpdateCall = invokePrivateMethod("attemptOptimisticUpdate", transaction, sourceAccount, amount, 0);
//        when(optimisticUpdateCall).thenReturn(optimisticUpdateResult);
//
//        Mono<Transaction> actualResponse = invokePrivateMethod("processInternal", new ProcessTransactionRequest(), false);
//
//        StepVerifier.create(actualResponse)
//                .expectNext(transaction)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testReprocessTransaction() throws Exception {
//        // 模拟交易存在且重试次数未超过限制
//        String bizId = "testBizId";
//        Transaction transaction = new Transaction();
//        transaction.setRetry(5);
//        when(transactionRepository.findByBizId(bizId)).thenReturn(Mono.just(transaction));
//
//        // 模拟获取锁成功
//        String lockValue = "lockValue";
//        when(lockService.acquireLock(Constants.LOCK_KEY + bizId, Constants.LOCK_EXPIRE)).thenReturn(Mono.just(lockValue));
//
//        // 模拟内部处理成功
//        Transaction processedTransaction = new Transaction();
//        Mono<Transaction> internalResult = Mono.just(processedTransaction);
//        Mono<Transaction> internalCall = invokePrivateMethod("processInternal", new ProcessTransactionRequest(), true);
//        when(internalCall).thenReturn(internalResult);
//
//        Mono<Void> actualResponse = transactionService.reprocessTransaction(bizId);
//
//        StepVerifier.create(actualResponse)
//                .verifyComplete();
//    }
//
//    @Test
//    public void testReprocessTransactionRetryExceeded() {
//        // 模拟交易存在且重试次数超过限制
//        String bizId = "testBizId";
//        Transaction transaction = new Transaction();
//        transaction.setRetry(7);
//        when(transactionRepository.findByBizId(bizId)).thenReturn(Mono.just(transaction));
//
//        Mono<Void> actualResponse = transactionService.reprocessTransaction(bizId);
//
//        StepVerifier.create(actualResponse)
//                .verifyComplete();
//    }

    @Test
    public void testReprocessTransactionNotFound() {
        // 模拟交易不存在
        String bizId = "testBizId";
        when(transactionRepository.findByBizId(bizId)).thenReturn(Mono.empty());

        Mono<Void> actualResponse = transactionService.reprocessTransaction(bizId);

        StepVerifier.create(actualResponse)
                .verifyComplete();
    }
}
