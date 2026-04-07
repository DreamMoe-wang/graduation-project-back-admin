package com.example.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作日志返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogVO {

    private Long id;

    private Long userId;

    private String username;

    private String menuName;

    private String menuPath;

    private String actionName;

    private String requestMethod;

    private String requestUri;

    private String ipAddress;

    private Integer operationStatus;

    private Long durationMs;

    private String resultMessage;

    private String createTime;
}
