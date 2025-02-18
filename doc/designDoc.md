# 系统设计文档

## 1. 概述

本系统主要实现账户管理及交易处理。账户管理功能支持创建账户、获取账户信息等。交易功能支持处理交易、查询交易状态等。系统使用单体架构，部署在K8S环境中，数据库采用MySQL，缓存使用Redis，分布式锁由Redis提供支持。

## 2. 技术架构

### 2.1 架构图

```plaintext
+-------------------------------------------+
|            Kubernetes Cluster             |
|                                           |
|                  HPA                      |
|  +-------------+      +-----------+       | 
|  |             |      |           |       |  
|  |  Web App    |      |  Web App  |       |  
|  | (webflux)   |      | (webflux) |       |
|  |             |      |           |       |   
|  +-------------+      +-----------+       |  
|          | \          /     |             |
+----------+------------------+-------------+
           |    \    /        |           
           |      \ /         |           
           v      / \         v           
  +---------------+    +----------------+       
  |   MySQL 5.7   |    |   Redis 6.2    |       
  |               |    |                |       
  +---------------+    +----------------+       


```
###  2.2 技术栈
- 后端框架：Spring Boot 3.4, WebFlux
- 数据库：MySQL 5.7.x
- 缓存：Redis 6.2.x
- 容器化部署：Kubernetes (K8S) + HPA
- 分布式锁：Redis
##  3 核心业务逻辑
### 3.1 交易处理逻辑
#### 1.交易处理接口

- 幂等性保障：基于 bizId 进行幂等性校验，防止重复提交。
- 乐观锁机制：基于 version 字段实现乐观锁，确保并发操作的正确性。
#### 2.交易流程

- 获取分布式锁：以 bizId 为标识获取 Redis 分布式锁。
- 校验：
  - 确保 sourceAccount 和 targetAccount 不同。
  - 确认源账户余额是否足够。
  - 检查目标账户是否存在，且货币类型和源账户一致。
  - 校验交易请求的货币类型是否与源账户一致。
- 更新数据库：
    - 在校验通过后，开启事务，更新源账户和目标账户余额，更新交易记录状态为 PROCESSING。
  - 乐观锁机制：在更新账户时，需要进行重试机制（单次重试最多 3 次），每次重试间隔为 50ms * (n+1)。
  - 重试次数超过 3 次，则标记交易为 RETRY，如果成功，则标记为 SUCCESS。
- 释放分布式锁并返回交易状态。
#### 3.异步重试任务

每 10秒 + 随机 3 秒查询所有 RETRY 或状态为 PENDING, PROCESSING 且更新时间大于 10秒的交易记录，重新处理交易。
任务需要加分布锁，每个交易事务处理也要加 分布式锁，防止重复处理
一个任务最大次数>9 次则置为FAIL ，注明原因，打印ERROR 日志，（短信邮件通知人工处理）

### 3.2 数据缓存
- 缓存策略：
    - 使用 Redis 缓存账户信息和交易记录，TTL 为 30分钟
    - 当账户余额或交易记录状态更新时，删除对应的缓存。
### 3.3 异常处理
- 捕获 Redis 锁失败、数据库操作失败等异常，统一封装成 BizException，记录日志并返回具体错误信息。
### 4 表结构设计

```sql
CREATE TABLE `accounts` (
      `account_id` bigint(36) NOT NULL AUTO_INCREMENT COMMENT '账户ID',
      `account_type` int(11) NOT NULL DEFAULT '1' COMMENT '账户类型:1:借记卡',
      `balance` decimal(18,2) NOT NULL COMMENT '账户余额',
      `currency` char(3) COLLATE utf8mb4_bin NOT NULL COMMENT '货币',
      `version` int(11) NOT NULL DEFAULT '0' COMMENT '修改版本',
      `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
      `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '修改时间',
      PRIMARY KEY (`account_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10000000001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `transactions` (
      `transaction_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '事务ID',
      `biz_id` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '业务ID',
      `source_account` bigint(36) NOT NULL COMMENT '源账户ID',
      `target_account` bigint(36) NOT NULL COMMENT '目前账户ID',
      `amount` decimal(18,2) NOT NULL COMMENT '转账金额',
      `status` enum('PENDING','PROCESSING','RETRY','FAILED','SUCCESS') COLLATE utf8mb4_bin NOT NULL COMMENT '交易状态',
      `retry` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
      `error` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '错误信息',
      `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
      `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
      `currency` char(3) COLLATE utf8mb4_bin NOT NULL DEFAULT 'CNY' COMMENT '货币',
      PRIMARY KEY (`transaction_id`),
      UNIQUE KEY `biz_id` (`biz_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

### 5 接口设计
详见 API.md
