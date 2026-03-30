package com.example.admin.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * 交易新增/编辑参数
 */
@Data
public class TradeSaveDTO {

    /**
     * 交易标题
     */
    @NotBlank(message = "交易标题不能为空")
    private String title;

    /**
     * 委托人姓名
     */
    @NotBlank(message = "委托人不能为空")
    private String clientName;

    /**
     * 委托人电话
     */
    @NotBlank(message = "委托人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String clientPhone;

    /**
     * 接单人姓名
     */
    private String workerName;

    /**
     * 接单人电话
     */
    private String workerPhone;

    /**
     * 金额
     */
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.00", message = "交易金额不能小于 0")
    private BigDecimal amount;

    /**
     * 状态
     */
    @NotBlank(message = "交易状态不能为空")
    private String status;

    /**
     * 描述
     */
    private String description;
}
