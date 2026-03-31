-- =============================================
-- 同城供需任务匹配平台 - 数据库初始化脚本
-- 一期核心模块：用户权限、发布审核、订单流程、聊天室
-- =============================================

-- 与 application.yml 中的数据源保持一致
USE `graduation_project`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 首次初始化时直接执行即可
-- 如数据库中已存在表结构，本脚本会跳过建表并补齐基础数据
-- =============================================

-- =============================================
-- 用户与权限
-- =============================================

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`),
    KEY `idx_sys_user_phone` (`phone`),
    KEY `idx_sys_user_email` (`email`),
    KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_role_user_role` (`user_id`, `role_id`),
    KEY `idx_sys_user_role_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =============================================
-- 发布、审核、订单
-- =============================================

CREATE TABLE IF NOT EXISTS `trade_post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_no` VARCHAR(32) NOT NULL COMMENT '发布单号',
    `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
    `post_type` TINYINT NOT NULL COMMENT '发布类型：1-需求，2-供给',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '详情描述',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '预估价格',
    `city_name` VARCHAR(50) DEFAULT NULL COMMENT '城市名称',
    `area_name` VARCHAR(50) DEFAULT NULL COMMENT '区域名称',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-待审核，2-审核不通过，3-上架中，4-已下架',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `review_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    `publish_time` DATETIME DEFAULT NULL COMMENT '上架时间',
    `off_shelf_time` DATETIME DEFAULT NULL COMMENT '下架时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_post_no` (`post_no`),
    KEY `idx_trade_post_publisher_status` (`publisher_id`, `status`),
    KEY `idx_trade_post_status_create_time` (`status`, `create_time`),
    KEY `idx_trade_post_city_area` (`city_name`, `area_name`),
    KEY `idx_trade_post_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布主表';

CREATE TABLE IF NOT EXISTS `trade_post_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT NOT NULL COMMENT '发布ID',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
    `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_trade_post_image_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布图片表';

CREATE TABLE IF NOT EXISTS `trade_post_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT NOT NULL COMMENT '发布ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '审核人ID',
    `review_result` TINYINT NOT NULL COMMENT '审核结果：1-通过，2-不通过',
    `review_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_trade_post_review_post_id` (`post_id`),
    KEY `idx_trade_post_review_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布审核记录表';

CREATE TABLE IF NOT EXISTS `trade_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `post_id` BIGINT NOT NULL COMMENT '来源发布ID',
    `publisher_id` BIGINT NOT NULL COMMENT '发布方用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接单方用户ID',
    `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '成交金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待确认，1-进行中，2-已完成，3-已取消',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `confirm_time` DATETIME DEFAULT NULL COMMENT '确认时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_order_no` (`order_no`),
    KEY `idx_trade_order_post_id` (`post_id`),
    KEY `idx_trade_order_publisher_id` (`publisher_id`),
    KEY `idx_trade_order_receiver_id` (`receiver_id`),
    KEY `idx_trade_order_status_create_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =============================================
-- 聊天室
-- =============================================

CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_type` TINYINT NOT NULL DEFAULT 1 COMMENT '会话类型：1-普通私聊，2-订单会话',
    `post_id` BIGINT DEFAULT NULL COMMENT '关联发布ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `last_message_id` BIGINT DEFAULT NULL COMMENT '最后一条消息ID',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-关闭，1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat_session_order_id` (`order_id`),
    KEY `idx_chat_session_post_id` (`post_id`),
    KEY `idx_chat_session_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

CREATE TABLE IF NOT EXISTS `chat_session_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `last_read_message_id` BIGINT DEFAULT NULL COMMENT '最后已读消息ID',
    `last_read_time` DATETIME DEFAULT NULL COMMENT '最后已读时间',
    `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_chat_session_user_session_user` (`session_id`, `user_id`),
    KEY `idx_chat_session_user_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话成员表';

CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送人ID',
    `message_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本，2-图片，3-系统消息',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `extra_json` JSON DEFAULT NULL COMMENT '扩展信息JSON',
    `is_recall` TINYINT NOT NULL DEFAULT 0 COMMENT '是否撤回：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_chat_message_session_time` (`session_id`, `create_time`),
    KEY `idx_chat_message_sender_id` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- =============================================
-- 初始化基础数据
-- =============================================

INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `status`, `remark`)
VALUES
    (1, '管理员', 'ADMIN', 1, '平台管理员，负责审核与后台管理'),
    (2, '普通用户', 'USER', 1, '平台普通用户，可发布、聊天、接单')
ON DUPLICATE KEY UPDATE
    `role_name` = VALUES(`role_name`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`);

INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `status`)
VALUES
    (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lqkkO9QS3TzCjH3rS', '管理员', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE
    `password` = VALUES(`password`),
    `nickname` = VALUES(`nickname`),
    `email` = VALUES(`email`),
    `status` = VALUES(`status`);

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
VALUES
    (1, 1);

SET FOREIGN_KEY_CHECKS = 1;
