package com.mybank.balance.transaction.service.impl;
import com.mybank.balance.transaction.dao.AccountRepository;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
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

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Account> createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    @Cacheable(value = "accounts", key = "#accountId")
    public Mono<CreateAccountResponse> getAccount(Long accountId) {
        return accountRepository.findByAccountId(accountId)
                .map(account -> CreateAccountResponse.from(account));
    }

    @CachePut(value = "accounts", key = "#account.accountId")
    public Account updateAccountCache(Account account) {
        return account;
    }

    @CacheEvict(value = "accounts", key = "#accountId")
    public void deleteAccountCache(Long accountId) {
    }
}