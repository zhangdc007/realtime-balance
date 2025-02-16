package com.mybank.balance.transaction;

import com.mybank.balance.transaction.model.Transaction;
import com.mybank.balance.transaction.model.TransationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * @author zhangdaochuan
 * @time 2025/1/17 21:39
 */
public abstract class BaseTest {
    public Transaction generateRandomTransaction(String bizId)
    {
        return generateRandomTransaction(bizId,null);
    }
    public Transaction generateRandomTransaction()
    {
        return generateRandomTransaction(null,null);
    }
    private static long currentId = 0;
    /**
     * 随机生成一个交易对象
     */
    public Transaction generateRandomTransaction(String bizId,Long id) {
        Random random = new Random();
        if (id == null) {
            id = currentId++;
        }
        if (bizId == null) {
            bizId = "BIZ" + currentId;
        }
        String accountId = "ACCT" + random.nextInt(1000);
        TransationType type = random.nextBoolean() ? TransationType.WITHDRAW :TransationType.DEPOSIT;
        BigDecimal amount = BigDecimal.valueOf(random.nextDouble() * 1000);
        String description = "Random transaction by " + id;
        LocalDateTime dateTime = LocalDateTime.now().minusDays(random.nextInt(30));

        return new Transaction(id, bizId, accountId, type, amount, description, dateTime);
    }

}
