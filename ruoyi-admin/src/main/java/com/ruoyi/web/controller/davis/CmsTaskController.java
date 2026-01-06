package com.ruoyi.web.controller.davis;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.CmsTask;
import com.ruoyi.system.service.ICmsTaskService;
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
}
