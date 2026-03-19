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
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.ISysRoleService;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.entity.SysRole;



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

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

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
        // Non-admin users only see tasks assigned to them
        String roleType = determineRoleType();
        if (!"admin".equals(roleType)) {
            cmsTask.setAssignedTo(SecurityUtils.getUserId());
        }
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

    /**
     * 判断当前用户角色类型
     * @return admin/accountant/sales
     */
    private String determineRoleType() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null && loginUser.getUser() != null) {
                java.util.List<SysRole> roles = loginUser.getUser().getRoles();
                if (roles != null) {
                    for (SysRole role : roles) {
                        if ("admin".equals(role.getRoleKey())) return "admin";
                    }
                    for (SysRole role : roles) {
                        if ("accountant".equals(role.getRoleKey())) return "accountant";
                    }
                    for (SysRole role : roles) {
                        if ("sales".equals(role.getRoleKey())) return "sales";
                    }
                }
            }
        } catch (Exception e) {
            // default to admin view if role can't be determined
        }
        return "admin";
    }

    /**
     * 获取可分配的会计用户列表
     *
     * @return 会计角色用户列表
     */
    @Override
    public List<SysUser> getAssignableUsers()
    {
        // 查找accountant角色
        SysRole queryRole = new SysRole();
        List<SysRole> allRoles = sysRoleService.selectRoleList(queryRole);
        Long accountantRoleId = null;
        for (SysRole role : allRoles) {
            if ("accountant".equals(role.getRoleKey())) {
                accountantRoleId = role.getRoleId();
                break;
            }
        }
        if (accountantRoleId == null) {
            return new ArrayList<>();
        }
        // 通过角色ID查询已分配该角色的用户
        SysUser queryUser = new SysUser();
        queryUser.setRoleId(accountantRoleId);
        return sysUserService.selectAllocatedList(queryUser);
    }

    /**
     * 会计将任务退回管理员(讲价)
     *
     * @param task 任务信息
     * @return 结果
     */
    @Override
    @Transactional
    public int returnToAdmin(CmsTask task)
    {
        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setStatus("3"); // 3已退回
        updateTask.setRemark(task.getRemark());
        updateTask.setCurrentAmount(task.getCurrentAmount());
        updateTask.setUpdateTime(DateUtils.getNowDate());
        return cmsTaskMapper.updateCmsTask(updateTask);
    }

    /**
     * 管理员修改协商金额后重新派发
     *
     * @param task 任务信息
     * @return 结果
     */
    @Override
    @Transactional
    public int redispatch(CmsTask task)
    {
        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setCurrentAmount(task.getCurrentAmount());
        updateTask.setAssignedTo(task.getAssignedTo());
        updateTask.setDeadline(task.getDeadline());
        updateTask.setStatus("0"); // 0待处理
        updateTask.setUpdateTime(DateUtils.getNowDate());
        return cmsTaskMapper.updateCmsTask(updateTask);
    }

    /**
     * 会计发起终止合作请求
     *
     * @param task 任务信息
     * @return 结果
     */
    @Override
    @Transactional
    public int requestTermination(CmsTask task)
    {
        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setStatus("2"); // 2待审批
        updateTask.setTaskType("3"); // 3终止
        updateTask.setRemark(task.getRemark());
        updateTask.setUpdateTime(DateUtils.getNowDate());
        return cmsTaskMapper.updateCmsTask(updateTask);
    }

    /**
     * 管理员确认终止合作
     *
     * @param taskId 任务ID
     * @param approved 是否同意终止
     * @return 结果
     */
    @Override
    @Transactional
    public int confirmTermination(Long taskId, boolean approved)
    {
        CmsTask task = cmsTaskMapper.selectCmsTaskByTaskId(taskId);
        if (task == null) {
            throw new ServiceException("任务不存在");
        }

        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(taskId);
        updateTask.setUpdateTime(DateUtils.getNowDate());

        if (approved) {
            updateTask.setStatus("4"); // 4已完成
            
            // 更新关联合同的催收状态为已完成
            Long contractIdToUpdate = task.getSourceContractId();
            if (contractIdToUpdate == null) {
                contractIdToUpdate = task.getContractId();
            }
            
            if (contractIdToUpdate != null) {
                CmsContract contract = cmsContractService.selectCmsContractByContractId(contractIdToUpdate);
                if (contract != null) {
                    contract.setReminderStatus("3"); // 3已完成
                    cmsContractService.updateCmsContract(contract);
                }
            }
        } else {
            updateTask.setStatus("3"); // 3已退回
        }
        
        return cmsTaskMapper.updateCmsTask(updateTask);
    }

    /**
     * 会计完成续签
     *
     * @param task 任务信息
     * @return 结果
     */
    @Override
    @Transactional
    public int completeRenewal(CmsTask task)
    {
        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setStatus("4"); // 4已完成
        updateTask.setRemark(task.getRemark());
        updateTask.setUpdateTime(DateUtils.getNowDate());
        return cmsTaskMapper.updateCmsTask(updateTask);
    }
}