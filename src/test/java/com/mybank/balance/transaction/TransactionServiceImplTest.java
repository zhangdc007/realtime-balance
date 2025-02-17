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
