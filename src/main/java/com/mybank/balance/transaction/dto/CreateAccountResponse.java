package com.mybank.balance.transaction.dto;

import com.mybank.balance.transaction.common.AccountType;
import com.mybank.balance.transaction.common.Currency;
import com.mybank.balance.transaction.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountResponse {
    private Long accountId;
    private BigDecimal balance;
    private Currency currency;
    private AccountType accountType;
    private LocalDateTime created;

    public static CreateAccountResponse from(Account acc) {
        CreateAccountResponse resp = new CreateAccountResponse();
        resp.setAccountId(acc.getAccountId());
        resp.setBalance(acc.getBalance());
        resp.setCurrency(acc.getCurrency());
        resp.setAccountType(AccountType.fromValue(acc.getAccountType()));
        resp.setCreated(acc.getCreatedAt());
        return resp;
    }
}