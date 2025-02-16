package com.mybank.balance.transaction.dto;
import com.fasterxml.jackson.databind.annotation.EnumNaming;
import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.model.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:34
 */
@Data
public class ProcessTransactionRequest {
    @NotBlank
    private String bizId;
    @NotNull
    private Long sourceAccount;
    @NotNull
    private Long targetAccount;
    @NotNull
    private Currency currency;
    @NotNull
    private BigDecimal amount;

    public Transaction to() {
        Transaction transaction = new Transaction();
        transaction.setBizId(this.bizId);
        transaction.setSourceAccount(this.sourceAccount);
        transaction.setTargetAccount(this.targetAccount);
        transaction.setCurrency(this.currency);
        transaction.setAmount(this.amount);
        transaction.setRetry(0);
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}
