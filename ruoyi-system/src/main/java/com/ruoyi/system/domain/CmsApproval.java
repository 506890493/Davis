package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 业务审批对象 cms_approval
 * 
 * @author ruoyi
 * @date 2025-11-29
 */
public class CmsApproval extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审批ID */
    private Long approvalId;

    /** 申请编号 */
    @Excel(name = "申请编号")
    private String applyNo;

    /** 申请人ID (sys_user) */
    @Excel(name = "申请人ID (sys_user)")
    private Long applicantId;

    /** 关联原合同ID */
    @Excel(name = "关联原合同ID")
    private Long contractId;

    /** 关联任务ID */
    @Excel(name = "关联任务ID")
    private Long taskId;

    /** 审批类型（字典：cms_approval_type 1新合同 2续费 3变更） */
    @Excel(name = "审批类型", readConverterExp = "字=典：cms_approval_type,1=新合同,2=续费,3=变更")
    private String approvalType;

    /** 申请内容快照 */
    @Excel(name = "申请内容快照")
    private String contentSnapshot;

    /** 审批状态（0待审批 1通过 2拒绝） */
    @Excel(name = "审批状态", readConverterExp = "0=待审批,1=通过,2=拒绝")
    private String status;

    /** 审批人ID */
    @Excel(name = "审批人ID")
    private Long approverId;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date approvalTime;

    /** 审批意见 */
    @Excel(name = "审批意见")
    private String approvalMsg;

    public void setApprovalId(Long approvalId) 
    {
        this.approvalId = approvalId;
    }

    public Long getApprovalId() 
    {
        return approvalId;
    }

    public void setApplyNo(String applyNo) 
    {
        this.applyNo = applyNo;
    }

    public String getApplyNo() 
    {
        return applyNo;
    }

    public void setApplicantId(Long applicantId) 
    {
        this.applicantId = applicantId;
    }

    public Long getApplicantId() 
    {
        return applicantId;
    }

    public void setContractId(Long contractId) 
    {
        this.contractId = contractId;
    }

    public Long getContractId() 
    {
        return contractId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setApprovalType(String approvalType) 
    {
        this.approvalType = approvalType;
    }

    public String getApprovalType() 
    {
        return approvalType;
    }

    public void setContentSnapshot(String contentSnapshot) 
    {
        this.contentSnapshot = contentSnapshot;
    }

    public String getContentSnapshot() 
    {
        return contentSnapshot;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setApproverId(Long approverId) 
    {
        this.approverId = approverId;
    }

    public Long getApproverId() 
    {
        return approverId;
    }

    public void setApprovalTime(Date approvalTime) 
    {
        this.approvalTime = approvalTime;
    }

    public Date getApprovalTime() 
    {
        return approvalTime;
    }

    public void setApprovalMsg(String approvalMsg) 
    {
        this.approvalMsg = approvalMsg;
    }

    public String getApprovalMsg() 
    {
        return approvalMsg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("approvalId", getApprovalId())
            .append("applyNo", getApplyNo())
            .append("applicantId", getApplicantId())
            .append("contractId", getContractId())
            .append("taskId", getTaskId())
            .append("approvalType", getApprovalType())
            .append("contentSnapshot", getContentSnapshot())
            .append("status", getStatus())
            .append("approverId", getApproverId())
            .append("approvalTime", getApprovalTime())
            .append("approvalMsg", getApprovalMsg())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
