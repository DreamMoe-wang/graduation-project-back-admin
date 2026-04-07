package com.example.admin.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.example.admin.common.PageResult;
import com.example.admin.entity.ModuleItem;
import com.example.admin.entity.SystemSetting;
import com.example.admin.mapper.ModuleItemMapper;
import com.example.admin.mapper.SystemSettingMapper;
import com.example.admin.service.PlaceholderModuleService;
import com.example.admin.vo.PlaceholderPageItemVO;
import com.example.admin.vo.SystemSettingVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 占位模块服务实现
 */
@Service
public class PlaceholderModuleServiceImpl implements PlaceholderModuleService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ModuleItemMapper moduleItemMapper;

    @Resource
    private SystemSettingMapper systemSettingMapper;

    @Override
    public PageResult<List<PlaceholderPageItemVO>> getPage(String moduleName, Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = moduleItemMapper.countPage(moduleName);
        List<PlaceholderPageItemVO> records = moduleItemMapper.selectPage(moduleName, (long) (currentPage - 1) * size, size)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(total, records);
    }

    @Override
    public PageResult<List<PlaceholderPageItemVO>> getPublishedPage(String moduleName, Integer pageNum, Integer pageSize) {
        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = moduleItemMapper.countPageByStatus(moduleName, 1);
        List<PlaceholderPageItemVO> records = moduleItemMapper.selectPageByStatus(moduleName, 1, (long) (currentPage - 1) * size, size)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(total, records);
    }

    @Override
    public PlaceholderPageItemVO getDetail(String moduleName, Long id) {
        ModuleItem item = moduleItemMapper.selectById(id, moduleName);
        if (item == null) {
            throw new RuntimeException("数据不存在");
        }
        return toVO(item);
    }

    @Override
    public PlaceholderPageItemVO getPublishedDetail(String moduleName, Long id) {
        ModuleItem item = moduleItemMapper.selectByIdAndStatus(id, moduleName, 1);
        if (item == null) {
            throw new RuntimeException("公告不存在或未发布");
        }
        return toVO(item);
    }

    @Override
    public boolean create(String moduleName, Map<String, Object> payload) {
        ModuleItem item = new ModuleItem();
        item.setModuleName(moduleName);
        item.setName(resolveString(payload, "name", moduleName + "数据"));
        item.setCode(resolveString(payload, "code", moduleName.toUpperCase() + "_" + System.currentTimeMillis()));
        item.setStatus(resolveInteger(payload, "status", 1));
        item.setDescription(resolveString(payload, "description", ""));
        return moduleItemMapper.insertModuleItem(item) > 0;
    }

    @Override
    public boolean update(String moduleName, Long id, Map<String, Object> payload) {
        ModuleItem item = moduleItemMapper.selectById(id, moduleName);
        if (item == null) {
            throw new RuntimeException("数据不存在");
        }
        item.setName(resolveString(payload, "name", item.getName()));
        item.setCode(resolveString(payload, "code", item.getCode()));
        item.setStatus(resolveInteger(payload, "status", item.getStatus()));
        item.setDescription(resolveString(payload, "description", item.getDescription()));
        return moduleItemMapper.updateModuleItem(item) > 0;
    }

    @Override
    public boolean delete(String moduleName, Long id) {
        return moduleItemMapper.deleteById(id, moduleName) > 0;
    }

    @Override
    public boolean clean(String moduleName) {
        return moduleItemMapper.cleanByModuleName(moduleName) >= 0;
    }

    @Override
    public SystemSettingVO getSystemSetting() {
        SystemSetting setting = systemSettingMapper.selectCurrent();
        if (setting == null) {
            setting = new SystemSetting();
            setting.setPlatformName("毕业设计后台管理系统");
            setting.setSupportEmail("support@example.com");
            setting.setServicePhone("400-800-1234");
            setting.setAllowRegister(1);
            setting.setMaintenanceMode(0);
            setting.setThemeColor("#5B66F3");
            setting.setThemeMode("light");
            setting.setFontSize("medium");
            setting.setLanguage("zh-CN");
            setting.setVersion("0.1.0");
            systemSettingMapper.insertSystemSetting(setting);
        }
        return toVO(setting);
    }

    @Override
    public boolean updateSystemSetting(SystemSettingVO settingVO) {
        SystemSetting current = systemSettingMapper.selectCurrent();
        if (current == null) {
            current = new SystemSetting();
            fillSetting(current, settingVO);
            return systemSettingMapper.insertSystemSetting(current) > 0;
        }
        fillSetting(current, settingVO);
        return systemSettingMapper.updateSystemSetting(current) > 0;
    }

    private PlaceholderPageItemVO toVO(ModuleItem item) {
        return PlaceholderPageItemVO.builder()
                .id(item.getId())
                .name(item.getName())
                .code(item.getCode())
                .status(item.getStatus())
                .description(item.getDescription())
                .updateTime(item.getUpdateTime() == null ? null : item.getUpdateTime().format(DATETIME_FORMATTER))
                .build();
    }

    private SystemSettingVO toVO(SystemSetting setting) {
        return SystemSettingVO.builder()
                .platformName(setting.getPlatformName())
                .supportEmail(setting.getSupportEmail())
                .servicePhone(setting.getServicePhone())
                .allowRegister(setting.getAllowRegister() != null && setting.getAllowRegister() == 1)
                .maintenanceMode(setting.getMaintenanceMode() != null && setting.getMaintenanceMode() == 1)
                .themeColor(setting.getThemeColor())
                .themeMode(setting.getThemeMode())
                .fontSize(setting.getFontSize())
                .language(setting.getLanguage())
                .version(setting.getVersion())
                .build();
    }

    private void fillSetting(SystemSetting target, SystemSettingVO source) {
        target.setPlatformName(source.getPlatformName());
        target.setSupportEmail(source.getSupportEmail());
        target.setServicePhone(source.getServicePhone());
        target.setAllowRegister(Boolean.TRUE.equals(source.getAllowRegister()) ? 1 : 0);
        target.setMaintenanceMode(Boolean.TRUE.equals(source.getMaintenanceMode()) ? 1 : 0);
        target.setThemeColor(StrUtil.blankToDefault(source.getThemeColor(), "#5B66F3"));
        target.setThemeMode(StrUtil.blankToDefault(source.getThemeMode(), "light"));
        target.setFontSize(StrUtil.blankToDefault(source.getFontSize(), "medium"));
        target.setLanguage(StrUtil.blankToDefault(source.getLanguage(), "zh-CN"));
        target.setVersion(source.getVersion());
    }

    private String resolveString(Map<String, Object> payload, String key, String defaultValue) {
        if (payload == null || !payload.containsKey(key)) {
            return defaultValue;
        }
        String value = Convert.toStr(payload.get(key));
        return StrUtil.isBlank(value) ? defaultValue : value;
    }

    private Integer resolveInteger(Map<String, Object> payload, String key, Integer defaultValue) {
        if (payload == null || !payload.containsKey(key)) {
            return defaultValue;
        }
        return Convert.toInt(payload.get(key), defaultValue);
    }
}
