package com.mybank.balance.transaction;

import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * @author zhangdaochuan
 * @time 2025/1/17 21:39
 */
public abstract class BaseTest {
    public Transaction generateRandomTransaction(Long sourceId, Long targetAccount,String bizId)
    {
        return generateRandomTransaction(bizId,null,sourceId,targetAccount);
    }
    public Transaction generateRandomTransaction(Long sourceId, Long targetAccount)
    {
        return generateRandomTransaction(null,null,sourceId,targetAccount);
    }

    private static long currentId = 0;

    /**
     * 随机生成一个交易对象
     */
    public Transaction generateRandomTransaction(String bizId,  Long id,Long sourceId, Long targetAccount) {
        Random random = new Random();
        if (id == null) {
            id = currentId++;
        }
        if (bizId == null) {
            bizId = UUID.randomUUID().toString().replace("-", "");
        }

        // 生成 100 到 10000 之间的随机金额
        BigDecimal amount = BigDecimal.valueOf(100 + random.nextInt(9901));

        String description = "Random transaction by " + id;
        LocalDateTime dateTime = LocalDateTime.now().minusDays(random.nextInt(30));

        Transaction transaction = new Transaction();
        transaction.setBizId(bizId);
        transaction.setSourceAccount(sourceId);
        transaction.setTargetAccount(targetAccount);
        transaction.setTransactionId(id);
        transaction.setAmount(amount);
        transaction.setCurrency(Currency.CNY);
        transaction.setError(description);
        transaction.setCreatedAt(dateTime);

        return transaction;
    }

}
