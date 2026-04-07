package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.MenuDTO;
import com.example.admin.service.MenuService;
import com.example.admin.vo.MenuVO;
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
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private MenuService menuService;

    @GetMapping("/page")
    public Result<PageResult<List<MenuVO>>> page(Integer pageNum, Integer pageSize) {
        return Result.success(menuService.page(pageNum, pageSize));
    }

    @GetMapping("/tree")
    public Result<List<MenuVO>> tree() {
        return Result.success(menuService.tree());
    }

    @GetMapping("/{id}")
    public Result<MenuVO> detail(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody @Validated MenuDTO menuDTO) {
        boolean success = menuService.create(menuDTO);
        return success ? Result.success("新增成功", true) : Result.error("新增失败");
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Validated MenuDTO menuDTO) {
        boolean success = menuService.update(id, menuDTO);
        return success ? Result.success("更新成功", true) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = menuService.delete(id);
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }
}
