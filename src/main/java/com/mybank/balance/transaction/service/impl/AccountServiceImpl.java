package com.mybank.balance.transaction.service.impl;
import com.mybank.balance.transaction.dao.AccountRepository;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
/**
 * @author zhangdaochuan
 * @time 2025/2/16 22:17
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Mono<Account> saveAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Mono<CreateAccountResponse> getAccount(Long accountId) {
        return accountRepository.findByAccountId(accountId)
                .map(account -> CreateAccountResponse.from(account));
    }
}