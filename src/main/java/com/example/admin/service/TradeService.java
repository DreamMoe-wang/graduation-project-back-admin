package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.TradeQueryDTO;
import com.example.admin.dto.TradeReviewDTO;
import com.example.admin.dto.TradeSaveDTO;
import com.example.admin.vo.TradeOrderStatsVO;
import com.example.admin.vo.TradeOrderVO;
import com.example.admin.vo.TradeVO;

import java.util.List;

/**
 * 交易模块服务
 */
public interface TradeService {

    /**
     * 发布管理分页数据
     */
    PageResult<List<TradeVO>> getPublishPage(TradeQueryDTO queryDTO);

    /**
     * 交易大全分页数据
     */
    PageResult<List<TradeVO>> getTradeListPage(TradeQueryDTO queryDTO);

    /**
     * 交易详情
     */
    TradeVO getTradeDetail(Long id);

    /**
     * 新增交易
     */
    boolean createTrade(TradeSaveDTO tradeSaveDTO);

    /**
     * 更新交易
     */
    boolean updateTrade(Long id, TradeSaveDTO tradeSaveDTO);

    /**
     * 删除交易
     */
    boolean deleteTrade(Long id);

    /**
     * 审核通过
     */
    boolean approveTrade(Long id, TradeReviewDTO reviewDTO);

    /**
     * 审核驳回
     */
    boolean rejectTrade(Long id, TradeReviewDTO reviewDTO);

    /**
     * 交易大全直接接取发布单
     */
    boolean receiveTradePost(Long postId);

    /**
     * 订单统计
     */
    TradeOrderStatsVO getOrderStats();

    /**
     * 订单分页
     */
    PageResult<List<TradeOrderVO>> getOrderPage(Integer pageNum, Integer pageSize, String status);

    /**
     * 订单详情
     */
    TradeOrderVO getOrderDetail(Long id);

    /**
     * 接单
     */
    boolean receiveOrder(Long id);

    /**
     * 完成订单
     */
    boolean completeOrder(Long id);

    /**
     * 订单支付
     */
    boolean payOrder(Long id);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long id);
}
