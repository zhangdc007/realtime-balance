package com.mybank.balance.transaction.exception;

import com.mybank.balance.transaction.common.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;


/**
 * @author zhangdaochuan
 * @time 2025/2/16 22:54
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public Mono<Response> handleBizException(BizException ex) {
        logger.error("biz exception:"+ex.toString(), ex);
        return Mono.just(Response.fail(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<Response> handleException(Exception ex) {
        logger.error("unknow exception:"+ex.toString(), ex);
        return Mono.just(Response.fail(ErrorCode.GENERAL_ERROR,ex.getMessage()));
    }
}
