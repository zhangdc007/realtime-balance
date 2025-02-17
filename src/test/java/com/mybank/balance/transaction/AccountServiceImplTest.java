package com.mybank.balance.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.dao.AccountRepository;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.exception.BizException;
import com.mybank.balance.transaction.exception.ErrorCode;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author zhangdaochuan
 * @time 2025/2/17 01:26
 */
@SpringBootTest()
public class AccountServiceImplTest {

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountServiceImpl accountService;

    private Account account;
    private CreateAccountResponse createAccountResponse;

    @BeforeEach
    public void setUp() {
        account = new Account(1L, 1, new BigDecimal("1000.00"), Currency.CNY, 1, LocalDateTime.of(2025, 2, 17, 1, 26), LocalDateTime.of(2025, 2, 17, 1, 26));
        createAccountResponse = CreateAccountResponse.from(account);
    }

    @Test
    public void createAccount_SuccessfulSave_ReturnsAccount() {
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(account));

        Account savedAccount = accountService.createAccount(account).block();

        assertEquals(account, savedAccount);
    }

    @Test
    public void createAccount_SaveFails_ThrowsException() {
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> accountService.createAccount(account).block());
    }

    @Test
    public void getAccount_SuccessfulFind_ReturnsCreateAccountResponse() throws JsonProcessingException {

        when(accountRepository.findByAccountId(any(Long.class))).thenReturn(Mono.just(account));
        // 模拟 ReactiveStringRedisTemplate 的行为
        ReactiveValueOperations<String, String> reactiveValueOperations = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        // 模拟从 Redis 获取数据，返回一个有效的 Mono 对象
        String json = objectMapper.writeValueAsString(createAccountResponse);
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(Mono.just(json));
        when(redisTemplate.opsForValue().set(anyString(),anyString())).thenReturn(Mono.just(true));
        CreateAccountResponse response = accountService.getAccount(account.getAccountId()).block();

        assertEquals(createAccountResponse, response);
    }

    @Test
    public void getAccount_AccountNotFound_ReturnsEmptyMono() {
        when(accountRepository.findByAccountId(any(Long.class))).thenReturn(Mono.empty());
        // 模拟 ReactiveStringRedisTemplate 的行为
        ReactiveValueOperations<String, String> reactiveValueOperations = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        // 模拟从 Redis 获取数据，返回一个有效的 Mono 对象
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(Mono.empty());
        when(redisTemplate.opsForValue().set(anyString(),anyString())).thenReturn(Mono.just(true));
        // 断言抛出 BizException 异常
        BizException exception = assertThrows(BizException.class, () -> {
            accountService.getAccount(999L).block();
        });
        // 验证异常的错误码和错误信息
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND.getCode(), exception.getErrorCode());
    }

    @Test
    public void getAccount_FindFails_ThrowsException() {
        when(accountRepository.findByAccountId(any(Long.class))).thenThrow(new RuntimeException("Find failed"));

        assertThrows(RuntimeException.class, () -> accountService.getAccount(999L).block());
    }
}
