package com.mybank.balance.transaction.service;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.model.Account;
import reactor.core.publisher.Mono;
/**
 * @author zhangdaochuan
 * @time 2025/2/16 22:17
 */
public interface AccountService {
    Mono<Account> saveAccount(Account account);
    Mono<CreateAccountResponse> getAccount(Long accountId);
}
