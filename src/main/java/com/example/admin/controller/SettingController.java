package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.service.PlaceholderModuleService;
import com.example.admin.vo.SystemSettingVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/setting")
public class SettingController {

    @Resource
    private PlaceholderModuleService placeholderModuleService;

    @GetMapping("/detail")
    public Result<SystemSettingVO> detail() {
        return Result.success(placeholderModuleService.getSystemSetting());
    }

    @PutMapping("/detail")
    public Result<Boolean> update(@RequestBody SystemSettingVO settingVO) {
        boolean success = placeholderModuleService.updateSystemSetting(settingVO);
        return success ? Result.success("保存成功", true) : Result.error("保存失败");
    }
}
