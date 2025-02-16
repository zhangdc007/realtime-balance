package com.mybank.balance.transaction.dao;

import com.mybank.balance.transaction.model.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:28
 */
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
    Mono<Account> findByAccountId(Long accountId);
}
