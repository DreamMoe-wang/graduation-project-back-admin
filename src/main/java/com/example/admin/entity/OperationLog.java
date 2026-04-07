package com.example.admin.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private LocalDateTime createTime;
}
