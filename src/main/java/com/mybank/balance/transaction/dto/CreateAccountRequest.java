package com.mybank.balance.transaction.dto;
import com.mybank.balance.transaction.common.AccountType;
import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.model.Account;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:31
 */
@Data
public class CreateAccountRequest {
    @NotNull
    private Currency currency;
    //账户类型 1：借记卡
    @NotNull
    private AccountType type;
    //初始账户
    @Min(0)
    private BigDecimal balance;

    public Account to() {
        Account account = new Account();
        account.setCurrency(this.currency);
        account.setAccountType(this.type.getValue());
        account.setBalance(this.balance);
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }
}