package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.system.domain.CmsContract;

/**
 * Dashboard统计数据VO
 * 
 * @author ruoyi
 * @date 2026-01-30
 */
public class DashboardStatsVo
{
    /** 用户角色类型: admin, accountant, sales */
    private String roleType;

    /** ========== 管理员/通用统计 ========== */
    
    /** 本月应完成金额（本月到期合同总金额） */
    private BigDecimal monthTargetAmount;
    
    /** 本月实际完成金额（已完成任务的金额） */
    private BigDecimal monthActualAmount;
    
    /** 本月到期合同数 */
    private Long expiringContractCount;
    
    /** 本月到期合同列表 */
    private List<CmsContract> expiringContracts;

    /** ========== 会计统计 ========== */
    
    /** 应收金额（本月需收款的合同总金额） */
    private BigDecimal totalReceivable;
    
    /** 已完成金额（本月已完成收款的金额） */
    private BigDecimal totalReceived;
    
    /** 代账收费应完成家数（代账类型合同本月需收款数量） */
    private Long bookkeepingTargetCount;
    
    /** 代账收费已完成家数（代账类型合同本月已完成数量） */
    private Long bookkeepingDoneCount;

    /** ========== 销售统计 ========== */
    
    /** 我的客户总数 */
    private Long myCustomerCount;
    
    /** 我的客户列表 */
    private List<CmsContract> myCustomers;
    
    /** 本月新增客户数 */
    private Long newCustomerCount;

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public BigDecimal getMonthTargetAmount() {
        return monthTargetAmount;
    }

    public void setMonthTargetAmount(BigDecimal monthTargetAmount) {
        this.monthTargetAmount = monthTargetAmount;
    }

    public BigDecimal getMonthActualAmount() {
        return monthActualAmount;
    }

    public void setMonthActualAmount(BigDecimal monthActualAmount) {
        this.monthActualAmount = monthActualAmount;
    }

    public Long getExpiringContractCount() {
        return expiringContractCount;
    }

    public void setExpiringContractCount(Long expiringContractCount) {
        this.expiringContractCount = expiringContractCount;
    }

    public List<CmsContract> getExpiringContracts() {
        return expiringContracts;
    }

    public void setExpiringContracts(List<CmsContract> expiringContracts) {
        this.expiringContracts = expiringContracts;
    }

    public BigDecimal getTotalReceivable() {
        return totalReceivable;
    }

    public void setTotalReceivable(BigDecimal totalReceivable) {
        this.totalReceivable = totalReceivable;
    }

    public BigDecimal getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(BigDecimal totalReceived) {
        this.totalReceived = totalReceived;
    }

    public Long getBookkeepingTargetCount() {
        return bookkeepingTargetCount;
    }

    public void setBookkeepingTargetCount(Long bookkeepingTargetCount) {
        this.bookkeepingTargetCount = bookkeepingTargetCount;
    }

    public Long getBookkeepingDoneCount() {
        return bookkeepingDoneCount;
    }

    public void setBookkeepingDoneCount(Long bookkeepingDoneCount) {
        this.bookkeepingDoneCount = bookkeepingDoneCount;
    }

    public Long getMyCustomerCount() {
        return myCustomerCount;
    }

    public void setMyCustomerCount(Long myCustomerCount) {
        this.myCustomerCount = myCustomerCount;
    }

    public List<CmsContract> getMyCustomers() {
        return myCustomers;
    }

    public void setMyCustomers(List<CmsContract> myCustomers) {
        this.myCustomers = myCustomers;
    }

    public Long getNewCustomerCount() {
        return newCustomerCount;
    }

    public void setNewCustomerCount(Long newCustomerCount) {
        this.newCustomerCount = newCustomerCount;
    }
}
