package com.example.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 菜单新增/编辑参数
 */
@Data
public class MenuDTO {

    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 100, message = "菜单名称长度不能超过 100")
    private String menuName;

    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    @Size(max = 255, message = "路由路径长度不能超过 255")
    private String path;

    @Size(max = 100, message = "路由名称长度不能超过 100")
    private String routeName;

    @Size(max = 255, message = "组件路径长度不能超过 255")
    private String component;

    @Size(max = 100, message = "图标长度不能超过 100")
    private String icon;

    @Size(max = 100, message = "权限标识长度不能超过 100")
    private String permissionCode;

    private Integer sortNo;

    private Integer visible;

    private Integer status;

    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
}
