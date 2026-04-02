package com.example.admin.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 当前用户个人信息修改参数
 */
@Data
public class UserProfileUpdateDTO {

    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    @Size(max = 255, message = "头像地址长度不能超过 255")
    private String avatar;

    @Size(max = 20, message = "手机号长度不能超过 20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 50, message = "真实姓名长度不能超过 50")
    private String realName;

    @Min(value = 0, message = "性别参数不正确")
    @Max(value = 2, message = "性别参数不正确")
    private Integer gender;

    @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "生日格式应为 yyyy-MM-dd")
    private String birthday;

    @Size(max = 50, message = "城市长度不能超过 50")
    private String cityName;

    @Size(max = 50, message = "区域长度不能超过 50")
    private String areaName;

    @Size(max = 255, message = "详细地址长度不能超过 255")
    private String address;

    @Size(max = 500, message = "个人简介长度不能超过 500")
    private String bio;
}