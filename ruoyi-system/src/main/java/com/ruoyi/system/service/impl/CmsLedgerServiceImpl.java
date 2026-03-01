package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.system.domain.vo.LedgerByPersonVo;
import com.ruoyi.system.domain.vo.LedgerSummaryVo;
import com.ruoyi.system.domain.vo.LedgerTrendVo;
import com.ruoyi.system.mapper.CmsContractMapper;
import com.ruoyi.system.mapper.CmsTaskMapper;
import com.ruoyi.system.service.ICmsLedgerService;

/**
 * 总账报表Service业务层处理
 *
 * @author ruoyi
 * @date 2026-03-01
 */
@Service
public class CmsLedgerServiceImpl implements ICmsLedgerService
{
    @Autowired
    private CmsContractMapper contractMapper;

    @Autowired
    private CmsTaskMapper taskMapper;

    /**
     * 获取总账汇总数据
     *
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @return 汇总数据
     */
    @Override
    public LedgerSummaryVo getSummary(Integer year, Integer month)
    {
        LedgerSummaryVo vo = new LedgerSummaryVo();

        BigDecimal dazhangAmt = contractMapper.sumAmountByType("1", year, month);
        BigDecimal addressAmt = contractMapper.sumAmountByType("2", year, month);
        Integer dazhangCnt = contractMapper.countByType("1", year, month);
        Integer addressCnt = contractMapper.countByType("2", year, month);

        vo.setDazhangAmount(dazhangAmt != null ? dazhangAmt : BigDecimal.ZERO);
        vo.setAddressAmount(addressAmt != null ? addressAmt : BigDecimal.ZERO);
        vo.setTotalAmount(vo.getDazhangAmount().add(vo.getAddressAmount()));
        vo.setTotalProfit(vo.getTotalAmount());
        vo.setDazhangContracts(dazhangCnt != null ? dazhangCnt : 0);
        vo.setAddressContracts(addressCnt != null ? addressCnt : 0);
        vo.setTotalContracts(vo.getDazhangContracts() + vo.getAddressContracts());

        return vo;
    }

    /**
     * 按人员统计总账数据
     *
     * @param year 年份（可选）
     * @param month 月份（可选）
     * @return 人员统计列表
     */
    @Override
    public List<LedgerByPersonVo> getByPerson(Integer year, Integer month)
    {
        return contractMapper.selectLedgerByPerson(year, month);
    }

    /**
     * 获取年度趋势数据
     *
     * @param year 年份
     * @return 趋势数据
     */
    @Override
    public LedgerTrendVo getTrend(Integer year)
    {
        LedgerTrendVo vo = new LedgerTrendVo();
        List<String> months = new ArrayList<>();
        List<BigDecimal> dazhangData = new ArrayList<>();
        List<BigDecimal> addressData = new ArrayList<>();
        List<BigDecimal> totalData = new ArrayList<>();

        for (int m = 1; m <= 12; m++)
        {
            months.add(m + "月");
            BigDecimal dz = contractMapper.sumAmountByType("1", year, m);
            BigDecimal addr = contractMapper.sumAmountByType("2", year, m);
            dz = dz != null ? dz : BigDecimal.ZERO;
            addr = addr != null ? addr : BigDecimal.ZERO;
            dazhangData.add(dz);
            addressData.add(addr);
            totalData.add(dz.add(addr));
        }

        vo.setMonths(months);
        vo.setDazhangData(dazhangData);
        vo.setAddressData(addressData);
        vo.setTotalData(totalData);
        return vo;
    }
}
