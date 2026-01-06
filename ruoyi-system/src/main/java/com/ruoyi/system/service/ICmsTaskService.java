package com.ruoyi.system.service;

import com.ruoyi.system.domain.CmsTask;

import java.util.List;

/**
 * 任务管理Service接口
 * 
 * @author ruoyi
 * @date 2025-11-29
 */
public interface ICmsTaskService 
{
    /**
     * 查询任务管理
     * 
     * @param taskId 任务管理主键
     * @return 任务管理
     */
    public CmsTask selectCmsTaskByTaskId(Long taskId);

    /**
     * 查询任务管理列表
     * 
     * @param cmsTask 任务管理
     * @return 任务管理集合
     */
    public List<CmsTask> selectCmsTaskList(CmsTask cmsTask);

    /**
     * 新增任务管理
     * 
     * @param cmsTask 任务管理
     * @return 结果
     */
    public int insertCmsTask(CmsTask cmsTask);

    /**
     * 修改任务管理
     * 
     * @param cmsTask 任务管理
     * @return 结果
     */
    public int updateCmsTask(CmsTask cmsTask);

    /**
     * 批量删除任务管理
     * 
     * @param taskIds 需要删除的任务管理主键集合
     * @return 结果
     */
    public int deleteCmsTaskByTaskIds(Long[] taskIds);

    /**
     * 删除任务管理信息
     * 
     * @param taskId 任务管理主键
     * @return 结果
     */
    public int deleteCmsTaskByTaskId(Long taskId);
}
