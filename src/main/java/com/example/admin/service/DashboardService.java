package com.example.admin.service;

import com.example.admin.vo.DashboardOverviewVO;

/**
 * 首页服务
 */
public interface DashboardService {

    /**
     * 获取首页概览
     */
    DashboardOverviewVO getOverview();
}
