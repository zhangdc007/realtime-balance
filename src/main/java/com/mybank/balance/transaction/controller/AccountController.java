package com.mybank.balance.transaction.controller;
import com.mybank.balance.transaction.dto.CreateAccountRequest;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 22:11
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest req) {
        Account account = req.to();
        return accountService.createAccount(account)
                .map(acc -> CreateAccountResponse.from(acc));
    }

    @GetMapping(value = "/{accountId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CreateAccountResponse> getAccount(@PathVariable Long accountId) {
        // 可添加 Redis 缓存逻辑，此处直接查询数据库
        return accountService.getAccount(accountId);
    }
}
