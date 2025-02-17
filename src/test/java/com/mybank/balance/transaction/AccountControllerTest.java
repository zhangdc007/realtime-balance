package com.mybank.balance.transaction;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.balance.transaction.common.AccountType;
import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.controller.AccountController;
import com.mybank.balance.transaction.dto.*;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.model.Transaction;
import com.mybank.balance.transaction.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebFluxTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApplicationContext applicationContext;

    private CreateAccountRequest req;
    private CreateAccountResponse resp;
    private Account account;
    private WebTestClient webTestClient;

    @MockitoBean
    private AccountService accountService;


    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        req = new CreateAccountRequest();
        req.setBalance(new BigDecimal("1000.00"));
        req.setCurrency(Currency.CNY);
        req.setType(AccountType.DEBIT);
        account = req.to();
        account.setAccountId(123L);
        resp = CreateAccountResponse.from(account);
    }
    @Test
    public void testCreateAccount() throws Exception {

        // 配置服务层的模拟行为
        when(accountService.createAccount(any(Account.class))).thenReturn(Mono.just(account));

        // 将请求对象转换为 JSON 字符串
        String json = objectMapper.writeValueAsString(req);

        // 执行 POST 请求
        webTestClient.post().uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("success");

    }
    @Test
    public void testGetAccount() throws Exception {

        // 配置服务层的模拟行为
        when(accountService.getAccount(account.getAccountId())).thenReturn(Mono.just(resp));


        // 执行 POST 请求
        webTestClient.get().uri("/api/v1/accounts/{accountsId}",account.getAccountId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("success");

    }
}
