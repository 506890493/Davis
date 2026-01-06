package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 任务管理对象 cms_task
 * 
 * @author ruoyi
 * @date 2025-11-29
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

    /** 任务类型（字典：cms_task_type） */
    @Excel(name = "任务类型", readConverterExp = "字=典：cms_task_type")
    private String taskType;

    /** 优先级（字典：cms_task_priority 1高 2中 3低） */
    @Excel(name = "优先级", readConverterExp = "字=典：cms_task_priority,1=高,2=中,3=低")
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
    @Excel(name = "任务状态", readConverterExp = "字=典：cms_task_status,0=待处理,1=进行中,2=待审批,3=已退回,4=已完成")
    private String status;

    /** 删除标志 */
    private String delFlag;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskTitle", getTaskTitle())
            .append("contractId", getContractId())
            .append("taskType", getTaskType())
            .append("priority", getPriority())
            .append("originalAmount", getOriginalAmount())
            .append("currentAmount", getCurrentAmount())
            .append("assignedTo", getAssignedTo())
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
