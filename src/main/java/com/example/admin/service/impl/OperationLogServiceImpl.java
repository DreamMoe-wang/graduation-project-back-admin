package com.example.admin.service.impl;

import com.example.admin.common.PageResult;
import com.example.admin.dto.OperationLogQueryDTO;
import com.example.admin.entity.OperationLog;
import com.example.admin.mapper.OperationLogMapper;
import com.example.admin.service.OperationLogService;
import com.example.admin.vo.OperationLogVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志服务实现
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public PageResult<List<OperationLogVO>> page(OperationLogQueryDTO queryDTO) {
        int currentPage = queryDTO == null || queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1
                ? 1
                : queryDTO.getPageNum();
        int pageSize = queryDTO == null || queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1
                ? 10
                : queryDTO.getPageSize();
        long total = operationLogMapper.countPage(queryDTO);
        List<OperationLogVO> records = operationLogMapper.selectPage(queryDTO, (long) (currentPage - 1) * pageSize, pageSize)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(total, records);
    }

    @Override
    public OperationLogVO getById(Long id) {
        OperationLog operationLog = operationLogMapper.selectById(id);
        if (operationLog == null) {
            throw new RuntimeException("日志记录不存在");
        }
        return toVO(operationLog);
    }

    @Override
    public void record(OperationLog operationLog) {
        if (operationLog == null) {
            return;
        }
        operationLogMapper.insertOperationLog(operationLog);
    }

    @Override
    public boolean delete(Long id) {
        return operationLogMapper.deleteById(id) > 0;
    }

    @Override
    public boolean clean() {
        return operationLogMapper.clean() >= 0;
    }

    private OperationLogVO toVO(OperationLog operationLog) {
        return OperationLogVO.builder()
                .id(operationLog.getId())
                .userId(operationLog.getUserId())
                .username(operationLog.getUsername())
                .menuName(operationLog.getMenuName())
                .menuPath(operationLog.getMenuPath())
                .actionName(operationLog.getActionName())
                .requestMethod(operationLog.getRequestMethod())
                .requestUri(operationLog.getRequestUri())
                .ipAddress(operationLog.getIpAddress())
                .operationStatus(operationLog.getOperationStatus())
                .durationMs(operationLog.getDurationMs())
                .resultMessage(operationLog.getResultMessage())
                .createTime(operationLog.getCreateTime() == null ? null : operationLog.getCreateTime().format(DATETIME_FORMATTER))
                .build();
    }
}
