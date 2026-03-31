package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.entity.TradeOrder;
import com.example.admin.entity.TradePost;
import com.example.admin.entity.User;
import com.example.admin.mapper.TradeOrderMapper;
import com.example.admin.mapper.TradePostMapper;
import com.example.admin.mapper.UserMapper;
import com.example.admin.service.DashboardService;
import com.example.admin.vo.DashboardOverviewVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

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
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleted, 0);

        LambdaQueryWrapper<TradePost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(TradePost::getDeleted, 0);

        LambdaQueryWrapper<TradeOrder> orderWrapper = new LambdaQueryWrapper<>();
        long userCount = userMapper.selectCount(userWrapper);
        long visitCount = tradePostMapper.selectCount(postWrapper);
        long orderCount = tradeOrderMapper.selectCount(orderWrapper);

        LambdaQueryWrapper<TradeOrder> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(TradeOrder::getStatus, 2);
        List<TradeOrder> completedOrders = tradeOrderMapper.selectList(completedWrapper);
        BigDecimal salesAmount = completedOrders.stream()
                .map(TradeOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardOverviewVO.builder()
                .visitCount(visitCount)
                .userCount(userCount)
                .orderCount(orderCount)
                .salesAmount(salesAmount)
                .build();
    }
}
