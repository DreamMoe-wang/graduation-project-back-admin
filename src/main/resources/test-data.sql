-- =============================================
-- 同城供需任务匹配平台 - 测试数据脚本
-- 可重复执行，用于本地联调、接口测试、前端展示验证
-- 默认测试密码统一为：admin123
-- =============================================

USE `graduation_project`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 测试用户
-- =============================================

INSERT INTO `sys_user`
(`id`, `username`, `password`, `nickname`, `avatar`, `phone`, `email`, `status`, `last_login_time`, `deleted`)
VALUES
    (11, 'alice', '$2a$10$w1wcaJmdAapRPF.I/GhNdeUtbrdEZNfmuAdcRei7ETpOXbS56W9oq', '爱丽丝', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/avatar/alice.png', '13800138011', 'alice@example.com', 1, NOW(), 0),
    (12, 'bob', '$2a$10$w1wcaJmdAapRPF.I/GhNdeUtbrdEZNfmuAdcRei7ETpOXbS56W9oq', '鲍勃', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/avatar/bob.png', '13800138012', 'bob@example.com', 1, NOW(), 0),
    (13, 'carol', '$2a$10$w1wcaJmdAapRPF.I/GhNdeUtbrdEZNfmuAdcRei7ETpOXbS56W9oq', '卡萝尔', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/avatar/carol.png', '13800138013', 'carol@example.com', 1, NOW(), 0),
    (14, 'david', '$2a$10$w1wcaJmdAapRPF.I/GhNdeUtbrdEZNfmuAdcRei7ETpOXbS56W9oq', '大卫', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/avatar/david.png', '13800138014', 'david@example.com', 1, NOW(), 0),
    (15, 'erin', '$2a$10$w1wcaJmdAapRPF.I/GhNdeUtbrdEZNfmuAdcRei7ETpOXbS56W9oq', '艾琳', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/avatar/erin.png', '13800138015', 'erin@example.com', 1, NOW(), 0)
ON DUPLICATE KEY UPDATE
    `password` = VALUES(`password`),
    `nickname` = VALUES(`nickname`),
    `avatar` = VALUES(`avatar`),
    `phone` = VALUES(`phone`),
    `email` = VALUES(`email`),
    `status` = VALUES(`status`),
    `deleted` = VALUES(`deleted`);

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
VALUES
    (11, 2),
    (12, 2),
    (13, 2),
    (14, 2),
    (15, 2);

-- =============================================
-- 占位模块测试数据
-- =============================================

INSERT INTO `sys_module_item`
(`id`, `module_name`, `name`, `code`, `status`, `description`, `create_time`, `update_time`)
VALUES
    (5201, 'notice', '系统升级通知', 'NOTICE_UPGRADE', 1, '本周末进行系统升级维护', '2026-03-22 14:00:00', '2026-03-22 14:00:00'),
    (5202, 'notice', '测试公告', 'NOTICE_TEST', 1, '用于前端联调展示的公告数据', '2026-03-22 14:15:00', '2026-03-22 14:15:00'),
    (5301, 'log', '用户登录日志', 'LOG_LOGIN', 1, '记录后台用户登录行为', '2026-03-23 08:00:00', '2026-03-23 08:00:00'),
    (5302, 'log', '交易审核日志', 'LOG_TRADE_REVIEW', 1, '记录交易审核动作', '2026-03-23 08:20:00', '2026-03-23 08:20:00')
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `code` = VALUES(`code`),
    `status` = VALUES(`status`),
    `description` = VALUES(`description`),
    `update_time` = VALUES(`update_time`);

INSERT INTO `sys_system_setting`
(`id`, `platform_name`, `support_email`, `service_phone`, `allow_register`, `maintenance_mode`, `theme_color`, `theme_mode`, `font_size`, `language`, `version`)
VALUES
    (1, '同城任务后台管理系统', 'support@example.com', '400-800-1234', 1, 0, '#5B66F3', 'light', 'medium', 'zh-CN', '0.1.0-test')
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

-- =============================================
-- 测试发布数据
-- 状态：0-草稿，1-待审核，2-审核不通过，3-可接取，4-交易中，5-交易结束
-- =============================================

INSERT INTO `trade_post`
(`id`, `post_no`, `publisher_id`, `post_type`, `title`, `content`, `price`, `city_name`, `area_name`, `address`,
 `contact_name`, `contact_phone`, `status`, `reviewer_id`, `review_time`, `review_remark`, `publish_time`, `off_shelf_time`, `deleted`, `create_time`, `update_time`)
VALUES
    (101, 'TP202603310001', 11, 1, '上门空调清洗', '需要清洗两台壁挂式空调，周末优先。', 150.00, '北京', '海淀区', '中关村软件园一期', '爱丽丝', '13800138011', 4, 1, '2026-03-25 09:10:00', '信息完整，审核通过', '2026-03-25 09:15:00', NULL, 0, '2026-03-25 08:30:00', '2026-03-25 09:15:00'),
    (102, 'TP202603310002', 12, 1, '宠物代遛服务', '工作日晚上需要遛狗 1 小时。', 80.00, '北京', '朝阳区', '望京 SOHO', '鲍勃', '13800138012', 0, NULL, NULL, NULL, NULL, NULL, 0, '2026-03-26 11:00:00', '2026-03-26 11:00:00'),
    (103, 'TP202603310003', 13, 2, '代取快递', '可提供校内快递代取代送服务。', 20.00, '北京', '昌平区', '沙河高教园', '卡萝尔', '13800138013', 1, NULL, NULL, NULL, NULL, NULL, 0, '2026-03-27 13:20:00', '2026-03-27 13:20:00'),
    (104, 'TP202603310004', 14, 1, '毕业设计答辩 PPT 优化', '需要美化答辩 PPT，风格简洁正式。', 200.00, '北京', '丰台区', '科技园区', '大卫', '13800138014', 2, 1, '2026-03-28 10:00:00', '描述过于简略，请补充需求细节', NULL, NULL, 0, '2026-03-28 09:10:00', '2026-03-28 10:00:00'),
    (105, 'TP202603310005', 12, 2, '上门电脑重装系统', '提供 Windows 系统重装和基础软件安装。', 120.00, '北京', '东城区', '东直门附近', '鲍勃', '13800138012', 5, 1, '2026-03-24 14:00:00', '审核通过', '2026-03-24 14:10:00', '2026-03-30 18:20:00', 0, '2026-03-24 13:30:00', '2026-03-30 18:20:00'),
    (106, 'TP202603310006', 15, 1, '家教辅导 - 初中数学', '每周三次，每次两小时，地点可协商。', 300.00, '北京', '西城区', '金融街附近', '艾琳', '13800138015', 4, 1, '2026-03-29 15:30:00', '审核通过', '2026-03-29 15:35:00', NULL, 0, '2026-03-29 15:00:00', '2026-03-29 15:35:00')
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `content` = VALUES(`content`),
    `price` = VALUES(`price`),
    `city_name` = VALUES(`city_name`),
    `area_name` = VALUES(`area_name`),
    `address` = VALUES(`address`),
    `contact_name` = VALUES(`contact_name`),
    `contact_phone` = VALUES(`contact_phone`),
    `status` = VALUES(`status`),
    `reviewer_id` = VALUES(`reviewer_id`),
    `review_time` = VALUES(`review_time`),
    `review_remark` = VALUES(`review_remark`),
    `publish_time` = VALUES(`publish_time`),
    `off_shelf_time` = VALUES(`off_shelf_time`),
    `deleted` = VALUES(`deleted`),
    `update_time` = VALUES(`update_time`);



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

INSERT INTO `trade_post_category`
(`id`, `post_id`, `category_id`, `sort_no`, `create_time`)
VALUES
    (3101, 101, 3001, 1, '2026-03-25 08:35:00'),
    (3102, 102, 3002, 1, '2026-03-26 11:05:00'),
    (3103, 103, 3003, 1, '2026-03-27 13:25:00'),
    (3104, 104, 3004, 1, '2026-03-28 09:12:00'),
    (3105, 105, 3005, 1, '2026-03-24 13:32:00'),
    (3106, 106, 3006, 1, '2026-03-29 15:02:00')
ON DUPLICATE KEY UPDATE
    `category_id` = VALUES(`category_id`),
    `sort_no` = VALUES(`sort_no`);

INSERT INTO `trade_post_image`
(`id`, `post_id`, `image_url`, `sort_no`, `create_time`)
VALUES
    (1001, 101, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/trade/air-conditioner-1.jpg', 1, '2026-03-25 08:35:00'),
    (1002, 101, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/trade/air-conditioner-2.jpg', 2, '2026-03-25 08:36:00'),
    (1003, 105, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/trade/computer-repair.jpg', 1, '2026-03-24 13:35:00'),
    (1004, 106, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/trade/math-tutor.jpg', 1, '2026-03-29 15:05:00')
ON DUPLICATE KEY UPDATE
    `image_url` = VALUES(`image_url`),
    `sort_no` = VALUES(`sort_no`);

INSERT INTO `trade_post_review`
(`id`, `post_id`, `reviewer_id`, `review_result`, `review_remark`, `create_time`)
VALUES
    (1101, 101, 1, 1, '信息完整，审核通过', '2026-03-25 09:10:00'),
    (1102, 104, 1, 2, '描述过于简略，请补充需求细节', '2026-03-28 10:00:00'),
    (1103, 105, 1, 1, '审核通过', '2026-03-24 14:00:00'),
    (1104, 106, 1, 1, '审核通过', '2026-03-29 15:30:00')
ON DUPLICATE KEY UPDATE
    `review_result` = VALUES(`review_result`),
    `review_remark` = VALUES(`review_remark`);

-- =============================================
-- 测试订单数据
-- 状态：0-待确认，1-进行中，2-已完成，3-已取消
-- =============================================

INSERT INTO `trade_order`
(`id`, `order_no`, `post_id`, `publisher_id`, `receiver_id`, `amount`, `status`, `pay_status`, `pay_gateway`, `pay_no`, `pay_time`, `refund_time`, `remark`,
 `confirm_time`, `finish_time`, `cancel_time`, `cancel_reason`, `create_time`, `update_time`)
VALUES
    (201, 'TO202603310001', 101, 11, 12, 150.00, 1, 1, 'mock', 'MOCKPAY-201', '2026-03-25 09:55:00', NULL, '已预约周末上门清洗', '2026-03-25 10:00:00', NULL, NULL, NULL, '2026-03-25 09:40:00', '2026-03-25 10:00:00'),
    (202, 'TO202603310002', 105, 12, 11, 120.00, 3, 3, 'mock', 'MOCKPAY-202', '2026-03-24 15:50:00', NULL, '系统重装已完成', '2026-03-24 16:00:00', '2026-03-30 18:00:00', NULL, NULL, '2026-03-24 15:20:00', '2026-03-30 18:00:00'),
    (203, 'TO202603310003', 106, 15, 13, 300.00, 0, 0, NULL, NULL, NULL, NULL, '等待接单人确认排课时间', NULL, NULL, NULL, NULL, '2026-03-30 09:00:00', '2026-03-30 09:00:00'),
    (204, 'TO202603310004', 101, 11, 14, 180.00, 4, 2, 'mock', 'MOCKPAY-204', '2026-03-26 11:20:00', '2026-03-26 12:31:00', '服务时间冲突，已取消', NULL, NULL, '2026-03-26 12:30:00', '双方时间冲突', '2026-03-26 11:00:00', '2026-03-26 12:30:00')
ON DUPLICATE KEY UPDATE
    `amount` = VALUES(`amount`),
    `status` = VALUES(`status`),
    `pay_status` = VALUES(`pay_status`),
    `pay_gateway` = VALUES(`pay_gateway`),
    `pay_no` = VALUES(`pay_no`),
    `pay_time` = VALUES(`pay_time`),
    `refund_time` = VALUES(`refund_time`),
    `remark` = VALUES(`remark`),
    `confirm_time` = VALUES(`confirm_time`),
    `finish_time` = VALUES(`finish_time`),
    `cancel_time` = VALUES(`cancel_time`),
    `cancel_reason` = VALUES(`cancel_reason`),
    `update_time` = VALUES(`update_time`);

-- =============================================
-- 测试聊天数据
-- =============================================

INSERT INTO `chat_session`
(`id`, `session_type`, `post_id`, `order_id`, `last_message_id`, `last_message_time`, `status`, `create_time`, `update_time`)
VALUES
    (301, 2, 101, 201, 4004, '2026-03-25 10:05:00', 1, '2026-03-25 09:45:00', '2026-03-25 10:05:00'),
    (302, 2, 105, 202, 4010, '2026-03-30 17:50:00', 1, '2026-03-24 15:25:00', '2026-03-30 17:50:00'),
    (303, 1, 106, NULL, 4014, '2026-03-30 09:12:00', 1, '2026-03-30 09:02:00', '2026-03-30 09:12:00')
ON DUPLICATE KEY UPDATE
    `post_id` = VALUES(`post_id`),
    `order_id` = VALUES(`order_id`),
    `last_message_id` = VALUES(`last_message_id`),
    `last_message_time` = VALUES(`last_message_time`),
    `status` = VALUES(`status`),
    `update_time` = VALUES(`update_time`);

INSERT INTO `chat_session_user`
(`id`, `session_id`, `user_id`, `last_read_message_id`, `last_read_time`, `unread_count`, `create_time`)
VALUES
    (3101, 301, 11, 4004, '2026-03-25 10:06:00', 0, '2026-03-25 09:45:00'),
    (3102, 301, 12, 4003, '2026-03-25 10:00:00', 1, '2026-03-25 09:45:00'),
    (3103, 302, 12, 4010, '2026-03-30 17:50:00', 0, '2026-03-24 15:25:00'),
    (3104, 302, 11, 4009, '2026-03-30 17:45:00', 1, '2026-03-24 15:25:00'),
    (3105, 303, 15, 4014, '2026-03-30 09:12:00', 0, '2026-03-30 09:02:00'),
    (3106, 303, 13, 4013, '2026-03-30 09:10:00', 1, '2026-03-30 09:02:00')
ON DUPLICATE KEY UPDATE
    `last_read_message_id` = VALUES(`last_read_message_id`),
    `last_read_time` = VALUES(`last_read_time`),
    `unread_count` = VALUES(`unread_count`);

INSERT INTO `chat_message`
(`id`, `session_id`, `sender_id`, `message_type`, `content`, `extra_json`, `is_recall`, `create_time`)
VALUES
    (4001, 301, 11, 1, '你好，周末上午可以上门吗？', NULL, 0, '2026-03-25 09:46:00'),
    (4002, 301, 12, 1, '可以的，周六上午十点没问题。', NULL, 0, '2026-03-25 09:48:00'),
    (4003, 301, 11, 1, '好的，那就约周六十点。', NULL, 0, '2026-03-25 09:55:00'),
    (4004, 301, 12, 1, '收到，我提前联系你。', NULL, 0, '2026-03-25 10:05:00'),
    (4005, 302, 12, 1, '电脑系统装好了，还帮你装了常用软件。', NULL, 0, '2026-03-30 17:20:00'),
    (4006, 302, 11, 1, '太好了，辛苦你了。', NULL, 0, '2026-03-30 17:22:00'),
    (4007, 302, 12, 1, '不客气，之后有问题可以再联系。', NULL, 0, '2026-03-30 17:25:00'),
    (4008, 302, 11, 3, '订单状态已更新为已完成', '{\"event\":\"ORDER_COMPLETED\"}', 0, '2026-03-30 17:40:00'),
    (4009, 302, 11, 1, '已确认完成，感谢。', NULL, 0, '2026-03-30 17:45:00'),
    (4010, 302, 12, 1, '欢迎下次再来。', NULL, 0, '2026-03-30 17:50:00'),
    (4011, 303, 13, 1, '你好，请问上课时间可以周末吗？', NULL, 0, '2026-03-30 09:03:00'),
    (4012, 303, 15, 1, '可以，周六下午比较方便。', NULL, 0, '2026-03-30 09:05:00'),
    (4013, 303, 13, 1, '好的，那我先下单再确认细节。', NULL, 0, '2026-03-30 09:10:00'),
    (4014, 303, 15, 1, '没问题，订单我看到后会尽快确认。', NULL, 0, '2026-03-30 09:12:00')
ON DUPLICATE KEY UPDATE
    `message_type` = VALUES(`message_type`),
    `content` = VALUES(`content`),
    `extra_json` = VALUES(`extra_json`),
    `is_recall` = VALUES(`is_recall`),
    `create_time` = VALUES(`create_time`);


INSERT INTO `sys_user_profile`
(`id`, `user_id`, `real_name`, `gender`, `birthday`, `city_name`, `area_name`, `address`, `bio`, `wallet_balance`)
VALUES
    (1, 11, 'Alice Wang', 2, '2000-03-18', '北京', '海淀区', '中关村软件园一期', '喜欢整理任务流程和做计划。', 100000.00),
    (2, 12, 'Bob Li', 1, '1999-11-05', '北京', '朝阳区', '望京 SOHO', '擅长数码维修和上门服务。', 100000.00),
    (3, 13, 'Carol Chen', 2, '2001-07-12', '北京', '昌平区', '沙河高教园', '校内跑腿和快递代取都很熟。', 100000.00),
    (4, 14, 'David Zhang', 1, '1998-09-22', '北京', '丰台区', '科技园区', '专注演示文稿与设计美化。', 100000.00),
    (5, 15, 'Erin Liu', 2, '2000-01-08', '北京', '西城区', '金融街附近', '有家教和学习辅导经验。', 100000.00)
ON DUPLICATE KEY UPDATE
    `real_name` = VALUES(`real_name`),
    `gender` = VALUES(`gender`),
    `birthday` = VALUES(`birthday`),
    `city_name` = VALUES(`city_name`),
    `area_name` = VALUES(`area_name`),
    `address` = VALUES(`address`),
    `bio` = VALUES(`bio`),
    `wallet_balance` = VALUES(`wallet_balance`);

-- =============================================
-- 资格认证测试数据
-- 状态：0-草稿，1-审核中，2-已通过，3-未通过
-- =============================================

INSERT INTO `user_qualification`
(`id`, `user_id`, `applicant_name`, `contact_phone`, `real_name`, `id_card_no`, `qualification_type`, `qualification_no`, `qualification_org`,
 `city_name`, `area_name`, `address`, `id_card_front_url`, `id_card_back_url`, `description`, `status`, `reviewer_id`, `review_time`, `review_remark`,
 `deleted`, `create_time`, `update_time`)
VALUES
    (2001, 11, '爱丽丝', '13800138011', 'Alice Wang', '110101200003180011', '家政清洗', 'Q-ALICE-001', '北京家政服务协会',
     '北京', '海淀区', '中关村软件园一期', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/alice-id-front.jpg',
     'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/alice-id-back.jpg', '具备上门家电清洗与保洁经验。', 2, 1,
     '2026-03-25 08:45:00', '身份与资格证明完整，审核通过', 0, '2026-03-25 08:00:00', '2026-03-25 08:45:00'),
    (2002, 12, '鲍勃', '13800138012', 'Bob Li', '110101199911050022', '电脑维修', 'Q-BOB-002', '北京电子维修职业中心',
     '北京', '朝阳区', '望京 SOHO', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/bob-id-front.jpg',
     'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/bob-id-back.jpg', '擅长系统重装与故障排查。', 1, NULL,
     NULL, NULL, 0, '2026-03-29 10:00:00', '2026-03-29 10:00:00'),
    (2003, 13, '卡罗尔', '13800138013', 'Carol Chen', '110101200107120033', '校园跑腿', 'Q-CAROL-003', '校园服务备案',
     '北京', '昌平区', '沙河高教园', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/carol-id-front.jpg',
     'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/carol-id-back.jpg', '可提供校内取件、代办等服务。', 3, 1,
     '2026-03-28 11:20:00', '资格证明不够清晰，请重新上传原件照片', 0, '2026-03-28 09:30:00', '2026-03-28 11:20:00'),
    (2004, 15, '艾琳', '13800138015', 'Erin Liu', '110101200001080044', '家教辅导', 'Q-ERIN-004', '教师资格认定中心',
     '北京', '西城区', '金融街附近', 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/erin-id-front.jpg',
     'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/erin-id-back.jpg', '家教经验丰富，已整理授课案例。', 0, NULL,
     NULL, NULL, 0, '2026-03-30 09:00:00', '2026-03-30 09:00:00')
ON DUPLICATE KEY UPDATE
    `applicant_name` = VALUES(`applicant_name`),
    `contact_phone` = VALUES(`contact_phone`),
    `real_name` = VALUES(`real_name`),
    `id_card_no` = VALUES(`id_card_no`),
    `qualification_type` = VALUES(`qualification_type`),
    `qualification_no` = VALUES(`qualification_no`),
    `qualification_org` = VALUES(`qualification_org`),
    `city_name` = VALUES(`city_name`),
    `area_name` = VALUES(`area_name`),
    `address` = VALUES(`address`),
    `id_card_front_url` = VALUES(`id_card_front_url`),
    `id_card_back_url` = VALUES(`id_card_back_url`),
    `description` = VALUES(`description`),
    `status` = VALUES(`status`),
    `reviewer_id` = VALUES(`reviewer_id`),
    `review_time` = VALUES(`review_time`),
    `review_remark` = VALUES(`review_remark`),
    `deleted` = VALUES(`deleted`),
    `update_time` = VALUES(`update_time`);

INSERT INTO `user_qualification_image`
(`id`, `qualification_id`, `image_url`, `sort_no`, `create_time`)
VALUES
    (2101, 2001, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/alice-proof-1.jpg', 1, '2026-03-25 08:10:00'),
    (2102, 2001, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/alice-proof-2.jpg', 2, '2026-03-25 08:11:00'),
    (2103, 2002, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/bob-proof-1.jpg', 1, '2026-03-29 10:05:00'),
    (2104, 2003, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/carol-proof-1.jpg', 1, '2026-03-28 09:35:00'),
    (2105, 2004, 'https://graduation-project-wang.oss-cn-beijing.aliyuncs.com/mock/qualification/erin-proof-1.jpg', 1, '2026-03-30 09:05:00')
ON DUPLICATE KEY UPDATE
    `image_url` = VALUES(`image_url`),
    `sort_no` = VALUES(`sort_no`);

INSERT INTO `user_qualification_review`
(`id`, `qualification_id`, `reviewer_id`, `review_result`, `review_remark`, `create_time`)
VALUES
    (2201, 2001, 1, 1, '身份与资格证明完整，审核通过', '2026-03-25 08:45:00'),
    (2202, 2003, 1, 2, '资格证明不够清晰，请重新上传原件照片', '2026-03-28 11:20:00')
ON DUPLICATE KEY UPDATE
    `review_result` = VALUES(`review_result`),
    `review_remark` = VALUES(`review_remark`);
SET FOREIGN_KEY_CHECKS = 1;
