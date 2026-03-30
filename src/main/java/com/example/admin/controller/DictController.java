package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.service.PlaceholderModuleService;
import com.example.admin.vo.PlaceholderPageItemVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 字典管理控制器
 */
@RestController
@RequestMapping("/dict")
public class DictController {

    private static final String MODULE_NAME = "dict";

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

    @PostMapping
    public Result<Boolean> create(@RequestBody(required = false) Map<String, Object> payload) {
        boolean success = placeholderModuleService.create(MODULE_NAME, payload);
        return success ? Result.success("新增成功", true) : Result.error("新增失败");
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> payload) {
        boolean success = placeholderModuleService.update(MODULE_NAME, id, payload);
        return success ? Result.success("更新成功", true) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = placeholderModuleService.delete(MODULE_NAME, id);
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }
}
