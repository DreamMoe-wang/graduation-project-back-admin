package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统设置实体
 */
@Data
public class SystemSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String platformName;

    private String supportEmail;

    private String servicePhone;

    private Integer allowRegister;

    private Integer maintenanceMode;

    private String themeColor;

    private String themeMode;

    private String fontSize;

    private String language;

    private String version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
