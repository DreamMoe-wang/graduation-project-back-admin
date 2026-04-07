SET NAMES utf8mb4;

ALTER TABLE `sys_system_setting`
    ADD COLUMN `theme_color` VARCHAR(20) NOT NULL DEFAULT '#5B66F3' COMMENT '主题色' AFTER `maintenance_mode`,
    ADD COLUMN `theme_mode` VARCHAR(20) NOT NULL DEFAULT 'light' COMMENT '主题模式：light-明亮，dark-暗黑' AFTER `theme_color`,
    ADD COLUMN `font_size` VARCHAR(20) NOT NULL DEFAULT 'medium' COMMENT '字体大小：small-小，medium-中，large-大' AFTER `theme_mode`,
    ADD COLUMN `language` VARCHAR(20) NOT NULL DEFAULT 'zh-CN' COMMENT '系统语言：zh-CN/en-US' AFTER `font_size`;

UPDATE `sys_system_setting`
SET `theme_color` = COALESCE(NULLIF(`theme_color`, ''), '#5B66F3'),
    `theme_mode` = COALESCE(NULLIF(`theme_mode`, ''), 'light'),
    `font_size` = COALESCE(NULLIF(`font_size`, ''), 'medium'),
    `language` = COALESCE(NULLIF(`language`, ''), 'zh-CN');
