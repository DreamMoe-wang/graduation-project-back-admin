package com.example.admin.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 角色详情
 */
@Data
@Builder
public class RoleDetailVO {

    private Long id;

    private String roleName;

    private String roleCode;

    private Integer status;

    private String remark;

    private List<Long> menuIds;
}
