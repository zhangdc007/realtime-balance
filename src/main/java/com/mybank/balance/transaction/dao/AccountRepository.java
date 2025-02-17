package com.mybank.balance.transaction.dao;

import com.mybank.balance.transaction.model.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:28
 */
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
    Mono<Account> findByAccountId(Long accountId);

    // 新增方法，根据账户 ID 列表查询多个账户
    Flux<Account> findAllByAccountIdIn(List<Long> accountIds);
}
