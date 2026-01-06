package com.ruoyi.web.controller.davis;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.service.ICmsContractService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 合同管理Controller
 * 
 * @author ruoyi
 * @date 2025-12-14
 */
@RestController
@RequestMapping("/system/contract")
public class CmsContractController extends BaseController
{
    @Autowired
    private ICmsContractService cmsContractService;

    /**
     * 查询合同管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:contract:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsContract cmsContract)
    {
        startPage();
        List<CmsContract> list = cmsContractService.selectCmsContractList(cmsContract);
        return getDataTable(list);
    }

    /**
     * 导出合同管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:contract:export')")
    @Log(title = "合同管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsContract cmsContract)
    {
        List<CmsContract> list = cmsContractService.selectCmsContractList(cmsContract);
        ExcelUtil<CmsContract> util = new ExcelUtil<CmsContract>(CmsContract.class);
        util.exportExcel(response, list, "合同管理数据");
    }

    @PreAuthorize("@ss.hasPermi('system:contract:import')")
    @Log(title = "合同管理", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<CmsContract> util = new ExcelUtil<>(CmsContract.class);
        List<CmsContract> contractList = util.importExcel(file.getInputStream());
        String message = cmsContractService.importCmsContract(contractList, updateSupport, getUsername());
        return AjaxResult.success(message);
    }

    /**
     * 获取合同管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:contract:query')")
    @GetMapping(value = "/{contractId}")
    public AjaxResult getInfo(@PathVariable("contractId") Long contractId)
    {
        return success(cmsContractService.selectCmsContractByContractId(contractId));
    }

    /**
     * 新增合同管理
     */
    @PreAuthorize("@ss.hasPermi('system:contract:add')")
    @Log(title = "合同管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsContract cmsContract)
    {
        return toAjax(cmsContractService.insertCmsContract(cmsContract));
    }

    /**
     * 修改合同管理
     */
    @PreAuthorize("@ss.hasPermi('system:contract:edit')")
    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsContract cmsContract)
    {
        return toAjax(cmsContractService.updateCmsContract(cmsContract));
    }

    /**
     * 删除合同管理
     */
    @PreAuthorize("@ss.hasPermi('system:contract:remove')")
    @Log(title = "合同管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{contractIds}")
    public AjaxResult remove(@PathVariable Long[] contractIds)
    {
        return toAjax(cmsContractService.deleteCmsContractByContractIds(contractIds));
    }
}
