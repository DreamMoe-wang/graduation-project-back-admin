package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.TradeQueryDTO;
import com.example.admin.service.TradeService;
import com.example.admin.vo.TradeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易大全控制器
 */
@RestController
@RequestMapping("/trade/list")
public class TradeListController {

    @Resource
    private TradeService tradeService;

    /**
     * 交易大全分页
     */
    @GetMapping("/page")
    public Result<PageResult<List<TradeVO>>> page(TradeQueryDTO queryDTO) {
        return Result.success(tradeService.getTradeListPage(queryDTO));
    }

    /**
     * 交易详情
     */
    @GetMapping("/{id}")
    public Result<TradeVO> detail(@PathVariable Long id) {
        return Result.success(tradeService.getTradeDetail(id));
    }

    /**
     * 接取交易
     */
    @PostMapping("/{id}/receive")
    public Result<Boolean> receive(@PathVariable Long id) {
        boolean success = tradeService.receiveTradePost(id);
        return success ? Result.success("接取成功", true) : Result.error("接取失败");
    }

    /**
     * 导出接口占位
     */
    @GetMapping("/export")
    public Result<Map<String, Object>> export(TradeQueryDTO queryDTO) {
        Map<String, Object> data = new HashMap<>();
        data.put("ready", false);
        data.put("message", "导出接口已预留，待数据库表完成后接入文件导出能力");
        data.put("query", queryDTO);
        return Result.success("导出接口已预留", data);
    }
}