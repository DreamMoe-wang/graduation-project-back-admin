package com.example.admin.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 百度地图配置注册
 */
@Configuration
@EnableConfigurationProperties(BaiduMapProperties.class)
public class BaiduMapConfig {
}
