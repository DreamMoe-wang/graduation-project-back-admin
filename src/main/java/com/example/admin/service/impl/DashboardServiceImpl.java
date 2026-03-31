package com.example.admin.service.impl;

import com.example.admin.mapper.TradeOrderMapper;
import com.example.admin.mapper.TradePostMapper;
import com.example.admin.mapper.UserMapper;
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
        long userCount = userMapper.countActiveUsers();
        long visitCount = tradePostMapper.countActivePosts();
        long orderCount = tradeOrderMapper.countAll();
        BigDecimal salesAmount = tradeOrderMapper.sumCompletedAmount();

        return DashboardOverviewVO.builder()
                .visitCount(visitCount)
                .userCount(userCount)
                .orderCount(orderCount)
                .salesAmount(salesAmount == null ? BigDecimal.ZERO : salesAmount)
                .build();
    }
}
