package com.mybank.balance.transaction.service.impl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybank.balance.transaction.cache.CacheKey;
import com.mybank.balance.transaction.common.Constants;
import com.mybank.balance.transaction.dao.AccountRepository;
import com.mybank.balance.transaction.dto.CreateAccountResponse;
import com.mybank.balance.transaction.exception.BizException;
import com.mybank.balance.transaction.exception.ErrorCode;
import com.mybank.balance.transaction.model.Account;
import com.mybank.balance.transaction.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
/**
 * 账户服务实现
 * @author zhangdaochuan
 * @time 2025/2/16 22:17
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;
    /**
     * redis client
     */
    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    /**
     * 序列化工具
     */
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Account> createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Mono<CreateAccountResponse> getAccount(Long accountId) {
        String key = CacheKey.getAccountKey(accountId.toString());
        return redisTemplate.opsForValue().get(key)
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(json, CreateAccountResponse.class));
                    } catch (JsonProcessingException e) {
                        return Mono.error(new BizException(e));
                    }
                }).switchIfEmpty(
                        accountRepository.findByAccountId(accountId)
                                .map(account -> CreateAccountResponse.from(account))
                                .flatMap(response -> {
                                    try {
                                        return redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(response))
                                                .thenReturn(response);
                                    } catch (JsonProcessingException e) {
                                        return Mono.error(new BizException(e));
                                    }
                                })
                )
                // 若都查不到，抛出账户未找到异常;
                .switchIfEmpty(Mono.error(new BizException(ErrorCode.ACCOUNT_NOT_FOUND,"accountId:"+accountId)));
    }

}