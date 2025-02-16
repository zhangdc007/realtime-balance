
SET NAMES utf8mb4;
-- ----------------------------
-- Table structure for accounts
-- ----------------------------
DROP TABLE IF EXISTS `accounts`;
CREATE TABLE `accounts` (
  `account_id` bigint(36) NOT NULL COMMENT '账户ID',
  `account_type` int(11) NOT NULL DEFAULT '1' COMMENT '账户类型:1:借记卡 ',
  `balance` decimal(18,2) NOT NULL COMMENT '账户余额',
  `currency` char(3) COLLATE utf8mb4_bin NOT NULL COMMENT '货币',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '修改版本',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '修改时间',
  PRIMARY KEY (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for transactions
-- ----------------------------
DROP TABLE IF EXISTS `transactions`;
CREATE TABLE `transactions` (
  `transaction_id` bigint(20) NOT NULL COMMENT '事务ID',
  `biz_id` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '业务ID',
  `source_account` bigint(36) NOT NULL COMMENT '源账户ID',
  `target_account` bigint(36) NOT NULL COMMENT '目前账户ID',
  `amount` decimal(18,2) NOT NULL COMMENT '转账金额',
  `status` enum('PENDING','PROCESSING','COMPLETED','RETRY','FAILED') COLLATE utf8mb4_bin NOT NULL COMMENT '交易状态',
  `error` varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT '错误信息',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`transaction_id`),
  UNIQUE KEY `biz_id` (`biz_id`,`source_account`,`target_account`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;