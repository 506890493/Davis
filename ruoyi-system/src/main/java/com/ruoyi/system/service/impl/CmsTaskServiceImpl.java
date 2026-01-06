package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.CmsTask;
import com.ruoyi.system.mapper.CmsTaskMapper;
import com.ruoyi.system.service.ICmsTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务管理Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-29
 */
@Service
public class CmsTaskServiceImpl implements ICmsTaskService 
{
    @Autowired
    private CmsTaskMapper cmsTaskMapper;

    /**
     * 查询任务管理
     * 
     * @param taskId 任务管理主键
     * @return 任务管理
     */
    @Override
    public CmsTask selectCmsTaskByTaskId(Long taskId)
    {
        return cmsTaskMapper.selectCmsTaskByTaskId(taskId);
    }

    /**
     * 查询任务管理列表
     * 
     * @param cmsTask 任务管理
     * @return 任务管理
     */
    @Override
    public List<CmsTask> selectCmsTaskList(CmsTask cmsTask)
    {
        return cmsTaskMapper.selectCmsTaskList(cmsTask);
    }

    /**
     * 新增任务管理
     * 
     * @param cmsTask 任务管理
     * @return 结果
     */
    @Override
    public int insertCmsTask(CmsTask cmsTask)
    {
        cmsTask.setCreateTime(DateUtils.getNowDate());
        return cmsTaskMapper.insertCmsTask(cmsTask);
    }

    /**
     * 修改任务管理
     * 
     * @param cmsTask 任务管理
     * @return 结果
     */
    @Override
    public int updateCmsTask(CmsTask cmsTask)
    {
        cmsTask.setUpdateTime(DateUtils.getNowDate());
        return cmsTaskMapper.updateCmsTask(cmsTask);
    }

    /**
     * 批量删除任务管理
     * 
     * @param taskIds 需要删除的任务管理主键
     * @return 结果
     */
    @Override
    public int deleteCmsTaskByTaskIds(Long[] taskIds)
    {
        return cmsTaskMapper.deleteCmsTaskByTaskIds(taskIds);
    }

    /**
     * 删除任务管理信息
     * 
     * @param taskId 任务管理主键
     * @return 结果
     */
    @Override
    public int deleteCmsTaskByTaskId(Long taskId)
    {
        return cmsTaskMapper.deleteCmsTaskByTaskId(taskId);
    }
}
