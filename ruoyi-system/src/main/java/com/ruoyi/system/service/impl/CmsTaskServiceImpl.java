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
import com.ruoyi.system.domain.CmsTaskLog;
import com.ruoyi.system.service.ICmsTaskLogService;



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

    @Autowired
    private ICmsTaskLogService cmsTaskLogService;

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
        // 幂等性检查：检查是否存在未完成的同类型任务
        CmsTask queryTask = new CmsTask();
        queryTask.setContractId(cmsTask.getContractId());
        queryTask.setTaskType(cmsTask.getTaskType());

        List<CmsTask> existingTasks = cmsTaskMapper.selectCmsTaskList(queryTask);
        for (CmsTask task : existingTasks) {
            // 状态 0待处理 1进行中 2待审批
            if ("0".equals(task.getStatus()) || "1".equals(task.getStatus()) || "2".equals(task.getStatus())) {
                throw new ServiceException("该合同已有进行中的同类型任务，请勿重复派发");
            }
        }

        cmsTask.setCreateTime(DateUtils.getNowDate());
        cmsTask.setCreateBy(SecurityUtils.getUsername());
        cmsTask.setUpdateBy(SecurityUtils.getUsername());
        
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

        int rows = cmsTaskMapper.insertCmsTask(cmsTask);

        // 记录任务日志（必须在 insert 之后，因为 taskId 由数据库自增生成）
        recordTaskLog(cmsTask.getTaskId(), "0", null, cmsTask.getStatus(), "创建任务");

        return rows;
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
        CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(cmsTask.getTaskId());
        String oldStatus = existingTask != null ? existingTask.getStatus() : null;
        
        cmsTask.setUpdateTime(DateUtils.getNowDate());
        cmsTask.setUpdateBy(SecurityUtils.getUsername());
        
        // 当任务状态更新为进行中(1)时，同步更新原合同的催收状态
        if ("1".equals(cmsTask.getStatus())) {
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
        
        int result = cmsTaskMapper.updateCmsTask(cmsTask);
        
        // 记录状态变更日志
        if (result > 0 && cmsTask.getStatus() != null && !cmsTask.getStatus().equals(oldStatus)) {
            String newStatus = cmsTask.getStatus();
            if ("1".equals(newStatus)) {
                recordTaskLog(cmsTask.getTaskId(), "1", oldStatus, newStatus, "开始处理任务");
            } else if ("3".equals(newStatus)) {
                recordTaskLog(cmsTask.getTaskId(), "3", oldStatus, newStatus, "终止任务");
            } else if ("4".equals(newStatus)) {
                recordTaskLog(cmsTask.getTaskId(), "2", oldStatus, newStatus, "完成任务");
            } else if ("5".equals(newStatus)) {
                recordTaskLog(cmsTask.getTaskId(), "5", oldStatus, newStatus, "任务被拒绝");
            }
        }
        
        return result;
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
     * 完成催收任务并生成新合同
     *
     * @param taskId 任务ID
     * @param newContract 新合同信息（含新金额、新期限等覆盖值）
     * @return 结果
     */
    @Override
    public int completeCollectionTask(Long taskId, CmsContract newContract) {
        CmsTask task = cmsTaskMapper.selectCmsTaskByTaskId(taskId);
        String oldStatus = task.getStatus();
        task.setStatus("4"); // 4 for completed
        cmsTaskMapper.updateCmsTask(task);
        recordTaskLog(taskId, "2", oldStatus, "4", "完成催收任务并生成新合同");

        // sourceContractId 空值保护：催收任务可能只有 contractId
        Long sourceContractId = task.getSourceContractId();
        if (sourceContractId == null) {
            sourceContractId = task.getContractId();
        }

        CmsContract sourceContract = cmsContractService.selectCmsContractByContractId(sourceContractId);
        if (sourceContract == null) {
            throw new ServiceException("原合同不存在，无法生成新合同");
        }

        CmsContract targetContract = buildNewContractFromSource(task, sourceContract, newContract);
        cmsContractService.insertCmsContract(targetContract);

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
        CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
        if (existingTask == null) {
            throw new ServiceException("任务不存在");
        }
        
        Long currentUserId = SecurityUtils.getUserId();
        if (!currentUserId.equals(existingTask.getAssignedTo())) {
            throw new ServiceException("只能退回分配给自己的任务");
        }
        
        String oldStatus = existingTask.getStatus();
        if (!"0".equals(oldStatus) && !"1".equals(oldStatus)) {
            throw new ServiceException("只能退回待处理或进行中的任务");
        }
        
        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setStatus("2");
        updateTask.setRemark(task.getRemark());
        updateTask.setCurrentAmount(task.getCurrentAmount());
        updateTask.setAdjustAmount(task.getAdjustAmount());
        updateTask.setAfterAmount(task.getAfterAmount());
        updateTask.setAttachment(task.getAttachment());
        updateTask.setUpdateTime(DateUtils.getNowDate());
        updateTask.setUpdateBy(SecurityUtils.getUsername());
        int result = cmsTaskMapper.updateCmsTask(updateTask);
        if (result > 0) {
            recordTaskLog(task.getTaskId(), "PRICE_SUBMIT", oldStatus, "2", 
                "提交协商价格: 原金额" + existingTask.getOriginalAmount() + "→新金额" + task.getCurrentAmount() + ", 备注: " + task.getRemark(),
                existingTask.getOriginalAmount(), task.getCurrentAmount());
            
            List<SysUser> admins = findUsersByRoleKey("admin");
            for (SysUser admin : admins) {
                sendNotification(admin.getUserId(), "协商价格待审批",
                    "您有新的协商价格待审批：任务【" + existingTask.getTaskTitle() + "】，原金额" + existingTask.getOriginalAmount() + "→新金额" + task.getCurrentAmount());
            }
        }
        return result;
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
        CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
        String oldStatus = existingTask != null ? existingTask.getStatus() : null;
        
        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setCurrentAmount(task.getCurrentAmount());
        updateTask.setAssignedTo(task.getAssignedTo());
        updateTask.setDeadline(task.getDeadline());
        updateTask.setStatus("0"); // 0待处理
        updateTask.setUpdateTime(DateUtils.getNowDate());
        updateTask.setUpdateBy(SecurityUtils.getUsername());
        int result = cmsTaskMapper.updateCmsTask(updateTask);
        if (result > 0) {
            recordTaskLog(task.getTaskId(), "PRICE_APPROVE", oldStatus, "0", 
                "同意协商价格: 新金额" + task.getCurrentAmount() + ", 备注: " + (task.getRemark() != null ? task.getRemark() : ""),
                existingTask.getCurrentAmount(), task.getCurrentAmount());
            
            // 通知会计
            if (existingTask.getAssignedTo() != null) {
                sendNotification(existingTask.getAssignedTo(), "协商价格已通过", 
                    "您的协商价格已通过：新金额【" + task.getCurrentAmount() + "】");
            }
        }
        return result;
    }

    /**
     * 拒绝协商价格
     */
    @Override
    @Transactional
    public int rejectPrice(CmsTask task)
    {
        CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
        String oldStatus = existingTask != null ? existingTask.getStatus() : null;
        
        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setStatus("0"); // 退回给会计，可重新协商
        updateTask.setRemark(task.getRemark());
        updateTask.setUpdateTime(DateUtils.getNowDate());
        updateTask.setUpdateBy(SecurityUtils.getUsername());
        
        int result = cmsTaskMapper.updateCmsTask(updateTask);
        if (result > 0) {
            recordTaskLog(task.getTaskId(), "PRICE_REJECT", oldStatus, "0", 
                "拒绝协商价格, 原因: " + task.getRemark());
            
            // 通知会计
            if (existingTask.getAssignedTo() != null) {
                sendNotification(existingTask.getAssignedTo(), "协商价格已拒绝", 
                    "您的协商价格已拒绝，原因：" + task.getRemark());
            }
        }
        return result;
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
        CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
        String oldStatus = existingTask != null ? existingTask.getStatus() : null;
        String originalTaskType = existingTask != null ? existingTask.getTaskType() : null;

        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setStatus("2"); // 2待审批
        updateTask.setTaskType("3"); // 3终止
        updateTask.setRemark(task.getRemark());
        updateTask.setUpdateTime(DateUtils.getNowDate());
        updateTask.setUpdateBy(SecurityUtils.getUsername());
        int result = cmsTaskMapper.updateCmsTask(updateTask);
        if (result > 0) {
            recordTaskLog(task.getTaskId(), "3", oldStatus, "2", "发起终止合作请求|原始任务类型:" + originalTaskType + "|" + task.getRemark());

            // 通知所有管理员/经理
            List<SysUser> admins = findUsersByRoleKey("admin");
            for (SysUser admin : admins) {
                sendNotification(admin.getUserId(), "终止合作待审批",
                    "您有新的终止合作待审批：任务【" + existingTask.getTaskTitle() + "】，原因：" + task.getRemark());
            }
        }
        return result;
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
        
        String oldStatus = task.getStatus();

        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(taskId);
        updateTask.setUpdateTime(DateUtils.getNowDate());
        updateTask.setUpdateBy(SecurityUtils.getUsername());

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
            // 从任务日志中恢复原始任务类型
            String originalTaskType = getOriginalTaskTypeFromLog(taskId);
            if (originalTaskType != null) {
                updateTask.setTaskType(originalTaskType);
            }
        }
        
        int result = cmsTaskMapper.updateCmsTask(updateTask);
        if (result > 0) {
            String newStatus = approved ? "4" : "3";
            recordTaskLog(taskId, "3", oldStatus, newStatus, approved ? "同意终止合作" : "拒绝终止合作");
        }
        return result;
    }

    /**
     * 会计完成续签
     *
     * @return 结果
     */
    @Override
    @Transactional
    public int completeRenewal(Long taskId, CmsContract newContract, boolean generateContract)
    {
        CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(taskId);
        String oldStatus = existingTask != null ? existingTask.getStatus() : null;

        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(taskId);
        updateTask.setStatus("4"); // 4已完成
        updateTask.setUpdateTime(DateUtils.getNowDate());
        updateTask.setUpdateBy(SecurityUtils.getUsername());
        int result = cmsTaskMapper.updateCmsTask(updateTask);
        if (result > 0) {
            recordTaskLog(taskId, "2", oldStatus, "4", "完成续签");

            if (generateContract && newContract != null) {
                // sourceContractId 空值保护
                Long sourceContractId = existingTask.getSourceContractId();
                if (sourceContractId == null) {
                    sourceContractId = existingTask.getContractId();
                }

                CmsContract sourceContract = cmsContractService.selectCmsContractByContractId(sourceContractId);
                if (sourceContract == null) {
                    throw new ServiceException("原合同不存在，无法生成新合同");
                }

                CmsContract targetContract = buildNewContractFromSource(existingTask, sourceContract, newContract);
                cmsContractService.insertCmsContract(targetContract);

                updateTask.setTargetContractId(targetContract.getContractId());
                cmsTaskMapper.updateCmsTask(updateTask);
            }
        }
        return result;
    }

/**
     * 确认收款（催收任务完成）
     *
     * @param task 任务信息（包含taskId, actualAmount, receiveRemark）
     * @return 结果
     */
    @Override
    @Transactional
    public int confirmPayment(CmsTask task)
    {
        CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
        if (existingTask == null) {
            throw new ServiceException("任务不存在");
        }
        
        Long currentUserId = SecurityUtils.getUserId();
        if (!currentUserId.equals(existingTask.getAssignedTo())) {
            throw new ServiceException("只能确认分配给自己的任务");
        }
        
        String oldStatus = existingTask.getStatus();
        if (!"0".equals(oldStatus) && !"1".equals(oldStatus)) {
            throw new ServiceException("只能确认待处理或进行中的任务");
        }

        CmsTask updateTask = new CmsTask();
        updateTask.setTaskId(task.getTaskId());
        updateTask.setActualAmount(task.getActualAmount());
        updateTask.setReceiveRemark(task.getReceiveRemark());
        updateTask.setStatus("4");
        updateTask.setUpdateTime(DateUtils.getNowDate());
        updateTask.setUpdateBy(SecurityUtils.getUsername());
        
        int result = cmsTaskMapper.updateCmsTask(updateTask);

        Long contractIdToUpdate = existingTask.getSourceContractId();
        if (contractIdToUpdate == null) {
            contractIdToUpdate = existingTask.getContractId();
        }
        
        if (contractIdToUpdate != null) {
            CmsContract contract = cmsContractService.selectCmsContractByContractId(contractIdToUpdate);
            if (contract != null) {
                contract.setActualAmount(task.getActualAmount());
                contract.setReminderStatus("3");
                cmsContractService.updateCmsContract(contract);
            }
        }

        if (result > 0) {
            recordTaskLog(task.getTaskId(), "2", oldStatus, "4", "确认收款: " + task.getActualAmount());
        }
        
        return result;
    }
    
    private void recordTaskLog(Long taskId, String actionType, String beforeStatus, String afterStatus, String remark) {
        recordTaskLog(taskId, actionType, beforeStatus, afterStatus, remark, null, null);
    }
    
    private void recordTaskLog(Long taskId, String actionType, String beforeStatus, String afterStatus, String remark, java.math.BigDecimal amountBefore, java.math.BigDecimal amountAfter) {
        CmsTaskLog log = new CmsTaskLog();
        log.setTaskId(taskId);
        log.setOperatorId(SecurityUtils.getUserId());
        log.setOperatorName(SecurityUtils.getUsername());
        log.setActionType(actionType);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setRemark(remark);
        log.setAmountBefore(amountBefore);
        log.setAmountAfter(amountAfter);
        log.setCreateTime(DateUtils.getNowDate());
        cmsTaskLogService.insertCmsTaskLog(log);
    }
    
    /**
     * 从原合同构建新合同，应用 newContract 中的覆盖值
     */
    private CmsContract buildNewContractFromSource(CmsTask task, CmsContract sourceContract, CmsContract newContract) {
        CmsContract target = new CmsContract();
        target.setCustomerId(sourceContract.getCustomerId());
        target.setCustomerName(sourceContract.getCustomerName());
        target.setContractName(sourceContract.getContractName());
        target.setContractType(sourceContract.getContractType());
        target.setLegalPerson(sourceContract.getLegalPerson());
        target.setContactPerson(sourceContract.getContactPerson());
        target.setContactPhone(sourceContract.getContactPhone());
        target.setContactEmail(sourceContract.getContactEmail());
        target.setPaymentCycle(sourceContract.getPaymentCycle());
        target.setPaymentMethod(sourceContract.getPaymentMethod());
        target.setTaxType(sourceContract.getTaxType());
        target.setRentalAddress(sourceContract.getRentalAddress());
        target.setOwnerId(sourceContract.getOwnerId());
        target.setDeptId(sourceContract.getDeptId());

        // 应用 newContract 覆盖值
        if (newContract != null) {
            if (newContract.getAmount() != null) {
                target.setAmount(newContract.getAmount());
            } else {
                target.setAmount(sourceContract.getAmount());
            }
            if (newContract.getStartDate() != null) {
                target.setStartDate(newContract.getStartDate());
            }
            if (newContract.getEndDate() != null) {
                target.setEndDate(newContract.getEndDate());
            }
            if (newContract.getContractName() != null) {
                target.setContractName(newContract.getContractName());
            }
        }

        target.setParentId(sourceContract.getContractId());
        target.setAuditStatus("0"); // 待审批
        return target;
    }

    /**
     * 按角色标识查找用户列表
     */
    private List<SysUser> findUsersByRoleKey(String roleKey) {
        SysRole queryRole = new SysRole();
        List<SysRole> allRoles = sysRoleService.selectRoleList(queryRole);
        Long roleId = null;
        for (SysRole role : allRoles) {
            if (roleKey.equals(role.getRoleKey())) {
                roleId = role.getRoleId();
                break;
            }
        }
        if (roleId == null) {
            return new ArrayList<>();
        }
        SysUser queryUser = new SysUser();
        queryUser.setRoleId(roleId);
        return sysUserService.selectAllocatedList(queryUser);
    }

    /**
     * 从任务日志中解析原始任务类型（用于终止拒绝后恢复）
     */
    private String getOriginalTaskTypeFromLog(Long taskId) {
        CmsTaskLog queryLog = new CmsTaskLog();
        queryLog.setTaskId(taskId);
        List<CmsTaskLog> logs = cmsTaskLogService.selectCmsTaskLogList(queryLog);
        for (CmsTaskLog log : logs) {
            if ("3".equals(log.getActionType()) && log.getRemark() != null && log.getRemark().contains("原始任务类型:")) {
                String[] parts = log.getRemark().split("\\|");
                for (String part : parts) {
                    if (part.startsWith("原始任务类型:")) {
                        return part.substring("原始任务类型:".length());
                    }
                }
            }
        }
        return null;
    }

    /**
     * 发送站内通知
     */
    private void sendNotification(Long receiverId, String title, String content) {
        if (receiverId == null) return;
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(title);
        notice.setNoticeType("2");
        notice.setNoticeContent(content);
        notice.setStatus("0");
        notice.setCreateBy(String.valueOf(receiverId));
        noticeService.insertNotice(notice);
    }
}