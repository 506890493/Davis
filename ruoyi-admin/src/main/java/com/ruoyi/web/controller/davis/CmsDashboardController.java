package com.ruoyi.web.controller.davis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.vo.DashboardStatsVo;
import com.ruoyi.system.service.ICmsDashboardService;

/**
 * Dashboard统计Controller
 * 
 * @author ruoyi
 * @date 2026-01-30
 */
@RestController
@RequestMapping("/system/dashboard")
public class CmsDashboardController extends BaseController
{
    @Autowired
    private ICmsDashboardService cmsDashboardService;

    /**
     * 获取Dashboard统计数据
     */
    @PreAuthorize("@ss.hasPermi('system:dashboard:query')")
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        DashboardStatsVo stats = cmsDashboardService.getDashboardStats();
        return success(stats);
    }
}
