package com.mybank.balance.transaction.model;

import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.common.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 *
 *
 * @time 2025/2/16 21:19
 * @author zhangdaochuan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions")
public class Transaction {
    @Id
    private Long transactionId;
    private String bizId;
    private Long sourceAccount;
    private Long targetAccount;
    private BigDecimal amount;
    /// PENDING, PROCESSING, SUCCESS, RETRY, FAILED
    private TransactionStatus status;
    private String error;
    private Integer retry;
    private Currency currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //不持久化的字段,用来判断是否需要处理事务
    @Transient
    private boolean needProcess = true;
}
