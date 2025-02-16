package com.mybank.balance.transaction.controller;

import com.mybank.balance.transaction.common.Response;
import com.mybank.balance.transaction.dao.TransactionRepository;
import com.mybank.balance.transaction.dto.GetTransactionResponse;
import com.mybank.balance.transaction.dto.ProcessTransactionRequest;
import com.mybank.balance.transaction.dto.ProcessTransactionResponse;
import com.mybank.balance.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
/**
 * 
 * 
 * @time 2025/2/16 22:18
 * @author zhangdaochuan
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Response<ProcessTransactionResponse>> processTransaction(@Valid @RequestBody ProcessTransactionRequest req) {
        return transactionService.processTransaction(req)
                .map(ProcessTransactionResponse -> Response.success(ProcessTransactionResponse));;
    }

    @GetMapping(value = "/{bizId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Response<GetTransactionResponse>> getTransaction(@PathVariable String bizId) {
        return transactionService.getTransaction(bizId)
                .map(GetTransactionResponse -> Response.success(GetTransactionResponse));
    }
}
