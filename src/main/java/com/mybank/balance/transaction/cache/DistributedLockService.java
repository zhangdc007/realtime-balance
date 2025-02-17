package com.mybank.balance.transaction.cache;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.UUID;
/**
 * 分布式锁
 * @author zhangdaochuan
 * @time 2025/2/16 21:37
 */
@Component
public class DistributedLockService {
    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    /**
     * 尝试获取锁
     * @param key 锁键（例如 bizId）
     * @param expireTime 锁过期时间
     * @return 锁的标识（UUID），如果获取失败返回空
     */
    public Mono<String> acquireLock(String key, Duration expireTime) {
        String value = UUID.randomUUID().toString();
        return redisTemplate.opsForValue()
                .setIfAbsent(key, value, expireTime)
                .flatMap(success -> success ? Mono.just(value) : Mono.empty());
    }

    /**
     * 释放锁
     */
    public Mono<Boolean> releaseLock(String key, String value) {
        return redisTemplate.opsForValue().get(key)
                .flatMap(currentValue -> {
                    if (value.equals(currentValue)) {
                        return redisTemplate.delete(key).map(deleted -> deleted > 0);
                    }
                    return Mono.just(false);
                });
    }
}
