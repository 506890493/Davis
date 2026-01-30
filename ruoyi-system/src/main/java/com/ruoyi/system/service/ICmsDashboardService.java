package com.ruoyi.system.service;

import com.ruoyi.system.domain.vo.DashboardStatsVo;

/**
 * Dashboard统计Service接口
 * 
 * @author ruoyi
 * @date 2026-01-30
 */
public interface ICmsDashboardService
{
    /**
     * 获取Dashboard统计数据
     * 
     * @return 统计数据
     */
    public DashboardStatsVo getDashboardStats();
}
