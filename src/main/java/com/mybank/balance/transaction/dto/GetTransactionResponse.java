package com.mybank.balance.transaction.dto;

import com.mybank.balance.transaction.common.TransactionStatus;
import com.mybank.balance.transaction.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTransactionResponse {
    private String bizId;
    private String error;
    private TransactionStatus status;
    private BigDecimal amount;
    private Long sourceAccount;
    private Long targetAccount;
    private LocalDateTime created;
    private LocalDateTime updated;

    public static GetTransactionResponse from(Transaction trx) {
        GetTransactionResponse resp = new GetTransactionResponse();
        resp.setBizId(trx.getBizId());
        resp.setAmount(trx.getAmount());
        resp.setStatus(trx.getStatus());
        resp.setSourceAccount(trx.getSourceAccount());
        resp.setTargetAccount(trx.getTargetAccount());
        resp.setCreated(trx.getCreatedAt());
        resp.setError(trx.getError());
        resp.setUpdated(trx.getUpdatedAt());
        return resp;
    }
}
