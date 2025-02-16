package com.mybank.balance.transaction.dao;

import com.mybank.balance.transaction.common.TransactionStatus;
import com.mybank.balance.transaction.model.Transaction;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:30
 */
public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {
    Mono<Transaction> findByBizId(String bizId);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'RETRY' OR (t.status IN :statuses AND t.updatedAt < :threshold)")
    Flux<Transaction> findRetryTransactionsOrStatusAndUpdatedBrfore(@Param("statuses") List<TransactionStatus> statuses, @Param("threshold") LocalDateTime threshold);
}
