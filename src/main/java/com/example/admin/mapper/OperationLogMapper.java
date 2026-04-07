package com.example.admin.mapper;

import com.example.admin.dto.OperationLogQueryDTO;
import com.example.admin.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface OperationLogMapper {

    OperationLog selectById(@Param("id") Long id);

    List<OperationLog> selectPage(@Param("query") OperationLogQueryDTO query,
                                  @Param("offset") long offset,
                                  @Param("pageSize") long pageSize);

    long countPage(@Param("query") OperationLogQueryDTO query);

    int insertOperationLog(OperationLog operationLog);

    int deleteById(@Param("id") Long id);

    int clean();
}
