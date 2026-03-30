package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.service.PlaceholderModuleService;
import com.example.admin.vo.PlaceholderPageItemVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 日志管理控制器
 */
@RestController
@RequestMapping("/log")
public class LogController {

    private static final String MODULE_NAME = "log";

    @Resource
    private PlaceholderModuleService placeholderModuleService;

    @GetMapping("/page")
    public Result<PageResult<List<PlaceholderPageItemVO>>> page(Integer pageNum, Integer pageSize) {
        return Result.success(placeholderModuleService.getPage(MODULE_NAME, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<PlaceholderPageItemVO> detail(@PathVariable Long id) {
        return Result.success(placeholderModuleService.getDetail(MODULE_NAME, id));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = placeholderModuleService.delete(MODULE_NAME, id);
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }

    @DeleteMapping("/clean")
    public Result<Boolean> clean() {
        boolean success = placeholderModuleService.clean(MODULE_NAME);
        return success ? Result.success("清空成功", true) : Result.error("清空失败");
    }
}
