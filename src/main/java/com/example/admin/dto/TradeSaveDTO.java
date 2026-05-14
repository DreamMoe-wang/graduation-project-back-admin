package com.example.admin.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 交易新增/编辑参数
 */
@Data
public class TradeSaveDTO {

    @NotBlank(message = "交易标题不能为空")
    private String title;

    @NotBlank(message = "委托人不能为空")
    private String clientName;

    @NotBlank(message = "委托人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String clientPhone;

    private String workerName;

    private String workerPhone;

    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.00", message = "交易金额不能小于 0")
    private BigDecimal amount;

    @Size(max = 255, message = "位置长度不能超过 255 个字符")
    private String location;

    @Size(max = 50, message = "城市长度不能超过 50 个字符")
    private String cityName;

    @Size(max = 50, message = "区域长度不能超过 50 个字符")
    private String areaName;

    private Double longitude;

    private Double latitude;

    @NotBlank(message = "交易状态不能为空")
    private String status;

    private String description;

    private List<String> imageUrls;

    private List<String> categoryNames;
}
