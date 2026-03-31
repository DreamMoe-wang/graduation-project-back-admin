package com.example.admin.mapper;

import com.example.admin.entity.SystemSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统设置 Mapper
 */
@Mapper
public interface SystemSettingMapper {

    SystemSetting selectCurrent();

    int insertSystemSetting(SystemSetting systemSetting);

    int updateSystemSetting(SystemSetting systemSetting);
}
