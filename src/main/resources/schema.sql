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

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID，0 表示根节点',
    `menu_name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `menu_type` TINYINT NOT NULL COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
    `route_name` VARCHAR(100) DEFAULT NULL COMMENT '路由名称',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `permission_code` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sys_menu_parent_id` (`parent_id`),
    UNIQUE KEY `uk_sys_menu_path` (`path`),
    UNIQUE KEY `uk_sys_menu_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_menu_role_menu` (`role_id`, `menu_id`),
    KEY `idx_sys_role_menu_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

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



CREATE TABLE IF NOT EXISTS `trade_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `category_name` VARCHAR(100) NOT NULL COMMENT 'Category name',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled,1-enabled',
    `requires_qualification` TINYINT NOT NULL DEFAULT 0 COMMENT '0-no,1-yes',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Trade category';

CREATE TABLE IF NOT EXISTS `trade_post_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `post_id` BIGINT NOT NULL COMMENT 'Trade post id',
    `category_id` BIGINT NOT NULL COMMENT 'Trade category id',
    `sort_no` INT NOT NULL DEFAULT 0 COMMENT 'Sort no',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    KEY `idx_trade_post_category_post_id` (`post_id`),
    KEY `idx_trade_post_category_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Trade post category relation';

INSERT INTO `trade_category` (`id`, `category_name`, `status`, `requires_qualification`)
VALUES
    (3001, '??', 1, 1),
    (3002, '??', 1, 1),
    (3003, '??', 1, 1),
    (3004, 'PPT??', 1, 0),
    (3005, '??', 1, 0),
    (3006, '??', 1, 0)
ON DUPLICATE KEY UPDATE
    `category_name` = VALUES(`category_name`),
    `status` = VALUES(`status`),
    `requires_qualification` = VALUES(`requires_qualification`);

CREATE TABLE IF NOT EXISTS `trade_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `post_id` BIGINT NOT NULL COMMENT '来源发布ID',
    `publisher_id` BIGINT NOT NULL COMMENT '发布方用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接单方用户ID',
    `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '成交金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待确认，1-进行中，2-已完成，3-已取消',
    `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-已退款，3-已结算',
    `pay_gateway` VARCHAR(32) DEFAULT NULL COMMENT '支付网关：mock/wechat',
    `pay_no` VARCHAR(64) DEFAULT NULL COMMENT '支付流水号',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `confirm_time` DATETIME DEFAULT NULL COMMENT '确认时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trade_order_no` (`order_no`),
    KEY `idx_trade_order_pay_status` (`pay_status`),
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

-- =============================================
-- 通用模块与系统设置
-- =============================================

CREATE TABLE IF NOT EXISTS `sys_module_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `module_name` VARCHAR(50) NOT NULL COMMENT '模块名称',
    `name` VARCHAR(100) NOT NULL COMMENT '名称',
    `code` VARCHAR(100) NOT NULL COMMENT '编码',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sys_module_item_module_name` (`module_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用模块数据表';

CREATE TABLE IF NOT EXISTS `sys_system_setting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform_name` VARCHAR(100) NOT NULL COMMENT '平台名称',
    `support_email` VARCHAR(100) DEFAULT NULL COMMENT '支持邮箱',
    `service_phone` VARCHAR(50) DEFAULT NULL COMMENT '客服电话',
    `allow_register` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许注册：0-否，1-是',
    `maintenance_mode` TINYINT NOT NULL DEFAULT 0 COMMENT '维护模式：0-关闭，1-开启',
    `theme_color` VARCHAR(20) NOT NULL DEFAULT '#5B66F3' COMMENT '主题色',
    `theme_mode` VARCHAR(20) NOT NULL DEFAULT 'light' COMMENT '主题模式：light-明亮，dark-暗黑',
    `font_size` VARCHAR(20) NOT NULL DEFAULT 'medium' COMMENT '字体大小：small-小，medium-中，large-大',
    `language` VARCHAR(20) NOT NULL DEFAULT 'zh-CN' COMMENT '系统语言：zh-CN/en-US',
    `version` VARCHAR(50) DEFAULT NULL COMMENT '版本号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

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
    (1, 'admin', '$2a$10$w1wcaJmdAapRPF.I/GhNdeUtbrdEZNfmuAdcRei7ETpOXbS56W9oq', '管理员', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE
    `password` = VALUES(`password`),
    `nickname` = VALUES(`nickname`),
    `email` = VALUES(`email`),
    `status` = VALUES(`status`),
    `requires_qualification` = VALUES(`requires_qualification`);

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
VALUES
    (1, 1);

INSERT INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `route_name`, `component`, `icon`, `permission_code`, `sort_no`, `visible`, `status`, `remark`)
VALUES
    (1001, 0, '首页', 2, '/', 'Dashboard', 'HomeView', 'HomeFilled', 'dashboard:view', 1, 1, 1, '首页菜单'),
    (1002, 0, '交易集市', 1, '/trade', 'TradeMarket', NULL, 'ShoppingCart', 'trade:view', 2, 1, 1, '交易集市目录'),
    (1003, 1002, '交易发布', 2, '/trade/publish', 'TradePublish', 'trade/TradePublish', 'EditPen', 'trade:publish:view', 1, 1, 1, '交易发布页面'),
    (1004, 1002, '交易大全', 2, '/trade/list', 'TradeList', 'trade/TradeList', 'List', 'trade:list:view', 2, 1, 1, '交易大全页面'),
    (1005, 1002, '订单大全', 2, '/trade/order', 'TradeOrder', 'trade/TradeOrder', 'Document', 'trade:order:view', 3, 1, 1, '订单大全页面'),
    (1006, 0, '聊天室', 2, '/chat', 'ChatRoom', 'chat/ChatRoom', 'ChatDotRound', 'chat:view', 3, 1, 1, '聊天室页面'),
    (1007, 0, '用户管理', 2, '/user', 'UserManage', 'user/UserManage', 'User', 'user:manage', 4, 1, 1, '用户管理页面'),
    (1008, 0, '角色管理', 2, '/role', 'RoleManage', 'role/RoleManage', 'Avatar', 'role:manage', 5, 1, 1, '角色管理页面'),
    (1009, 0, '菜单管理', 2, '/menu', 'MenuManage', 'menu/MenuManage', 'Menu', 'menu:view', 6, 1, 1, '菜单管理页面'),
    (1011, 0, '通知公告', 2, '/notice', 'NoticeManage', 'notice/NoticeManage', 'Bell', 'notice:manage', 8, 1, 1, '通知公告页面'),
    (1012, 0, '日志管理', 2, '/log', 'LogManage', 'log/LogManage', 'Notebook', 'log:manage', 9, 1, 1, '日志管理页面'),
    (1013, 0, '系统设置', 2, '/setting', 'SystemSetting', 'setting/SystemSetting', 'Setting', 'setting:manage', 10, 1, 1, '系统设置页面'),
    (1030, 0, '个人中心', 2, '/profile', 'ProfileCenter', 'profile/ProfileCenter', 'UserFilled', 'profile:view', 11, 1, 1, '当前用户个人信息中心'),
    (1014, 1002, '交易审核', 3, NULL, NULL, NULL, NULL, 'trade:review', 99, 0, 1, '交易审核按钮权限'),
    (1031, 1009, '新增菜单', 3, NULL, NULL, NULL, NULL, 'menu:create', 1, 0, 1, '菜单管理-新增按钮'),
    (1032, 1009, '编辑菜单', 3, NULL, NULL, NULL, NULL, 'menu:edit', 2, 0, 1, '菜单管理-编辑按钮'),
    (1033, 1009, '删除菜单', 3, NULL, NULL, NULL, NULL, 'menu:delete', 3, 0, 1, '菜单管理-删除按钮')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `menu_type` = VALUES(`menu_type`),
    `path` = VALUES(`path`),
    `route_name` = VALUES(`route_name`),
    `component` = VALUES(`component`),
    `icon` = VALUES(`icon`),
    `permission_code` = VALUES(`permission_code`),
    `sort_no` = VALUES(`sort_no`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES
    (1, 1001), (1, 1002), (1, 1003), (1, 1004), (1, 1005), (1, 1006),
    (1, 1007), (1, 1008), (1, 1009), (1, 1011), (1, 1012), (1, 1013), (1, 1014), (1, 1030),
    (1, 1031), (1, 1032), (1, 1033),
    (2, 1001), (2, 1002), (2, 1003), (2, 1004), (2, 1005), (2, 1006), (2, 1030);

INSERT INTO `sys_system_setting`
(`id`, `platform_name`, `support_email`, `service_phone`, `allow_register`, `maintenance_mode`, `theme_color`, `theme_mode`, `font_size`, `language`, `version`)
VALUES
    (1, '毕业设计后台管理系统', 'support@example.com', '400-800-1234', 1, 0, '#5B66F3', 'light', 'medium', 'zh-CN', '0.1.0')
ON DUPLICATE KEY UPDATE
    `platform_name` = VALUES(`platform_name`),
    `support_email` = VALUES(`support_email`),
    `service_phone` = VALUES(`service_phone`),
    `allow_register` = VALUES(`allow_register`),
    `maintenance_mode` = VALUES(`maintenance_mode`),
    `theme_color` = VALUES(`theme_color`),
    `theme_mode` = VALUES(`theme_mode`),
    `font_size` = VALUES(`font_size`),
    `language` = VALUES(`language`),
    `version` = VALUES(`version`);


CREATE TABLE IF NOT EXISTS `sys_user_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `city_name` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `area_name` VARCHAR(50) DEFAULT NULL COMMENT '区域',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `wallet_balance` DECIMAL(12, 2) NOT NULL DEFAULT 100000.00 COMMENT '钱包余额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_profile_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户扩展资料表';

INSERT INTO `sys_user_profile` (`id`, `user_id`, `real_name`, `gender`, `city_name`, `area_name`, `address`, `bio`, `wallet_balance`)
VALUES
    (1, 1, '系统管理员', 1, '北京', '海淀区', '中关村软件园', '负责平台审核与管理', 100000.00)
ON DUPLICATE KEY UPDATE
    `real_name` = VALUES(`real_name`),
    `gender` = VALUES(`gender`),
    `city_name` = VALUES(`city_name`),
    `area_name` = VALUES(`area_name`),
    `address` = VALUES(`address`),
    `bio` = VALUES(`bio`),
    `wallet_balance` = VALUES(`wallet_balance`);
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 基本信息认证
-- =============================================

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `user_qualification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `applicant_name` VARCHAR(50) NOT NULL COMMENT '申请人',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `id_card_no` VARCHAR(18) NOT NULL COMMENT '身份证号',
    `qualification_type` VARCHAR(100) NOT NULL COMMENT '资格类型',
    `qualification_no` VARCHAR(100) NOT NULL COMMENT '资格编号',
    `qualification_org` VARCHAR(100) NOT NULL COMMENT '发证机构',
    `city_name` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `area_name` VARCHAR(50) DEFAULT NULL COMMENT '区域',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
    `id_card_front_url` VARCHAR(255) NOT NULL COMMENT '身份证正面',
    `id_card_back_url` VARCHAR(255) NOT NULL COMMENT '身份证反面',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '补充说明',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-审核中，2-已通过，3-未通过',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `review_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核说明',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_qualification_status` (`status`),
    KEY `idx_user_qualification_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基本信息认证表';

CREATE TABLE IF NOT EXISTS `user_qualification_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `qualification_id` BIGINT NOT NULL COMMENT '认证ID',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
    `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_qualification_image_qualification_id` (`qualification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证资格证明图片表';

CREATE TABLE IF NOT EXISTS `user_qualification_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `qualification_id` BIGINT NOT NULL COMMENT '认证ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '审核人ID',
    `review_result` TINYINT NOT NULL COMMENT '审核结果：1-通过，2-驳回',
    `review_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_qualification_review_qualification_id` (`qualification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认证审核记录表';

INSERT INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `route_name`, `component`, `icon`, `permission_code`, `sort_no`, `visible`, `status`, `remark`)
VALUES
    (1040, 0, CONVERT(0xE59FBAE69CACE4BFA1E681AFE8AEA4E8AF81 USING utf8mb4), 2, '/qualification', 'QualificationManage', 'qualification/QualificationManage', 'Medal', 'qualification:view', 2, 1, 1, 'Qualification page'),
    (1041, 1040, CONVERT(0xE58F91E8B5B7E8AEA4E8AF81E68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:create', 1, 0, 1, 'Create qualification'),
    (1042, 1040, CONVERT(0xE4BFAEE694B9E8AEA4E8AF81E68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:edit', 2, 0, 1, 'Edit qualification'),
    (1043, 1040, CONVERT(0xE4BF9DE5AD98E88D89E7A8BFE68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:save', 3, 0, 1, 'Save qualification draft'),
    (1044, 1040, CONVERT(0xE68F90E4BAA4E5AEA1E6A0B8E68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:submit', 4, 0, 1, 'Submit qualification'),
    (1045, 1040, CONVERT(0xE8AEA4E8AF81E5AEA1E6A0B8E68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:review', 5, 0, 1, 'Review qualification')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `menu_type` = VALUES(`menu_type`),
    `path` = VALUES(`path`),
    `route_name` = VALUES(`route_name`),
    `component` = VALUES(`component`),
    `icon` = VALUES(`icon`),
    `permission_code` = VALUES(`permission_code`),
    `sort_no` = VALUES(`sort_no`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES
    (1, 1040), (1, 1041), (1, 1042), (1, 1043), (1, 1044), (1, 1045),
    (2, 1040), (2, 1041), (2, 1042), (2, 1043), (2, 1044);

SET FOREIGN_KEY_CHECKS = 1;


-- =============================================
-- Qualification
-- =============================================

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `user_qualification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` BIGINT NOT NULL COMMENT 'User id',
    `applicant_name` VARCHAR(50) NOT NULL COMMENT 'Applicant name',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT 'Contact phone',
    `real_name` VARCHAR(50) NOT NULL COMMENT 'Real name',
    `id_card_no` VARCHAR(18) NOT NULL COMMENT 'ID card no',
    `qualification_type` VARCHAR(100) NOT NULL COMMENT 'Qualification type',
    `qualification_no` VARCHAR(100) NOT NULL COMMENT 'Qualification no',
    `qualification_org` VARCHAR(100) NOT NULL COMMENT 'Issuing organization',
    `city_name` VARCHAR(50) DEFAULT NULL COMMENT 'City name',
    `area_name` VARCHAR(50) DEFAULT NULL COMMENT 'Area name',
    `address` VARCHAR(255) DEFAULT NULL COMMENT 'Address',
    `id_card_front_url` VARCHAR(255) NOT NULL COMMENT 'ID card front image',
    `id_card_back_url` VARCHAR(255) NOT NULL COMMENT 'ID card back image',
    `description` VARCHAR(500) DEFAULT NULL COMMENT 'Description',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-draft,1-auditing,2-approved,3-rejected',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT 'Reviewer id',
    `review_time` DATETIME DEFAULT NULL COMMENT 'Review time',
    `review_remark` VARCHAR(255) DEFAULT NULL COMMENT 'Review remark',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '0-active,1-deleted',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    KEY `idx_user_qualification_status` (`status`),
    KEY `idx_user_qualification_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User qualification';

CREATE TABLE IF NOT EXISTS `user_qualification_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `qualification_id` BIGINT NOT NULL COMMENT 'Qualification id',
    `image_url` VARCHAR(255) NOT NULL COMMENT 'Image url',
    `sort_no` INT NOT NULL DEFAULT 0 COMMENT 'Sort no',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    KEY `idx_user_qualification_image_qualification_id` (`qualification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Qualification proof image';

CREATE TABLE IF NOT EXISTS `user_qualification_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `qualification_id` BIGINT NOT NULL COMMENT 'Qualification id',
    `reviewer_id` BIGINT NOT NULL COMMENT 'Reviewer id',
    `review_result` TINYINT NOT NULL COMMENT '1-approved,2-rejected',
    `review_remark` VARCHAR(255) DEFAULT NULL COMMENT 'Review remark',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    KEY `idx_user_qualification_review_qualification_id` (`qualification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Qualification review record';

INSERT INTO `sys_menu`
(`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `route_name`, `component`, `icon`, `permission_code`, `sort_no`, `visible`, `status`, `remark`)
VALUES
    (1040, 0, CONVERT(0xE59FBAE69CACE4BFA1E681AFE8AEA4E8AF81 USING utf8mb4), 2, '/qualification', 'QualificationManage', 'qualification/QualificationManage', 'Medal', 'qualification:view', 2, 1, 1, 'Qualification page'),
    (1041, 1040, CONVERT(0xE58F91E8B5B7E8AEA4E8AF81E68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:create', 1, 0, 1, 'Create qualification'),
    (1042, 1040, CONVERT(0xE4BFAEE694B9E8AEA4E8AF81E68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:edit', 2, 0, 1, 'Edit qualification'),
    (1043, 1040, CONVERT(0xE4BF9DE5AD98E88D89E7A8BFE68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:save', 3, 0, 1, 'Save qualification draft'),
    (1044, 1040, CONVERT(0xE68F90E4BAA4E5AEA1E6A0B8E68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:submit', 4, 0, 1, 'Submit qualification'),
    (1045, 1040, CONVERT(0xE8AEA4E8AF81E5AEA1E6A0B8E68C89E992AE USING utf8mb4), 3, NULL, NULL, NULL, NULL, 'qualification:review', 5, 0, 1, 'Review qualification')
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `menu_type` = VALUES(`menu_type`),
    `path` = VALUES(`path`),
    `route_name` = VALUES(`route_name`),
    `component` = VALUES(`component`),
    `icon` = VALUES(`icon`),
    `permission_code` = VALUES(`permission_code`),
    `sort_no` = VALUES(`sort_no`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`),
    `remark` = VALUES(`remark`);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES
    (1, 1040), (1, 1041), (1, 1042), (1, 1043), (1, 1044), (1, 1045),
    (2, 1040), (2, 1041), (2, 1042), (2, 1043), (2, 1044);

SET FOREIGN_KEY_CHECKS = 1;
