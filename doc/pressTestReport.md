# 压测报告
机器信息

Mac os m1 8核

内存16GB

JDK 21

jmeter 5.6.3

MySQL和redis 在本机 Docker部署

本地发压

一：转账交易

测试并发给相同的source target账户转账

![image](images/iH9_dav3yMY8f_-HtfA84ZBUKsFp8FKa8ZRVxhQYIB8.png)

交易流水号10000\~90000

![image](images/zSFmY4XsBUWBx_b0cD7a_1r1SywWuvXav53q5JmjOaU.png)



线程数稳定在25

服务进程峰值CPU 30%（机器8核）

处理交易接口为 195/s，从日志看，出现大量乐观锁失败，也有一次性成功的，耗时6ms

很多请求失败后，进入递增sleep 重试，导致523ms,影响了吞吐

GC 线程总数均正常

![image](images/mGOq3QUI-6DcVFnPyAFJCSp7P4WQ9RCfZPHFP9waYrQ.png)

查看数据库，压测期间竞争很激烈，重试次数有的到7，

![image](images/CTeR5YDO6iPfyyKr-qbV-a6kdlvEpheA7_uByt9zR-0.png)

![image](images/FeJVo-SmnMJq0qCc5gbCeT4FZJ89wLmiZrwu383HOJM.png)

source和target account 余额相加符合预期

![image](images/8Y3rlHR1UZsQZaT0ZFDbHkzDjD3nCMz-Zz1FQYNmGy8.png)

统计本次压测的10000个请求，成功有8210

![image](images/qHh_DqWsY3Ujpjq0zazKLVu2GM7lrUKdK0XoxNjkfQU.png)

失败原因均为重试次数过多导致，可以通过提高重试次数缓解

![image](images/AiaTY-MlV4zus6Od2AynGgDsqPtKaAfG_qIJADmdtSk.png)



二：查询交易状态和账户信息

![image](images/qLKnIVsUWfoACHi65Bf43kNtSwzMWKH2IZ2ZCRu0NAg.png)



![image](images/7ublZwO2EqDDFLSOZTyNKTek-nA22WK6VbSSeVSUtwE.png)

![image](images/bfIWv3GBv3fx280Fn7VNRWIDBjlRH1oLoYAScvSegRA.png)

线程数稳定在25

峰值CPU 32%，机器8核

2个接口均为 1700/s 合计 3400/s 

由于接口有redis缓存，所以基本是打到redis上，没有能压到上限

GC 线程总数均正常

![image](images/QMffron-iwL_5j20Bb5q2lF-BCpMmkEtEJNWFE0tQao.png)

