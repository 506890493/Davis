package com.ruoyi.system.service;

import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.domain.CmsTask;
import com.ruoyi.common.core.domain.entity.SysUser;

import java.util.List;

/**
 * 任务管理Service接口
 *
 * @author ruoyi
 * @date 2026-01-17
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

    /**
     * 完成催收任务
     *
     * @param taskId 任务ID
     * @param newContract 新合同信息
     * @return 结果
     */
    public int completeCollectionTask(Long taskId, CmsContract newContract);

    /**
     * 获取可分配的会计用户列表
     *
     * @return 会计角色用户列表
     */
    /**
     * 会计将任务退回管理员(讲价)
     *
     * @param task 任务信息
     * @return 结果
     */
    public int returnToAdmin(CmsTask task);

    /**
     * 管理员修改协商金额后重新派发
     *
     * @param task 任务信息
     * @return 结果
     */
    public int redispatch(CmsTask task);

    /**
     * 会计发起终止合作请求
     *
     * @param task 任务信息
     * @return 结果
     */
    public int requestTermination(CmsTask task);

    /**
     * 管理员确认终止合作
     *
     * @param taskId 任务ID
     * @param approved 是否同意终止
     * @return 结果
     */
    public int confirmTermination(Long taskId, boolean approved);

    /**
     * 会计完成续签
     *
     * @param task 任务信息
     * @return 结果
     */
    public int completeRenewal(CmsTask task);

    public List<SysUser> getAssignableUsers();
}