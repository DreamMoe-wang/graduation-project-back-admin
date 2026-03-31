package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通用模块数据项
 */
@Data
public class ModuleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String moduleName;

    private String name;

    private String code;

    private Integer status;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
