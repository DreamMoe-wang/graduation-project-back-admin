package com.example.admin.service.impl;

import com.example.admin.common.PageResult;
import com.example.admin.service.PlaceholderModuleService;
import com.example.admin.vo.PlaceholderPageItemVO;
import com.example.admin.vo.SystemSettingVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 占位模块服务实现
 *
 * <p>这些模块对应的前端页面还在开发中，当前只预留控制器和稳定路径。</p>
 */
@Service
public class PlaceholderModuleServiceImpl implements PlaceholderModuleService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<List<PlaceholderPageItemVO>> getPage(String moduleName, Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        List<PlaceholderPageItemVO> records = currentPage == 1
                ? Collections.singletonList(buildItem(moduleName, 1L))
                : Collections.emptyList();
        return PageResult.of(1L, records);
    }

    @Override
    public PlaceholderPageItemVO getDetail(String moduleName, Long id) {
        return buildItem(moduleName, id);
    }

    @Override
    public boolean create(String moduleName, Map<String, Object> payload) {
        return true;
    }

    @Override
    public boolean update(String moduleName, Long id, Map<String, Object> payload) {
        return id != null && id > 0;
    }

    @Override
    public boolean delete(String moduleName, Long id) {
        return id != null && id > 0;
    }

    @Override
    public boolean clean(String moduleName) {
        return true;
    }

    @Override
    public SystemSettingVO getSystemSetting() {
        return SystemSettingVO.builder()
                .platformName("毕业设计后台管理系统")
                .supportEmail("support@example.com")
                .servicePhone("400-800-1234")
                .allowRegister(true)
                .maintenanceMode(false)
                .version("0.1.0")
                .build();
    }

    @Override
    public boolean updateSystemSetting(SystemSettingVO settingVO) {
        return true;
    }

    private PlaceholderPageItemVO buildItem(String moduleName, Long id) {
        String normalizedName = moduleName.toUpperCase(Locale.ROOT);
        return PlaceholderPageItemVO.builder()
                .id(id)
                .name(moduleName + "模块占位数据")
                .code(normalizedName + "_DEMO")
                .status(1)
                .description("当前仅预留控制器和 API 路径，待数据库表与 Mapper 完成后接入真实数据。")
                .updateTime(LocalDateTime.now().format(DATETIME_FORMATTER))
                .build();
    }
}
