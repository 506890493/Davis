package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 总账报表趋势VO
 *
 * @author ruoyi
 * @date 2026-03-01
 */
public class LedgerTrendVo
{
    /** 月份列表 ["1月","2月",...,"12月"] */
    private List<String> months;

    /** 代账月度收入 */
    private List<BigDecimal> dazhangData;

    /** 地址月度收入 */
    private List<BigDecimal> addressData;

    /** 总月度收入 */
    private List<BigDecimal> totalData;

    public List<String> getMonths() {
        return months;
    }

    public void setMonths(List<String> months) {
        this.months = months;
    }

    public List<BigDecimal> getDazhangData() {
        return dazhangData;
    }

    public void setDazhangData(List<BigDecimal> dazhangData) {
        this.dazhangData = dazhangData;
    }

    public List<BigDecimal> getAddressData() {
        return addressData;
    }

    public void setAddressData(List<BigDecimal> addressData) {
        this.addressData = addressData;
    }

    public List<BigDecimal> getTotalData() {
        return totalData;
    }

    public void setTotalData(List<BigDecimal> totalData) {
        this.totalData = totalData;
    }
}
