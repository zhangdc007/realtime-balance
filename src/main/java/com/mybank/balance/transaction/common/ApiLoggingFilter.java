package com.mybank.balance.transaction.common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 接口摘要日志
 * @author zhangdaochuan
 * @time 2025/2/17 15:18
 */
@Component
public class ApiLoggingFilter implements WebFilter {
    private static final Logger apiLogger = LoggerFactory.getLogger("apiLogger");
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange)
                .doOnTerminate(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    String status = exchange.getResponse().getStatusCode() != null && exchange.getResponse().getStatusCode().is2xxSuccessful() ? "T" : "F";
                    apiLogger.info(method+" "+ path+" "+status+" use "+duration+"ms");
                });
    }
}
