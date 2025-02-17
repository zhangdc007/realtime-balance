package com.mybank.balance.transaction.common;

/**
 * @author zhangdaochuan
 * @time 2025/2/16 21:50
 */
public class Constants {
    //返回码
    public static final String SUCCESS = "success";
    public static final Integer CODE_SUCCESS = 0;
    /** 默认日期时间格式 */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /** 默认日期格式 */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    /** 默认时间格式 */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    //一个事务最大重试次数
    public static final int MAX_TX_RETRY = 6;
    //单个循序最大重试次数
    public static final int SINGLE_MAX_TX_RETRY = 3;
}
