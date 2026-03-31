package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发布主表实体
 */
@Data
public class TradePost implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String postNo;

    private Long publisherId;

    private Integer postType;

    private String title;

    private String content;

    private BigDecimal price;

    private String cityName;

    private String areaName;

    private String address;

    private String contactName;

    private String contactPhone;

    private Integer status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String reviewRemark;

    private LocalDateTime publishTime;

    private LocalDateTime offShelfTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
