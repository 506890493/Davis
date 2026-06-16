package com.ruoyi.web.controller.davis;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.domain.CmsTask;
import com.ruoyi.system.domain.CmsTaskLog;
import com.ruoyi.system.service.ICmsTaskService;
import com.ruoyi.system.service.ICmsTaskLogService;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

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
    
    @Autowired
    private ICmsTaskLogService cmsTaskLogService;

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
     * 获取可分配的会计用户列表
     * 仅管理员和经理可调用
     * 注意：sys_role 表中"经理"角色的 role_key 是 'common'（RuoYi 默认普通角色），
     *      SecurityUtils.hasRole 必须用 'common' 才能命中；同时兼容 'manager' 字面量
     *      以防未来数据库 role_key 被统一改为 'manager'
     */
    @GetMapping("/assignableUsers")
    public AjaxResult getAssignableUsers()
    {
        Long userId = SecurityUtils.getUserId();
        boolean isManagerLike = SecurityUtils.hasRole("common") || SecurityUtils.hasRole("manager");
        if (!SecurityUtils.isAdmin(userId) && !isManagerLike)
        {
            // 无权限（sales/account 等），返回空列表
            return success();
        }
        List<SysUser> users = cmsTaskService.getAssignableUsers();
        return success(users);
    }

    /**
     * 会计将任务退回管理员(讲价)
     * POST /system/task/returnToAdmin
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
     * 拒绝协商价格
     * POST /system/task/rejectPrice
     * @param task 任务信息
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:audit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/rejectPrice")
    public AjaxResult rejectPrice(@RequestBody CmsTask task)
    {
        return toAjax(cmsTaskService.rejectPrice(task));
    }

    /**
     * 会计发起终止合作请求
     * POST /system/task/requestTermination
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
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:audit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/confirmTermination")
    public AjaxResult confirmTermination(@RequestBody Map<String, Object> params)
    {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        boolean approved = Boolean.parseBoolean(params.get("approved").toString());
        return toAjax(cmsTaskService.confirmTermination(taskId, approved));
    }

    /**
     * 会计完成续签
     * POST /system/task/completeRenewal
     * @param params 包含 taskId、newContract、generateContract
     * @return AjaxResult
     */
    @PreAuthorize("@ss.hasPermi('cms:task:edit')")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/completeRenewal")
    public AjaxResult completeRenewal(@RequestBody Map<String, Object> params)
    {
        Long taskId = Long.valueOf(params.get("taskId").toString());
        boolean generateContract = Boolean.parseBoolean(params.get("generateContract").toString());
        CmsContract newContract = null;
        if (generateContract && params.get("newContract") != null) {
            newContract = JSON.parseObject(
                JSON.toJSONString(params.get("newContract")),
                CmsContract.class);
        }
        return toAjax(cmsTaskService.completeRenewal(taskId, newContract, generateContract));
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
    
    /**
     * 获取任务操作日志列表
     */
    @PreAuthorize("@ss.hasPermi('system:task:query')")
    @GetMapping("/log")
    public TableDataInfo logList(CmsTaskLog cmsTaskLog)
    {
        startPage();
        List<CmsTaskLog> list = cmsTaskLogService.selectCmsTaskLogList(cmsTaskLog);
        return getDataTable(list);
    }

    /**
     * 获取待审批任务列表
     */
    @PreAuthorize("@ss.hasPermi('system:task:list')")
    @GetMapping("/pendingList")
    public TableDataInfo pendingList(CmsTask cmsTask)
    {
        cmsTask.setStatus("2");
        startPage();
        List<CmsTask> list = cmsTaskService.selectCmsTaskList(cmsTask);
        return getDataTable(list);
    }
}
