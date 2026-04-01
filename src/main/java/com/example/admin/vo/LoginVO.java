package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录返回信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private Long userId;

    private String username;

    private String nickname;

    private String displayName;

    private String avatar;

    private String phone;

    private String email;

    private String token;

    private String tokenType;

    private Long expiresIn;

    private List<String> roles;

    private List<String> roleNames;

    private List<String> authorities;

    private List<String> permissions;

    private UserProfileVO userInfo;

    private List<MenuVO> menus;
}
