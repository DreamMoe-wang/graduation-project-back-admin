package com.example.admin.dto;

import lombok.Data;

/**
 * 操作日志查询参数
 */
@Data
public class OperationLogQueryDTO {

    private Integer pageNum;

    private Integer pageSize;

    private String menuName;

    private String actionName;

    private String username;

    private String ipAddress;

    private Integer operationStatus;

    private String startTime;

    private String endTime;
}
