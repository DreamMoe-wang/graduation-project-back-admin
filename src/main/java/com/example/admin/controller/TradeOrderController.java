package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.service.TradeService;
import com.example.admin.vo.TradeOrderStatsVO;
import com.example.admin.vo.TradeOrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/trade/order")
public class TradeOrderController {

    @Resource
    private TradeService tradeService;

    @GetMapping("/stats")
    public Result<TradeOrderStatsVO> stats() {
        return Result.success(tradeService.getOrderStats());
    }

    @GetMapping("/page")
    public Result<PageResult<List<TradeOrderVO>>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(tradeService.getOrderPage(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<TradeOrderVO> detail(@PathVariable Long id) {
        return Result.success(tradeService.getOrderDetail(id));
    }

    @PostMapping("/{id}/receive")
    public Result<Boolean> receive(@PathVariable Long id) {
        boolean success = tradeService.receiveOrder(id);
        return success ? Result.success("接单成功", true) : Result.error("接单失败");
    }

    @PostMapping("/{id}/complete")
    public Result<Boolean> complete(@PathVariable Long id) {
        boolean success = tradeService.completeOrder(id);
        return success ? Result.success("已提交完成，等待委托方确认", true) : Result.error("操作失败");
    }

    @PostMapping("/{id}/confirm")
    public Result<Boolean> confirm(@PathVariable Long id) {
        boolean success = tradeService.confirmOrder(id);
        return success ? Result.success("订单已确认完成", true) : Result.error("操作失败");
    }

    @PostMapping("/{id}/pay")
    public Result<Boolean> pay(@PathVariable Long id) {
        boolean success = tradeService.payOrder(id);
        return success ? Result.success("支付成功", true) : Result.error("支付失败");
    }

    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancel(@PathVariable Long id) {
        boolean success = tradeService.cancelOrder(id);
        return success ? Result.success("订单已取消", true) : Result.error("操作失败");
    }
}
