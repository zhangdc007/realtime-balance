package com.mybank.balance.transaction.common;

/**
 * 交易状态
 *
 *  1. SUCCESS: 交易成功
 *  2. FAILED: 交易失败
 *  3. RETRY: 交易重试
 *  4. PROCESSING: 交易处理中
 *  5. PENDING: 交易待处理
 *
 *  状态机
 *  PENDING > PROCESSING > FAILED
 *                       > SUCCESS
 *                       > RETRY > PROCESSING > FAILED
 *                                            > SUCCESS
 * @author zhangdaochuan
 * @time 2025/2/16 21:27
 */
public enum TransactionStatus {
    SUCCESS,
    FAILED,
    RETRY,
    PROCESSING,
    PENDING;
}
