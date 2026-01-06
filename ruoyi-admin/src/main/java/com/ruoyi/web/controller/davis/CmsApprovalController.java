package com.ruoyi.web.controller.davis;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.CmsApproval;
import com.ruoyi.system.service.ICmsApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 业务审批Controller
 * 
 * @author ruoyi
 * @date 2025-11-29
 */
@RestController
@RequestMapping("/system/approval")
public class CmsApprovalController extends BaseController
{
    @Autowired
    private ICmsApprovalService cmsApprovalService;

    /**
     * 查询业务审批列表
     */
    @PreAuthorize("@ss.hasPermi('system:approval:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsApproval cmsApproval)
    {
        startPage();
        List<CmsApproval> list = cmsApprovalService.selectCmsApprovalList(cmsApproval);
        return getDataTable(list);
    }

    /**
     * 导出业务审批列表
     */
    @PreAuthorize("@ss.hasPermi('system:approval:export')")
    @Log(title = "业务审批", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsApproval cmsApproval)
    {
        List<CmsApproval> list = cmsApprovalService.selectCmsApprovalList(cmsApproval);
        ExcelUtil<CmsApproval> util = new ExcelUtil<CmsApproval>(CmsApproval.class);
        util.exportExcel(response, list, "业务审批数据");
    }

    /**
     * 获取业务审批详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:approval:query')")
    @GetMapping(value = "/{approvalId}")
    public AjaxResult getInfo(@PathVariable("approvalId") Long approvalId)
    {
        return success(cmsApprovalService.selectCmsApprovalByApprovalId(approvalId));
    }

    /**
     * 新增业务审批
     */
    @PreAuthorize("@ss.hasPermi('system:approval:add')")
    @Log(title = "业务审批", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsApproval cmsApproval)
    {
        return toAjax(cmsApprovalService.insertCmsApproval(cmsApproval));
    }

    /**
     * 修改业务审批
     */
    @PreAuthorize("@ss.hasPermi('system:approval:edit')")
    @Log(title = "业务审批", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsApproval cmsApproval)
    {
        return toAjax(cmsApprovalService.updateCmsApproval(cmsApproval));
    }

    /**
     * 删除业务审批
     */
    @PreAuthorize("@ss.hasPermi('system:approval:remove')")
    @Log(title = "业务审批", businessType = BusinessType.DELETE)
	@DeleteMapping("/{approvalIds}")
    public AjaxResult remove(@PathVariable Long[] approvalIds)
    {
        return toAjax(cmsApprovalService.deleteCmsApprovalByApprovalIds(approvalIds));
    }
}
