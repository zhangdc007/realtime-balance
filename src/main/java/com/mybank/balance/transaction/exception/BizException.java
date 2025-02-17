package com.mybank.balance.transaction.exception;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:50
 */
public class BizException extends RuntimeException {

    private final int errorCode; // 错误码

    public BizException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    // 新增构造函数，支持包装 Throwable 类型的异常
    public BizException(Throwable cause) {
        super(cause);
        // 这里可以根据具体情况设置默认的错误码
        this.errorCode = -1;
    }
    // 新增构造函数，支持包装 Throwable 类型的异常
    public BizException(Throwable cause,String message) {
        super(message,cause);
        // 这里可以根据具体情况设置默认的错误码
        this.errorCode = -1;
    }

    public BizException( ErrorCode error,String message)  {
        super(error.getMessage() + ":" + message);
        this.errorCode = error.getCode();
    }

    public BizException( ErrorCode error)  {
        super(error.getMessage());
        this.errorCode = error.getCode();
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "[" +
                "code:" + errorCode +
                " msg:" + getMessage() +
                ']';
    }
}
