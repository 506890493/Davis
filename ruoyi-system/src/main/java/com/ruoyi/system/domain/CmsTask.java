package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 任务管理对象 cms_task
 * 
 * @author ruoyi
 * @date 2026-01-17
 */
public class CmsTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务标题 */
    @Excel(name = "任务标题")
    private String taskTitle;

    /** 关联同ID */
    @Excel(name = "关联同ID")
    private Long contractId;
    
    /** 关联发起的原合同 */
    @Excel(name = "关联发起的原合同")
    private Long sourceContractId;

    /** 关联续签后的新合同 */
    @Excel(name = "关联续签后的新合同")
    private Long targetContractId;

    /** 任务类型（字典：cms_task_type） */
    @Excel(name = "任务类型", dictType = "cms_task_type")
    private String taskType;

    /** 优先级（字典：cms_task_priority 1高 2中 3低） */
    @Excel(name = "优先级", dictType = "cms_task_priority")
    private String priority;

    /** 原金额 */
    @Excel(name = "原金额")
    private BigDecimal originalAmount;

    /** 当前协商金额 */
    @Excel(name = "当前协商金额")
    private BigDecimal currentAmount;

    /** 执行人ID (关联sys_user) */
    @Excel(name = "执行人ID (关联sys_user)")
    private Long assignedTo;

    /** 截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "截止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date deadline;

    /** 任务状态（字典：cms_task_status 0待处理 1进行中 2待审批 3已退回 4已完成） */
    @Excel(name = "任务状态", dictType = "cms_task_status")
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 执行人名称 */
    private String assignedToName;

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }
    public void setTaskTitle(String taskTitle) 
    {
        this.taskTitle = taskTitle;
    }

    public String getTaskTitle() 
    {
        return taskTitle;
    }
    public void setContractId(Long contractId) 
    {
        this.contractId = contractId;
    }

    public Long getContractId() 
    {
        return contractId;
    }
    public void setSourceContractId(Long sourceContractId)
    {
        this.sourceContractId = sourceContractId;
    }
    public Long getSourceContractId()
    {
        return sourceContractId;
    }
    public void setTargetContractId(Long targetContractId)
    {
        this.targetContractId = targetContractId;
    }
    public Long getTargetContractId()
    {
        return targetContractId;
    }

    public void setTaskType(String taskType) 
    {
        this.taskType = taskType;
    }

    public String getTaskType() 
    {
        return taskType;
    }
    public void setPriority(String priority) 
    {
        this.priority = priority;
    }

    public String getPriority() 
    {
        return priority;
    }
    public void setOriginalAmount(BigDecimal originalAmount) 
    {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getOriginalAmount() 
    {
        return originalAmount;
    }
    public void setCurrentAmount(BigDecimal currentAmount) 
    {
        this.currentAmount = currentAmount;
    }

    public BigDecimal getCurrentAmount() 
    {
        return currentAmount;
    }
    public void setAssignedTo(Long assignedTo) 
    {
        this.assignedTo = assignedTo;
    }

    public Long getAssignedTo() 
    {
        return assignedTo;
    }
    public void setDeadline(Date deadline) 
    {
        this.deadline = deadline;
    }

    public Date getDeadline() 
    {
        return deadline;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public void setAssignedToName(String assignedToName)
    {
        this.assignedToName = assignedToName;
    }

    public String getAssignedToName()
    {
        return assignedToName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskTitle", getTaskTitle())
            .append("contractId", getContractId())
            .append("sourceContractId", getSourceContractId())
            .append("targetContractId", getTargetContractId())
            .append("taskType", getTaskType())
            .append("priority", getPriority())
            .append("originalAmount", getOriginalAmount())
            .append("currentAmount", getCurrentAmount())
            .append("assignedTo", getAssignedTo())
            .append("assignedToName", getAssignedToName())
            .append("deadline", getDeadline())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}