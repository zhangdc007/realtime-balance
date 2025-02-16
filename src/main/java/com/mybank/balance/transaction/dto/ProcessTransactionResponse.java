package com.mybank.balance.transaction.dto;

import com.mybank.balance.transaction.common.TransactionStatus;
import com.mybank.balance.transaction.model.Transaction;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:35
 */
@Data
public class ProcessTransactionResponse {
    private String bizId;
    private TransactionStatus status;
    private String error;
    private LocalDateTime created;
    public static ProcessTransactionResponse from(Transaction trx) {
        ProcessTransactionResponse resp = new ProcessTransactionResponse();
        resp.setBizId(trx.getBizId());
        resp.setStatus(trx.getStatus());
        resp.setError(trx.getError());
        resp.setCreated(trx.getCreatedAt());
        return resp;
    }

}
