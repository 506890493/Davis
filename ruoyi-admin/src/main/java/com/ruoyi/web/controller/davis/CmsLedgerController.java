package com.ruoyi.web.controller.davis;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.vo.LedgerByPersonVo;
import com.ruoyi.system.service.ICmsLedgerService;

import javax.servlet.http.HttpServletResponse;

/**
 * 总账报表Controller
 *
 * @author ruoyi
 * @date 2026-03-01
 */
@RestController
@RequestMapping("/system/ledger")
public class CmsLedgerController extends BaseController
{
    @Autowired
    private ICmsLedgerService ledgerService;

    /**
     * 获取总账汇总数据
     */
    @PreAuthorize("@ss.hasPermi('cms:ledger:list')")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(required = false) Integer year,
                              @RequestParam(required = false) Integer month)
    {
        return success(ledgerService.getSummary(year, month));
    }

    /**
     * 按人员统计总账数据
     */
    @PreAuthorize("@ss.hasPermi('cms:ledger:list')")
    @GetMapping("/byPerson")
    public AjaxResult byPerson(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month)
    {
        return success(ledgerService.getByPerson(year, month));
    }

    /**
     * 获取年度趋势数据
     */
    @PreAuthorize("@ss.hasPermi('cms:ledger:list')")
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(required = false) Integer year)
    {
        if (year == null)
        {
            year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        }
        return success(ledgerService.getTrend(year));
    }

    /**
     * 导出总账报表数据
     */
    @PreAuthorize("@ss.hasPermi('cms:ledger:list')")
    @Log(title = "总账报表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Integer month)
    {
        List<LedgerByPersonVo> list = ledgerService.getByPerson(year, month);
        ExcelUtil<LedgerByPersonVo> util = new ExcelUtil<>(LedgerByPersonVo.class);
        util.exportExcel(response, list, "总账报表数据");
    }
}
