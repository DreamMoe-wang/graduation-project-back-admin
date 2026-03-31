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

    private String token;

    private String tokenType;

    private Long expiresIn;

    private List<String> roles;
}
