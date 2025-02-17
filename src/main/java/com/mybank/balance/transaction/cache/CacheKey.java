package com.mybank.balance.transaction.cache;

import com.mybank.balance.transaction.common.Constants;

/**
 * @author zhangdaochuan
 * @time 2025/2/17 16:46
 */
public class CacheKey {
    public static String getAccountKey(String key){
        return Constants.ACCOUUNT_CACHE +key;
    }

    public static String getTxnKey(String key){
        return Constants.TRANSACTION_CACHE +key;
    }
}
