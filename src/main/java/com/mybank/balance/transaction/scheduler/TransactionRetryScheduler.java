package com.mybank.balance.transaction.scheduler;

import com.mybank.balance.transaction.cache.DistributedLockService;
import com.mybank.balance.transaction.common.TransactionStatus;
import com.mybank.balance.transaction.dao.TransactionRepository;
import com.mybank.balance.transaction.dto.ProcessTransactionResponse;
import com.mybank.balance.transaction.exception.BizException;
import com.mybank.balance.transaction.exception.ErrorCode;
import com.mybank.balance.transaction.model.Transaction;
import com.mybank.balance.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 定时重试
 * @author zhangdaochuan
 * @time 2025/2/16 22:48
 */
@Component
public class TransactionRetryScheduler {

    Logger logger = LoggerFactory.getLogger(TransactionRetryScheduler.class);
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private DistributedLockService lockService;
    private static final String lockKey = "TransactionRetryScheduler";
    /**
     * 间隔的定时任务，定时查询 transactions status=“RETRY” 或者 “PENGDING” ,"PROCESSING" 且 now - 10s > updated 的记录
     * 重新处理交易：
     * a:使用基于bizId获取分布式锁，如果没获取到，则跳过，处理下一条记录
     * b：如果获取到锁，如果retry > 6 次，则置为FAIL ，error 写为重试超过最大次数，需要人工介入，注释：发送告警短信通知人工介入。
     *    如果retry <6 ,重复 一：处理交易接口的 b,c步骤
     * c:释放分布式锁
     */
    // 每 10s + 随机 0~5 秒执行一次（单位：毫秒）
    @Scheduled(fixedDelayString = "#{10000 + T(java.util.concurrent.ThreadLocalRandom).current().nextInt(5000)}")
    public void retryTransactions() {
        // 尝试获取分布式锁
        lockService.acquireLock(lockKey, Duration.ofSeconds(10))
                .switchIfEmpty(Mono.empty())
                .flatMap(lockValue -> {
                    // 查询状态为 RETRY、PENDING、PROCESSING 且 updatedAt 早于 threshold 的记录
                    return transactionRepository.findAllByStatusOrStatusInAndUpdatedAtBefore(
                            TransactionStatus.RETRY.name(),
                            Arrays.asList(TransactionStatus.PENDING.name(), TransactionStatus.PROCESSING.name()),
                            LocalDateTime.now().minusSeconds(10)
                    ).flatMap(txn ->
                            transactionService.reprocessTransaction(txn.getBizId())
                                    .onErrorResume(ex -> {
                                        // 可记录日志，通知告警
                                        logger.error("retryTransactions:" + txn.getBizId() + " process failed", ex);
                                        return Mono.empty();
                                    })
                    ).then(
                            // 释放锁
                            lockService.releaseLock(lockKey, lockValue)
                                    .doOnError(ex -> logger.error("Failed to release lock: {}", lockKey, ex))
                                    .thenReturn(true)
                    );
                })
                .subscribe(
                        result -> {
                            if (result) {
                                logger.info("Transaction retry completed successfully.");
                            } else {
                                logger.warn("Transaction retry failed or lock not acquired.");
                            }
                        },
                        error -> logger.error("Error occurred during transaction retry: ", error)
                );
    }
}
