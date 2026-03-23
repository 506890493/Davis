package com.ruoyi.web.controller.davis;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.CmsTask;
import com.ruoyi.system.service.ICmsTaskService;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 任务管理Controller
 * 
 * @author ruoyi
 * @date 2025-11-29
 */
@RestController
@RequestMapping("/system/task")
public class CmsTaskController extends BaseController
{
    @Autowired
    private ICmsTaskService cmsTaskService;

    /**
     * 查询任务管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsTask cmsTask)
    {
        startPage();
        List<CmsTask> list = cmsTaskService.selectCmsTaskList(cmsTask);
        return getDataTable(list);
    }

    /**
     * 导出任务管理列表
     */
    @PreAuthorize("@ss.hasPermi('system:task:export')")
    @Log(title = "任务管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CmsTask cmsTask)
    {
        List<CmsTask> list = cmsTaskService.selectCmsTaskList(cmsTask);
        ExcelUtil<CmsTask> util = new ExcelUtil<CmsTask>(CmsTask.class);
        util.exportExcel(response, list, "任务管理数据");
    }

    /**
     * 获取任务管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(cmsTaskService.selectCmsTaskByTaskId(taskId));
    }

    /**
     * 新增任务管理
     */
    @PreAuthorize("@ss.hasPermi('system:task:add')")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsTask cmsTask)
    {
        return toAjax(cmsTaskService.insertCmsTask(cmsTask));
    }

    /**
     * 修改任务管理
     */
    @PreAuthorize("@ss.hasPermi('system:task:edit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsTask cmsTask)
    {
        return toAjax(cmsTaskService.updateCmsTask(cmsTask));
    }

    /**
     * 删除任务管理
     */
    @PreAuthorize("@ss.hasPermi('system:task:remove')")
    @Log(title = "任务管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(cmsTaskService.deleteCmsTaskByTaskIds(taskIds));
    }

    /**
    /**
     * 获取可分配的会计用户列表
     */
    @PreAuthorize("@ss.hasPermi('cms:task:dispatch')")
    @GetMapping("/assignableUsers")
    public AjaxResult getAssignableUsers()
    {
        List<SysUser> users = cmsTaskService.getAssignableUsers();
        return success(users);
    }

    /**
     * 会计将任务退回管理员(讲价)
     * POST /system/task/returnToAdmin
     * @param taskId 任务ID
     * @param remark 退回原因
     * @param currentAmount 客户期望价格
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:edit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/returnToAdmin")
    public AjaxResult returnToAdmin(@RequestBody CmsTask task)
    {
        return toAjax(cmsTaskService.returnToAdmin(task));
    }

    /**
     * 管理员修改协商金额后重新派发
     * POST /system/task/redispatch
     * @param taskId 任务ID
     * @param currentAmount 修改后的金额
     * @param assigneeId 新分配的会计ID
     * @param deadline 截止日期
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:dispatch')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/redispatch")
    public AjaxResult redispatch(@RequestBody CmsTask task)
    {
        return toAjax(cmsTaskService.redispatch(task));
    }

    /**
     * 会计发起终止合作请求
     * POST /system/task/requestTermination
     * @param taskId 任务ID
     * @param remark 终止原因
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:edit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/requestTermination")
    public AjaxResult requestTermination(@RequestBody CmsTask task)
    {
        return toAjax(cmsTaskService.requestTermination(task));
    }

    /**
     * 管理员确认终止合作
     * POST /system/task/confirmTermination
     * @param taskId 任务ID
     * @param approved 是否同意终止
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:audit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/confirmTermination")
    public AjaxResult confirmTermination(@RequestBody java.util.Map<String, Object> params)
    {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        boolean approved = Boolean.parseBoolean(params.get("approved").toString());
        return toAjax(cmsTaskService.confirmTermination(taskId, approved));
    }

    /**
     * 会计完成续签
     * POST /system/task/completeRenewal
     * @param taskId 任务ID
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:edit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/completeRenewal")
    public AjaxResult completeRenewal(@RequestBody CmsTask task)
    {
        return toAjax(cmsTaskService.completeRenewal(task));
    }

    /**
     * 确认收款（催收任务完成）
     * POST /system/task/confirmPayment
     * @param task 任务信息（包含taskId, actualAmount, receiveRemark）
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:edit')")
    @Log(title = "确认收款", businessType = BusinessType.UPDATE)
    @PostMapping("/confirmPayment")
    public AjaxResult confirmPayment(@RequestBody CmsTask task)
    {
        return toAjax(cmsTaskService.confirmPayment(task));
    }
}
