package com.example.admin.service.impl;

import com.example.admin.mapper.TradeOrderMapper;
import com.example.admin.mapper.TradePostMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.security.SecurityUtils;
import com.example.admin.service.DashboardService;
import com.example.admin.vo.DashboardOverviewVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * 首页服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TradePostMapper tradePostMapper;

    @Resource
    private TradeOrderMapper tradeOrderMapper;

    @Override
    public DashboardOverviewVO getOverview() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean admin = SecurityUtils.isAdmin();

        long visitCount = tradePostMapper.countActivePosts();
        long userCount = userMapper.countActiveUsers();
        long orderCount = tradeOrderMapper.countAll();
        BigDecimal salesAmount = tradeOrderMapper.sumCompletedAmount();

        long publishOrderCount = tradeOrderMapper.countByPublisher(currentUserId, admin);
        long receiveOrderCount = tradeOrderMapper.countByReceiver(currentUserId, admin);
        BigDecimal publishAmount = tradeOrderMapper.sumCompletedAmountByPublisher(currentUserId, admin);
        BigDecimal receiveAmount = tradeOrderMapper.sumCompletedAmountByReceiver(currentUserId, admin);

        return DashboardOverviewVO.builder()
                .visitCount(visitCount)
                .userCount(userCount)
                .orderCount(orderCount)
                .salesAmount(salesAmount == null ? BigDecimal.ZERO : salesAmount)
                .publishOrderCount(publishOrderCount)
                .receiveOrderCount(receiveOrderCount)
                .publishAmount(publishAmount == null ? BigDecimal.ZERO : publishAmount)
                .receiveAmount(receiveAmount == null ? BigDecimal.ZERO : receiveAmount)
                .build();
    }
}
