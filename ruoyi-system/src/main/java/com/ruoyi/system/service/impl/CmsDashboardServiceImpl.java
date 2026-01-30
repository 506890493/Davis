package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.domain.vo.DashboardStatsVo;
import com.ruoyi.system.mapper.CmsContractMapper;
import com.ruoyi.system.mapper.CmsTaskMapper;
import com.ruoyi.system.service.ICmsDashboardService;

/**
 * Dashboard统计Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-01-30
 */
@Service
public class CmsDashboardServiceImpl implements ICmsDashboardService
{
    @Autowired
    private CmsContractMapper cmsContractMapper;

    @Autowired
    private CmsTaskMapper cmsTaskMapper;

    /**
     * 获取Dashboard统计数据
     * 
     * @return 统计数据
     */
    @Override
    public DashboardStatsVo getDashboardStats()
    {
        DashboardStatsVo stats = new DashboardStatsVo();
        
        // 获取当前用户信息
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        Long userId = loginUser.getUserId();
        
        // 获取本月的开始和结束日期
        Date[] monthRange = getMonthDateRange();
        Date startDate = monthRange[0];
        Date endDate = monthRange[1];
        
        // 判断用户角色类型
        String roleType = determineRoleType(user);
        stats.setRoleType(roleType);
        
        if ("admin".equals(roleType)) {
            // 管理员：查看所有数据
            buildAdminStats(stats, startDate, endDate);
        } else if ("accountant".equals(roleType)) {
            // 会计：查看所有代账数据
            buildAccountantStats(stats, startDate, endDate);
        } else {
            // 销售/其他：查看自己的数据
            buildSalesStats(stats, startDate, endDate, userId);
        }
        
        return stats;
    }

    /**
     * 构建管理员统计数据
     */
    private void buildAdminStats(DashboardStatsVo stats, Date startDate, Date endDate)
    {
        // 本月到期合同数量
        Long expiringCount = cmsContractMapper.countExpiringContracts(startDate, endDate, null);
        stats.setExpiringContractCount(expiringCount);
        
        // 本月到期合同列表
        List<CmsContract> expiringContracts = cmsContractMapper.selectExpiringContracts(startDate, endDate, null);
        stats.setExpiringContracts(expiringContracts);
        
        // 本月应完成金额（到期合同总金额）
        BigDecimal targetAmount = cmsContractMapper.sumExpiringContractAmount(startDate, endDate, null);
        stats.setMonthTargetAmount(targetAmount);
        
        // 本月实际完成金额（已完成任务的金额）
        BigDecimal actualAmount = cmsTaskMapper.sumCompletedTaskAmount(startDate, endDate, null);
        stats.setMonthActualAmount(actualAmount);
    }

    /**
     * 构建会计统计数据
     */
    private void buildAccountantStats(DashboardStatsVo stats, Date startDate, Date endDate)
    {
        // 应收金额（本月到期合同总金额）
        BigDecimal receivable = cmsContractMapper.sumExpiringContractAmount(startDate, endDate, null);
        stats.setTotalReceivable(receivable);
        
        // 已完成金额（本月已完成任务的金额）
        BigDecimal received = cmsTaskMapper.sumCompletedTaskAmount(startDate, endDate, null);
        stats.setTotalReceived(received);
        
        // 代账收费应完成家数（代账类型合同本月到期数量）
        Long bookkeepingTarget = cmsContractMapper.countBookkeepingContracts(startDate, endDate, null);
        stats.setBookkeepingTargetCount(bookkeepingTarget);
        
        // 代账收费已完成家数（代账类型已完成任务数量）
        Long bookkeepingDone = cmsTaskMapper.countCompletedBookkeepingTasks(startDate, endDate, null);
        stats.setBookkeepingDoneCount(bookkeepingDone);
        
        // 也展示本月到期合同列表
        List<CmsContract> expiringContracts = cmsContractMapper.selectExpiringContracts(startDate, endDate, null);
        stats.setExpiringContracts(expiringContracts);
        stats.setExpiringContractCount((long) expiringContracts.size());
    }

    /**
     * 构建销售统计数据
     */
    private void buildSalesStats(DashboardStatsVo stats, Date startDate, Date endDate, Long userId)
    {
        // 我的客户总数
        Long customerCount = cmsContractMapper.countMyCustomers(userId);
        stats.setMyCustomerCount(customerCount);
        
        // 我的客户列表
        List<CmsContract> myCustomers = cmsContractMapper.selectMyCustomers(userId);
        stats.setMyCustomers(myCustomers);
        
        // 本月新增客户数
        Long newCount = cmsContractMapper.countNewCustomers(startDate, endDate, userId);
        stats.setNewCustomerCount(newCount);
        
        // 本月应完成金额（我的到期合同总金额）
        BigDecimal targetAmount = cmsContractMapper.sumExpiringContractAmount(startDate, endDate, userId);
        stats.setMonthTargetAmount(targetAmount);
        
        // 本月实际完成金额（我的已完成任务的金额）
        BigDecimal actualAmount = cmsTaskMapper.sumCompletedTaskAmount(startDate, endDate, userId);
        stats.setMonthActualAmount(actualAmount);
        
        // 我的到期合同列表
        List<CmsContract> expiringContracts = cmsContractMapper.selectExpiringContracts(startDate, endDate, userId);
        stats.setExpiringContracts(expiringContracts);
        stats.setExpiringContractCount((long) expiringContracts.size());
    }

    /**
     * 判断用户角色类型
     */
    private String determineRoleType(SysUser user)
    {
        if (SysUser.isAdmin(user.getUserId())) {
            return "admin";
        }
        
        List<SysRole> roles = user.getRoles();
        if (roles != null && !roles.isEmpty()) {
            for (SysRole role : roles) {
                String roleKey = role.getRoleKey();
                if ("accountant".equals(roleKey) || "finance".equals(roleKey)) {
                    return "accountant";
                }
                if ("sales".equals(roleKey)) {
                    return "sales";
                }
            }
        }
        
        // 默认返回销售类型（只能看自己的数据）
        return "sales";
    }

    /**
     * 获取本月的开始和结束日期
     */
    private Date[] getMonthDateRange()
    {
        Calendar calendar = Calendar.getInstance();
        
        // 本月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        
        // 本月最后一天
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Date endDate = calendar.getTime();
        
        return new Date[] { startDate, endDate };
    }
}
