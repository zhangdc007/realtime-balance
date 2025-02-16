package com.mybank.balance.transaction.exception;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:50
 */
public enum ErrorCode {
    DUPLICATE_TRANSACTION(1001, "Transaction bizID  already exists"),
    TRANSACTION_NOT_FOUND(1002, "Transaction not found"),
    INVALID_TRANSACTION_DATA(1003, "Invalid transaction data"),
    BIZID_IS_EMPTY(1004, "biz id can not empty"),
    ACCOUNT_NOT_FOUND(1005, "account not found"),
    PAGE_SIZE_INVALID(1006, "page size is invalid"),
    PARAMETER_VALID(1007, "parameter is invalid"),
    LOCK_ACQUIRE_FAILED(1008, "distribute lock acquire failed"),
    CURRENCY_MISMATCH(1009, "currency mismatch"),
    INSUFFICIENT_FUNDS(1010, "Insufficient funds"),
    GENERAL_ERROR(9999, "An unexpected error occurred");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
