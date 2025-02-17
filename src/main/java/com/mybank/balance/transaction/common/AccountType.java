package com.mybank.balance.transaction.common;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:50
 */
public enum AccountType {
    // 信用卡
    CREDIT(0),
    // 借记卡
    DEBIT(1);

    private final int value;

    AccountType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    public static AccountType fromValue(int value) {
        for (AccountType type : AccountType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching AccountType for value: " + value);
    }
}
