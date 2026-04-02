package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class CmsTaskLog extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long logId;
    private Long taskId;
    private Long operatorId;
    private String operatorName;
    private String actionType;
    private String beforeStatus;
    private String afterStatus;
    private String remark;
    private java.math.BigDecimal amountBefore;
    private java.math.BigDecimal amountAfter;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getBeforeStatus() { return beforeStatus; }
    public void setBeforeStatus(String beforeStatus) { this.beforeStatus = beforeStatus; }
    public String getAfterStatus() { return afterStatus; }
    public void setAfterStatus(String afterStatus) { this.afterStatus = afterStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public java.math.BigDecimal getAmountBefore() { return amountBefore; }
    public void setAmountBefore(java.math.BigDecimal amountBefore) { this.amountBefore = amountBefore; }
    public java.math.BigDecimal getAmountAfter() { return amountAfter; }
    public void setAmountAfter(java.math.BigDecimal amountAfter) { this.amountAfter = amountAfter; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
