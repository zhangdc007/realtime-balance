package com.mybank.balance.transaction.dao;

import com.mybank.balance.transaction.model.Transaction;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:30
 */
public interface TransactionRepository extends R2dbcRepository<Transaction, Long> {
    Mono<Transaction> findByBizId(String bizId);

//    @Query("SELECT * FROM transactions WHERE status = 'RETRY' OR (status IN (:statuses) AND updated_at < :threshold limit 1000)")
    Flux<Transaction> findTop1000ByStatusOrStatusInAndUpdatedAtBefore(String status,@Param("statuses") List<String> statuses, @Param("threshold") LocalDateTime threshold);
}
