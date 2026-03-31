package com.example.admin.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 交易审核参数
 */
@Data
public class TradeReviewDTO {

    @Size(max = 255, message = "审核备注长度不能超过 255")
    private String reviewRemark;
}
