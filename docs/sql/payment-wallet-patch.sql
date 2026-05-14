USE `graduation_project`;

SET NAMES utf8mb4;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'pay_status') = 0,
    "ALTER TABLE `trade_order` ADD COLUMN `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-已退款，3-已结算' AFTER `status`",
    "SELECT 'skip add pay_status'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'pay_gateway') = 0,
    "ALTER TABLE `trade_order` ADD COLUMN `pay_gateway` VARCHAR(32) DEFAULT NULL COMMENT '支付网关：mock/wechat' AFTER `pay_status`",
    "SELECT 'skip add pay_gateway'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'pay_no') = 0,
    "ALTER TABLE `trade_order` ADD COLUMN `pay_no` VARCHAR(64) DEFAULT NULL COMMENT '支付流水号' AFTER `pay_gateway`",
    "SELECT 'skip add pay_no'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'pay_time') = 0,
    "ALTER TABLE `trade_order` ADD COLUMN `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间' AFTER `pay_no`",
    "SELECT 'skip add pay_time'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND COLUMN_NAME = 'refund_time') = 0,
    "ALTER TABLE `trade_order` ADD COLUMN `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间' AFTER `pay_time`",
    "SELECT 'skip add refund_time'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trade_order' AND INDEX_NAME = 'idx_trade_order_pay_status') = 0,
    "ALTER TABLE `trade_order` ADD KEY `idx_trade_order_pay_status` (`pay_status`)",
    "SELECT 'skip add idx_trade_order_pay_status'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `trade_order`
SET `pay_status` = CASE
    WHEN `status` = 2 THEN 3
    WHEN `status` = 3 THEN 2
    ELSE 0
END
WHERE `pay_status` IS NULL OR `pay_status` NOT IN (0, 1, 2, 3);

SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user_profile' AND COLUMN_NAME = 'wallet_balance') = 0,
    "ALTER TABLE `sys_user_profile` ADD COLUMN `wallet_balance` DECIMAL(12, 2) NOT NULL DEFAULT 100000.00 COMMENT '钱包余额' AFTER `bio`",
    "SELECT 'skip add wallet_balance'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `sys_user_profile`
SET `wallet_balance` = 100000.00
WHERE `wallet_balance` IS NULL;
