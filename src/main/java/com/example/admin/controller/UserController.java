package com.example.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.LoginDTO;
import com.example.admin.dto.UserDTO;
import com.example.admin.entity.User;
import com.example.admin.service.AuthService;
import com.example.admin.service.UserService;
import com.example.admin.vo.UserProfileVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@RequestParam String username, 
                                 @RequestParam String password) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);
        String token = authService.login(loginDTO).getToken();
        return Result.success("登录成功", token);
    }

    /**
     * 根据 ID 查询用户
     */
    @GetMapping("/{id}")
    public Result<UserProfileVO> getById(@PathVariable Long id) {
        return Result.success(userService.getProfileById(id));
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<PageResult<List<User>>> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<User> page = userService.pageList(pageNum, pageSize);
        PageResult<List<User>> result = PageResult.of(page.getTotal(), page.getRecords());
        return Result.success(result);
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> list = userService.list();
        return Result.success(list);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody @Validated UserDTO userDTO) {
        boolean success = userService.create(userDTO);
        return success ? Result.success("创建成功", true) : Result.error("创建失败");
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, 
                                   @RequestBody @Validated UserDTO userDTO) {
        boolean success = userService.update(id, userDTO);
        return success ? Result.success("更新成功", true) : Result.error("更新失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = userService.delete(id);
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }
}
