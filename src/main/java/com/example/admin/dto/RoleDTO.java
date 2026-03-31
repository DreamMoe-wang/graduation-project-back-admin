package com.example.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 角色新增/编辑参数
 */
@Data
public class RoleDTO {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过 50")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过 50")
    private String roleCode;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
}
