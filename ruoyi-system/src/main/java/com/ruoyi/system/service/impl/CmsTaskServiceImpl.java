package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CmsTaskMapper;
import com.ruoyi.system.domain.CmsTask;
import com.ruoyi.system.service.ICmsTaskService;
import com.ruoyi.system.service.ICmsContractService;
import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysNoticeService;
import com.ruoyi.system.domain.SysNotice;



/**
 * 任务管理Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-17
 */
@Service
public class CmsTaskServiceImpl implements ICmsTaskService
{
    @Autowired
    private CmsTaskMapper cmsTaskMapper;

    @Autowired
    private ICmsContractService cmsContractService;

    @Autowired
    private ISysNoticeService noticeService;

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
    @Transactional
    public int insertCmsTask(CmsTask cmsTask)
    {
        // 幂等性检查：检查是否存在未完成的同类型任务分配给同一个人
        CmsTask queryTask = new CmsTask();
        queryTask.setContractId(cmsTask.getContractId());
        queryTask.setAssignedTo(cmsTask.getAssignedTo());
        queryTask.setTaskType(cmsTask.getTaskType());
        
        List<CmsTask> existingTasks = cmsTaskMapper.selectCmsTaskList(queryTask);
        for (CmsTask task : existingTasks) {
            // 状态 0待处理 1进行中 2待审批
            if ("0".equals(task.getStatus()) || "1".equals(task.getStatus()) || "2".equals(task.getStatus())) {
                throw new ServiceException("该合同已分配给此人催收，且任务未完成");
            }
        }

        cmsTask.setCreateTime(DateUtils.getNowDate());
        
        // 更新合同状态为催收中
        CmsContract contract = cmsContractService.selectCmsContractByContractId(cmsTask.getContractId());
        if (contract != null) {
            contract.setReminderStatus("1"); // 1=已催交/催收中
            cmsContractService.updateCmsContract(contract);
        }

        // Send notification
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle("新的催收任务");
        notice.setNoticeType("2"); // 1-通知 2-公告
        notice.setNoticeContent("您有一个新的催收任务，请及时处理。");
        notice.setStatus("0"); // 0-正常 1-关闭
        notice.setCreateBy(String.valueOf(cmsTask.getAssignedTo()));
        noticeService.insertNotice(notice);
        return cmsTaskMapper.insertCmsTask(cmsTask);
    }

    /**
     * 修改任务管理
     *
     * @param cmsTask 任务管理
     * @return 结果
     */
    @Override
    @Transactional
    public int updateCmsTask(CmsTask cmsTask)
    {
        cmsTask.setUpdateTime(DateUtils.getNowDate());
        
        // 当任务状态更新为进行中(1)时，同步更新原合同的催收状态
        if ("1".equals(cmsTask.getStatus())) {
             CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(cmsTask.getTaskId());
             if (existingTask != null) {
                 Long contractIdToUpdate = existingTask.getSourceContractId();
                 // If sourceContractId is null but it is a collection task, use contractId
                 if (contractIdToUpdate == null && "1".equals(existingTask.getTaskType())) {
                     contractIdToUpdate = existingTask.getContractId();
                 }

                 if (contractIdToUpdate != null) {
                     CmsContract contract = cmsContractService.selectCmsContractByContractId(contractIdToUpdate);
                     if (contract != null) {
                         contract.setReminderStatus("1");
                         cmsContractService.updateCmsContract(contract);
                     }
                 }
             }
        }
        
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

    /**
     * 完成催收任务
     *
     * @param taskId 任务ID
     * @param newContract 新合同信息
     * @return 结果
     */
    @Override
    public int completeCollectionTask(Long taskId, CmsContract newContract) {
        CmsTask task = cmsTaskMapper.selectCmsTaskByTaskId(taskId);
        task.setStatus("4"); // 4 for completed
        cmsTaskMapper.updateCmsTask(task);

        CmsContract sourceContract = cmsContractService.selectCmsContractByContractId(task.getSourceContractId());

        CmsContract targetContract = new CmsContract();
        // copy customer info
        targetContract.setContractName(sourceContract.getContractName());
        targetContract.setContactPerson(sourceContract.getContactPerson());
        targetContract.setContactPhone(sourceContract.getContactPhone());
        // set parent id and audit status
        targetContract.setParentId(sourceContract.getContractId());
        targetContract.setAuditStatus("0"); // 0 for pending approval

        // save new contract
        cmsContractService.insertCmsContract(targetContract);

        // update task with new contract id
        task.setTargetContractId(targetContract.getContractId());
        return cmsTaskMapper.updateCmsTask(task);
    }
}