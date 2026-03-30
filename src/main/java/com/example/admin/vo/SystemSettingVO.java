package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统设置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingVO {

    private String platformName;

    private String supportEmail;

    private String servicePhone;

    private Boolean allowRegister;

    private Boolean maintenanceMode;

    private String version;
}
