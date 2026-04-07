package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.OperationLogQueryDTO;
import com.example.admin.service.OperationLogService;
import com.example.admin.vo.OperationLogVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 日志管理控制器
 */
@RestController
@RequestMapping("/log")
public class LogController {

    @Resource
    private OperationLogService operationLogService;

    @GetMapping("/page")
    public Result<PageResult<List<OperationLogVO>>> page(OperationLogQueryDTO queryDTO) {
        return Result.success(operationLogService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<OperationLogVO> detail(@PathVariable Long id) {
        return Result.success(operationLogService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = operationLogService.delete(id);
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }

    @DeleteMapping("/clean")
    public Result<Boolean> clean() {
        boolean success = operationLogService.clean();
        return success ? Result.success("清空成功", true) : Result.error("清空失败");
    }
}
