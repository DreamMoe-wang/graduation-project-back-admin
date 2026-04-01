package com.example.admin.service;

import com.example.admin.dto.LoginDTO;
import com.example.admin.vo.CurrentUserVO;
import com.example.admin.vo.LoginVO;
import com.example.admin.vo.MenuVO;

import java.util.List;

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

    /**
     * 获取当前用户菜单
     */
    List<MenuVO> currentMenus();

    /**
     * 获取当前用户按钮/权限标识
     */
    List<String> currentPermissions();
}
