package com.example.admin.service;

import com.example.admin.dto.LoginDTO;
import com.example.admin.vo.CurrentUserVO;
import com.example.admin.vo.LoginVO;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前登录用户
     */
    CurrentUserVO currentUser();
}
