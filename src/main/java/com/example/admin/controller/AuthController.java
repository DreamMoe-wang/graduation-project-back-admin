package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.dto.LoginDTO;
import com.example.admin.service.AuthService;
import com.example.admin.vo.CurrentUserVO;
import com.example.admin.vo.LoginVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        return Result.success("登录成功", authService.login(loginDTO));
    }

    /**
     * 当前用户
     */
    @GetMapping("/me")
    public Result<CurrentUserVO> me() {
        return Result.success(authService.currentUser());
    }
}
