package com.example.admin.service.impl;

import com.example.admin.service.DashboardService;
import com.example.admin.vo.DashboardOverviewVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 首页服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public DashboardOverviewVO getOverview() {
        return DashboardOverviewVO.builder()
                .visitCount(1528L)
                .userCount(286L)
                .orderCount(12L)
                .salesAmount(new BigDecimal("8620.00"))
                .build();
    }
}
