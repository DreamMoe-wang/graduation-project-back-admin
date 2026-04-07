SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
    `menu_name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `menu_path` VARCHAR(255) DEFAULT NULL COMMENT '菜单路径',
    `action_name` VARCHAR(100) NOT NULL COMMENT '操作名称',
    `request_method` VARCHAR(10) NOT NULL COMMENT '请求方法',
    `request_uri` VARCHAR(255) NOT NULL COMMENT '请求地址',
    `ip_address` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
    `operation_status` TINYINT NOT NULL DEFAULT 1 COMMENT '操作结果：0-失败，1-成功',
    `duration_ms` BIGINT NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
    `result_message` VARCHAR(255) DEFAULT NULL COMMENT '结果消息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_sys_operation_log_user_id` (`user_id`),
    KEY `idx_sys_operation_log_menu_name` (`menu_name`),
    KEY `idx_sys_operation_log_action_name` (`action_name`),
    KEY `idx_sys_operation_log_ip_address` (`ip_address`),
    KEY `idx_sys_operation_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
