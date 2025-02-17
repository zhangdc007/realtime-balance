package com.mybank.balance.transaction.model;

import com.mybank.balance.transaction.common.AccountType;
import com.mybank.balance.transaction.common.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("accounts")
public class Account {
    //自增ID
    @Id
    private Long accountId;
    private Integer accountType;
    private BigDecimal balance;
    private Currency currency;
    // 乐观锁版本字段
    @Version
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
