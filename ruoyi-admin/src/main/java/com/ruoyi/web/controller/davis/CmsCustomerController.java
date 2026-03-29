package com.ruoyi.web.controller.davis;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.CmsCustomer;
import com.ruoyi.system.service.ICmsCustomerService;

/**
 * 客户管理Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/customer")
public class CmsCustomerController extends BaseController
{
    @Autowired
    private ICmsCustomerService cmsCustomerService;

    /**
     * 获取客户列表
     */
    @PreAuthorize("@ss.hasPermi('system:customer:list')")
    @GetMapping("/list")
    public AjaxResult list(CmsCustomer cmsCustomer)
    {
        startPage();
        List<CmsCustomer> list = cmsCustomerService.selectCmsCustomerList(cmsCustomer);
        return success(getDataTable(list));
    }

    /**
     * 导出客户列表
     */
    @PreAuthorize("@ss.hasPermi('system:customer:export')")
    @Log(title = "客户管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public AjaxResult export(CmsCustomer cmsCustomer)
    {
        List<CmsCustomer> list = cmsCustomerService.selectCmsCustomerList(cmsCustomer);
        ExcelUtil<CmsCustomer> util = new ExcelUtil<CmsCustomer>(CmsCustomer.class);
        return util.exportExcel(list, "客户数据");
    }

    /**
     * 获取客户详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:customer:query')")
    @GetMapping("/{customerId}")
    public AjaxResult getInfo(@PathVariable("customerId") Long customerId)
    {
        return success(cmsCustomerService.selectCmsCustomerById(customerId));
    }

    /**
     * 新增客户
     */
    @PreAuthorize("@ss.hasPermi('system:customer:add')")
    @Log(title = "客户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsCustomer cmsCustomer)
    {
        return toAjax(cmsCustomerService.insertCmsCustomer(cmsCustomer));
    }

    /**
     * 修改客户
     */
    @PreAuthorize("@ss.hasPermi('system:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsCustomer cmsCustomer)
    {
        return toAjax(cmsCustomerService.updateCmsCustomer(cmsCustomer));
    }

    /**
     * 删除客户
     */
    @PreAuthorize("@ss.hasPermi('system:customer:remove')")
    @Log(title = "客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds)
    {
        return toAjax(cmsCustomerService.deleteCmsCustomerByIds(customerIds));
    }
}
