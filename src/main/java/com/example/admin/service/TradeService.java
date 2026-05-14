package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.TradeQueryDTO;
import com.example.admin.dto.TradeReviewDTO;
import com.example.admin.dto.TradeSaveDTO;
import com.example.admin.vo.TradeCategoryVO;
import com.example.admin.vo.TradeOrderStatsVO;
import com.example.admin.vo.TradeOrderVO;
import com.example.admin.vo.TradeVO;

import java.util.List;

/**
 * 交易模块服务
 */
public interface TradeService {

    PageResult<List<TradeVO>> getPublishPage(TradeQueryDTO queryDTO);

    PageResult<List<TradeVO>> getTradeListPage(TradeQueryDTO queryDTO);

    TradeVO getTradeDetail(Long id);

    boolean createTrade(TradeSaveDTO tradeSaveDTO);

    boolean updateTrade(Long id, TradeSaveDTO tradeSaveDTO);

    boolean deleteTrade(Long id);

    boolean approveTrade(Long id, TradeReviewDTO reviewDTO);

    boolean rejectTrade(Long id, TradeReviewDTO reviewDTO);

    boolean receiveTradePost(Long postId);

    List<TradeCategoryVO> getAvailableTradeCategories();

    TradeOrderStatsVO getOrderStats();

    PageResult<List<TradeOrderVO>> getOrderPage(Integer pageNum, Integer pageSize, String status);

    TradeOrderVO getOrderDetail(Long id);

    boolean receiveOrder(Long id);

    boolean completeOrder(Long id);

    boolean confirmOrder(Long id);

    boolean payOrder(Long id);

    boolean cancelOrder(Long id);
}
