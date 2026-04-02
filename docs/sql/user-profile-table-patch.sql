USE `graduation_project`;

SET NAMES utf8mb4;

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
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_profile_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户扩展资料表';

INSERT INTO `sys_user_profile`
(`id`, `user_id`, `real_name`, `gender`, `city_name`, `area_name`, `address`, `bio`)
VALUES
    (1, 1, '系统管理员', 1, '北京', '海淀区', '中关村软件园', '负责平台审核与管理')
ON DUPLICATE KEY UPDATE
    `real_name` = VALUES(`real_name`),
    `gender` = VALUES(`gender`),
    `city_name` = VALUES(`city_name`),
    `area_name` = VALUES(`area_name`),
    `address` = VALUES(`address`),
    `bio` = VALUES(`bio`);
