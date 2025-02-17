package com.mybank.balance.transaction;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.controller.TransactionController;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.dto.GetTransactionResponse;
import com.mybank.balance.transaction.dto.ProcessTransactionRequest;
import com.mybank.balance.transaction.dto.ProcessTransactionResponse;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.model.Transaction;
import com.mybank.balance.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@WebFluxTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProcessTransactionRequest req;
    private GetTransactionResponse resp;
    private Transaction transaction;
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();

        // 模拟请求参数
        req = new ProcessTransactionRequest();
        req.setAmount(new BigDecimal("100"));
        req.setBizId("123");
        req.setCurrency(Currency.CNY);
        req.setSourceAccount(1L);
        req.setTargetAccount(2L);

        transaction = req.to();
        resp = GetTransactionResponse.from(transaction);
    }

    @Test
    public void testProcessTransaction() throws Exception {
        // 模拟服务层处理结果
        ProcessTransactionResponse response = ProcessTransactionResponse.from(transaction);

        // 配置服务层的模拟行为
        when(transactionService.processTransaction(req)).thenReturn(Mono.just(response));

        // 将请求对象转换为 JSON 字符串
        String json = objectMapper.writeValueAsString(req);

        // 执行 POST 请求
        webTestClient.post().uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("success");

        // 验证服务层方法被调用一次
        verify(transactionService, times(1)).processTransaction(req);
    }

    @Test
    public void testGetTransaction() throws Exception {
        // 模拟业务 ID
        String bizId = req.getBizId();

        // 配置服务层的模拟行为
        when(transactionService.getTransaction(bizId)).thenReturn(Mono.just(resp));

        // 执行 GET 请求
        webTestClient.get().uri("/api/v1/transactions/{bizId}", bizId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("success");

        // 验证服务层方法被调用一次
        verify(transactionService, times(1)).getTransaction(bizId);
    }
}

