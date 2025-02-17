package com.mybank.balance.transaction.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mybank.balance.transaction.exception.ErrorCode;

/**
 * 返回结果包装
 * @author zhangdaochuan
 * @time 2025/2/16 21:50
 */
public class Response<T> {
    private Integer statusCode;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public Response(Integer statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static Response<Object> fail(ErrorCode errorCode) {
        return new Response<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static Response<Object> fail(ErrorCode errorCode,String message) {
        return new Response<>(errorCode.getCode(), errorCode.getMessage()+message, null);
    }

    public static Response<Object> fail(Integer statusCode, String message) {
        return new Response<>(statusCode, message, null);
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(Constants.CODE_SUCCESS, Constants.SUCCESS, data);
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
