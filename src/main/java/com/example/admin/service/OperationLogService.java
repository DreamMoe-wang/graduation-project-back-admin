package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.OperationLogQueryDTO;
import com.example.admin.entity.OperationLog;
import com.example.admin.vo.OperationLogVO;

import java.util.List;

/**
 * 操作日志服务
 */
public interface OperationLogService {

    PageResult<List<OperationLogVO>> page(OperationLogQueryDTO queryDTO);

    OperationLogVO getById(Long id);

    void record(OperationLog operationLog);

    boolean delete(Long id);

    boolean clean();
}
