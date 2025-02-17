package com.mybank.balance.transaction;

import com.mybank.balance.transaction.dao.AccountRepository;
import com.mybank.balance.transaction.dao.TransactionRepository;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.service.TransactionService;
import com.mybank.balance.transaction.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

/**
 * @author zhangdaochuan
 * @time 2025/2/17 01:26
 */
@SpringBootTest()
public class AccountServiceImplTest {
    @Autowired
    private AccountServiceImpl accountService;

    @MockitoBean
    private AccountRepository accountRepository;
    @MockitoBean
    private ReactiveStringRedisTemplate redisTemplate;

//
//    @Test
//    public void testGetAccount() {
//        // 模拟账户 ID
//        Long accountId = 1L;
//
//        // 模拟账户数据
//        Account account = new Account();
//        account.setAccountId(accountId);
//
//        // 模拟账户查询结果
//        when(accountRepository.findByAccountId(accountId)).thenReturn(Mono.just(account));
//
//        // 执行测试方法
//        Mono<CreateAccountResponse> responseMono = accountService.getAccount(accountId);
//
//        // 验证结果
//        StepVerifier.create(responseMono)
//                .expectNext(CreateAccountResponse.from(account))
//                .verifyComplete();
//    }

}
