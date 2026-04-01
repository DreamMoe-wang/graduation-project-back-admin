package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO {

    private Long id;

    private Long parentId;

    private String name;

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

    private List<MenuVO> children;
}
