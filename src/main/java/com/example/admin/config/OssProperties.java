package com.example.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云 OSS 配置属性
 */
@Data
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    /**
     * 是否启用 OSS
     */
    private boolean enabled = true;

    /**
     * 访问节点
     */
    private String endpoint;

    /**
     * Bucket 名称
     */
    private String bucketName;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 公开访问域名
     */
    private String publicDomain;

    /**
     * 默认上传目录
     */
    private String defaultDir = "uploads";

    /**
     * 单文件最大大小，单位 MB
     */
    private Integer maxSizeMb = 10;
}
