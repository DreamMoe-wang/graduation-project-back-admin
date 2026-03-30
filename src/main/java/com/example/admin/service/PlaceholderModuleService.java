package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.vo.PlaceholderPageItemVO;
import com.example.admin.vo.SystemSettingVO;

import java.util.List;
import java.util.Map;

/**
 * 占位模块服务
 */
public interface PlaceholderModuleService {

    /**
     * 获取分页数据
     */
    PageResult<List<PlaceholderPageItemVO>> getPage(String moduleName, Integer pageNum, Integer pageSize);

    /**
     * 获取详情
     */
    PlaceholderPageItemVO getDetail(String moduleName, Long id);

    /**
     * 新增
     */
    boolean create(String moduleName, Map<String, Object> payload);

    /**
     * 更新
     */
    boolean update(String moduleName, Long id, Map<String, Object> payload);

    /**
     * 删除
     */
    boolean delete(String moduleName, Long id);

    /**
     * 清空
     */
    boolean clean(String moduleName);

    /**
     * 获取系统设置
     */
    SystemSettingVO getSystemSetting();

    /**
     * 更新系统设置
     */
    boolean updateSystemSetting(SystemSettingVO settingVO);
}
