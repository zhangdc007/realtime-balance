package com.mybank.balance.transaction.service;
import com.mybank.balance.transaction.dto.GetTransactionResponse;
import com.mybank.balance.transaction.dto.ProcessTransactionRequest;
import com.mybank.balance.transaction.dto.ProcessTransactionResponse;
import reactor.core.publisher.Mono;
/**
 *
 * @time 2025/2/16 21:38
 * @author zhangdaochuan
 */
public interface TransactionService {

    Mono<GetTransactionResponse> getTransaction(String bizId);

    /**
     * 处理交易
     * 核心接口处理逻辑
     * 一：处理交易接口
     * 1：基于bizId实现幂等性，基于version实现乐观锁
     * a：先在redis 以bizId 加分布式锁，获取锁后，新增 transactions记录 ，一开始为pengding 状态，
     * b:查询source 账户是否存在，且账户余额是否>= amount,如果不存在或者不足，返回异常原因，记录该transactions记录为FAIL，并写入失败原因
     * c:校验成功后，使用乐观锁更新source 账户和target 账户，
     *   开启事务
     *   更新transactions 记录为PROCESSING
     *   乐观锁需要重试，sleep 50(n+1)ms 后重试，n为重试次数，n最大3，，重试时候，retry = retry+1
     *   超过3 ，则transactions记录更新为 RETRY，
     *   如果3次内成功，更新transactions记录为SUCCESS
     *   结束事务
     * d：释放分布式锁，返回transaction状态
     * @param request
     * @return
     */
    Mono<ProcessTransactionResponse> processTransaction(ProcessTransactionRequest request);

    // 异步重试任务调用的方法
    Mono<Void> reprocessTransaction(String bizId);

}
