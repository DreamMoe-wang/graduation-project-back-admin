package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前登录用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserVO {

    private Long userId;

    private String username;

    private String nickname;

    private String displayName;

    private String avatar;

    private String phone;

    private String email;

    private List<String> roles;

    private List<String> roleNames;

    private List<String> authorities;

    private UserProfileVO userInfo;
}
