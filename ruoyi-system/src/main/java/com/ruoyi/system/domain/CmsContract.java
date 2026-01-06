package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 合同管理对象 cms_contract
 * 
 * @author ruoyi
 * @date 2025-12-14
 */
public class CmsContract extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 合同ID */
    private Long contractId;

    /** 合同编号 */
    @Excel(name = "合同编号")
    private String contractCode;

    /** 合同/公司名称 */
    @Excel(name = "合同/公司名称")
    private String contractName;

    /** 合同类型（字典：cms_contract_type 1代账报税 2地址出售） */
    @Excel(name = "合同类型", readConverterExp = "字=典：cms_contract_type,1=代账报税,2=地址出售")
    private String contractType;

    /** 法人 */
    @Excel(name = "法人")
    private String legalPerson;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 联系邮箱 */
    @Excel(name = "联系邮箱")
    private String contactEmail;

    /** 收费标准 */
    @Excel(name = "收费标准")
    private BigDecimal amount;

    /** 付款周期（字典：cms_pay_cycle） */
    @Excel(name = "付款周期", readConverterExp = "字=典：cms_pay_cycle")
    private String paymentCycle;

    /** 收款日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "收款日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date paymentDate;

    /** 收款方式（字典：cms_pay_method 1微信 2支付宝 3公户） */
    @Excel(name = "收款方式", readConverterExp = "字=典：cms_pay_method,1=微信,2=支付宝,3=公户")
    private String paymentMethod;

    /** 合同开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合同开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 合同结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合同结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 税务类型（字典：cms_tax_type） */
    @Excel(name = "税务类型", readConverterExp = "字=典：cms_tax_type")
    private String taxType;

    /** 成立日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "成立日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date establishmentDate;

    /** 租赁地址 */
    @Excel(name = "租赁地址")
    private String rentalAddress;

    /** 是否已出租（0否 1是） */
    @Excel(name = "是否已出租", readConverterExp = "0=否,1=是")
    private String isRented;

    /** 利润 */
    @Excel(name = "利润")
    private BigDecimal profit;

    /** 归属人ID (关联sys_user) */
    @Excel(name = "归属人ID (关联sys_user)")
    private Long ownerId;

    /** 归属部门ID (用于数据权限) */
    @Excel(name = "归属部门ID (用于数据权限)")
    private Long deptId;

    /** 状态（字典：cms_contract_status 0正常 1停用 2过期） */
    @Excel(name = "状态", readConverterExp = "字=典：cms_contract_status,0=正常,1=停用,2=过期")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 催交状态（字典：cms_reminder_status） */
    @Excel(name = "催交状态", readConverterExp = "字=典：cms_reminder_status")
    private String reminderStatus;

    /** 附件列表（JSON格式存储，包含文件ID和路径，用于列表快速展示） */
    @Excel(name = "附件列表", readConverterExp = "J=SON格式存储，包含文件ID和路径，用于列表快速展示")
    private String annex;

    /** 附件明细信息 */
    private List<CmsFile> cmsFileList;

    public void setContractId(Long contractId) 
    {
        this.contractId = contractId;
    }

    public Long getContractId() 
    {
        return contractId;
    }

    public void setContractCode(String contractCode) 
    {
        this.contractCode = contractCode;
    }

    public String getContractCode() 
    {
        return contractCode;
    }

    public void setContractName(String contractName) 
    {
        this.contractName = contractName;
    }

    public String getContractName() 
    {
        return contractName;
    }

    public void setContractType(String contractType) 
    {
        this.contractType = contractType;
    }

    public String getContractType() 
    {
        return contractType;
    }

    public void setLegalPerson(String legalPerson) 
    {
        this.legalPerson = legalPerson;
    }

    public String getLegalPerson() 
    {
        return legalPerson;
    }

    public void setContactPerson(String contactPerson) 
    {
        this.contactPerson = contactPerson;
    }

    public String getContactPerson() 
    {
        return contactPerson;
    }

    public void setContactPhone(String contactPhone) 
    {
        this.contactPhone = contactPhone;
    }

    public String getContactPhone() 
    {
        return contactPhone;
    }

    public void setContactEmail(String contactEmail) 
    {
        this.contactEmail = contactEmail;
    }

    public String getContactEmail() 
    {
        return contactEmail;
    }

    public void setAmount(BigDecimal amount) 
    {
        this.amount = amount;
    }

    public BigDecimal getAmount() 
    {
        return amount;
    }

    public void setPaymentCycle(String paymentCycle) 
    {
        this.paymentCycle = paymentCycle;
    }

    public String getPaymentCycle() 
    {
        return paymentCycle;
    }

    public void setPaymentDate(Date paymentDate) 
    {
        this.paymentDate = paymentDate;
    }

    public Date getPaymentDate() 
    {
        return paymentDate;
    }

    public void setPaymentMethod(String paymentMethod) 
    {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() 
    {
        return paymentMethod;
    }

    public void setStartDate(Date startDate) 
    {
        this.startDate = startDate;
    }

    public Date getStartDate() 
    {
        return startDate;
    }

    public void setEndDate(Date endDate) 
    {
        this.endDate = endDate;
    }

    public Date getEndDate() 
    {
        return endDate;
    }

    public void setTaxType(String taxType) 
    {
        this.taxType = taxType;
    }

    public String getTaxType() 
    {
        return taxType;
    }

    public void setEstablishmentDate(Date establishmentDate) 
    {
        this.establishmentDate = establishmentDate;
    }

    public Date getEstablishmentDate() 
    {
        return establishmentDate;
    }

    public void setRentalAddress(String rentalAddress) 
    {
        this.rentalAddress = rentalAddress;
    }

    public String getRentalAddress() 
    {
        return rentalAddress;
    }

    public void setIsRented(String isRented) 
    {
        this.isRented = isRented;
    }

    public String getIsRented() 
    {
        return isRented;
    }

    public void setProfit(BigDecimal profit) 
    {
        this.profit = profit;
    }

    public BigDecimal getProfit() 
    {
        return profit;
    }

    public void setOwnerId(Long ownerId) 
    {
        this.ownerId = ownerId;
    }

    public Long getOwnerId() 
    {
        return ownerId;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
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

    public void setReminderStatus(String reminderStatus) 
    {
        this.reminderStatus = reminderStatus;
    }

    public String getReminderStatus() 
    {
        return reminderStatus;
    }

    public void setAnnex(String annex) 
    {
        this.annex = annex;
    }

    public String getAnnex() 
    {
        return annex;
    }

    public List<CmsFile> getCmsFileList()
    {
        return cmsFileList;
    }

    public void setCmsFileList(List<CmsFile> cmsFileList)
    {
        this.cmsFileList = cmsFileList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("contractId", getContractId())
            .append("contractCode", getContractCode())
            .append("contractName", getContractName())
            .append("contractType", getContractType())
            .append("legalPerson", getLegalPerson())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("contactEmail", getContactEmail())
            .append("amount", getAmount())
            .append("paymentCycle", getPaymentCycle())
            .append("paymentDate", getPaymentDate())
            .append("paymentMethod", getPaymentMethod())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("taxType", getTaxType())
            .append("establishmentDate", getEstablishmentDate())
            .append("rentalAddress", getRentalAddress())
            .append("isRented", getIsRented())
            .append("profit", getProfit())
            .append("ownerId", getOwnerId())
            .append("deptId", getDeptId())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("reminderStatus", getReminderStatus())
            .append("annex", getAnnex())
            .append("cmsFileList", getCmsFileList())
            .toString();
    }
}
