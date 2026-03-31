package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.RoleDTO;
import com.example.admin.entity.Role;
import com.example.admin.service.RoleService;
import org.springframework.validation.annotation.Validated;
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

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @GetMapping("/page")
    public Result<PageResult<List<Role>>> page(Integer pageNum, Integer pageSize) {
        return Result.success(roleService.page(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Role> detail(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody @Validated RoleDTO roleDTO) {
        boolean success = roleService.create(roleDTO);
        return success ? Result.success("新增成功", true) : Result.error("新增失败");
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Validated RoleDTO roleDTO) {
        boolean success = roleService.update(id, roleDTO);
        return success ? Result.success("更新成功", true) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = roleService.delete(id);
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }
}
