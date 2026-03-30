package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.TradeQueryDTO;
import com.example.admin.dto.TradeSaveDTO;
import com.example.admin.service.TradeService;
import com.example.admin.vo.TradeVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 交易发布管理控制器
 */
@RestController
@RequestMapping("/trade/publish")
public class TradePublishController {

    @Resource
    private TradeService tradeService;

    /**
     * 发布管理分页
     */
    @GetMapping("/page")
    public Result<PageResult<List<TradeVO>>> page(TradeQueryDTO queryDTO) {
        return Result.success(tradeService.getPublishPage(queryDTO));
    }

    /**
     * 交易详情
     */
    @GetMapping("/{id}")
    public Result<TradeVO> detail(@PathVariable Long id) {
        return Result.success(tradeService.getTradeDetail(id));
    }

    /**
     * 新增交易
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody @Validated TradeSaveDTO tradeSaveDTO) {
        boolean success = tradeService.createTrade(tradeSaveDTO);
        return success ? Result.success("发布成功", true) : Result.error("发布失败");
    }

    /**
     * 更新交易
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Validated TradeSaveDTO tradeSaveDTO) {
        boolean success = tradeService.updateTrade(id, tradeSaveDTO);
        return success ? Result.success("更新成功", true) : Result.error("更新失败");
    }

    /**
     * 删除交易
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = tradeService.deleteTrade(id);
        return success ? Result.success("删除成功", true) : Result.error("删除失败");
    }
}
