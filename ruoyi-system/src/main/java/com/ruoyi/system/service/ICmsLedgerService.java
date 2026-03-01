package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.vo.LedgerByPersonVo;
import com.ruoyi.system.domain.vo.LedgerSummaryVo;
import com.ruoyi.system.domain.vo.LedgerTrendVo;

/**
 * 总账报表Service接口
 *
 * @author ruoyi
 * @date 2026-03-01
 */
public interface ICmsLedgerService
{
    /**
     * 获取总账汇总数据
     *
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @return 汇总数据
     */
    public LedgerSummaryVo getSummary(Integer year, Integer month);

    /**
     * 按人员统计总账数据
     *
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @return 人员统计列表
     */
    public List<LedgerByPersonVo> getByPerson(Integer year, Integer month);

    /**
     * 获取年度趋势数据
     *
     * @param year 年份
     * @return 趋势数据
     */
    public LedgerTrendVo getTrend(Integer year);
}
