package com.example.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 百度地图配置
 */
@Data
@ConfigurationProperties(prefix = "baidu.map")
public class BaiduMapProperties {

    /**
     * 百度地图 Web 服务 AK
     */
    private String ak;

    /**
     * 百度地图 JS API AK
     */
    private String jsAk;

    /**
     * 百度地图接口超时时间，单位毫秒
     */
    private Integer timeoutMs = 5000;
}
