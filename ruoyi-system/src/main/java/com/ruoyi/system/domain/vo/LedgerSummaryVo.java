package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;

/**
 * 总账报表汇总VO
 *
 * @author ruoyi
 * @date 2026-03-01
 */
public class LedgerSummaryVo
{
    /** 总收入 */
    private BigDecimal totalAmount;

    /** 代账收入 */
    private BigDecimal dazhangAmount;

    /** 地址租赁收入 */
    private BigDecimal addressAmount;

    /** 总利润 */
    private BigDecimal totalProfit;

    /** 合同总数 */
    private Integer totalContracts;

    /** 代账合同数 */
    private Integer dazhangContracts;

    /** 地址合同数 */
    private Integer addressContracts;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDazhangAmount() {
        return dazhangAmount;
    }

    public void setDazhangAmount(BigDecimal dazhangAmount) {
        this.dazhangAmount = dazhangAmount;
    }

    public BigDecimal getAddressAmount() {
        return addressAmount;
    }

    public void setAddressAmount(BigDecimal addressAmount) {
        this.addressAmount = addressAmount;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Integer getTotalContracts() {
        return totalContracts;
    }

    public void setTotalContracts(Integer totalContracts) {
        this.totalContracts = totalContracts;
    }

    public Integer getDazhangContracts() {
        return dazhangContracts;
    }

    public void setDazhangContracts(Integer dazhangContracts) {
        this.dazhangContracts = dazhangContracts;
    }

    public Integer getAddressContracts() {
        return addressContracts;
    }

    public void setAddressContracts(Integer addressContracts) {
        this.addressContracts = addressContracts;
    }
}
