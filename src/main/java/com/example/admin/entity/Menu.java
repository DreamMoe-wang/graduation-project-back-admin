package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单实体
 */
@Data
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    private String menuName;

    private Integer menuType;

    private String path;

    private String routeName;

    private String component;

    private String icon;

    private String permissionCode;

    private Integer sortNo;

    private Integer visible;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
