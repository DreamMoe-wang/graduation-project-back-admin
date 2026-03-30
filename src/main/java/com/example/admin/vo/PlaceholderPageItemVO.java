package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 占位模块返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceholderPageItemVO {

    private Long id;

    private String name;

    private String code;

    private Integer status;

    private String description;

    private String updateTime;
}
