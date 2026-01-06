package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 附件明细对象 cms_file
 * 
 * @author ruoyi
 * @date 2025-12-14
 */
public class CmsFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long fileId;

    /** 关联合同ID */
    @Excel(name = "关联合同ID")
    private Long contractId;

    /** 原始文件名 */
    @Excel(name = "原始文件名")
    private String fileName;

    /** 文件存储路径 (相对路径) */
    @Excel(name = "文件存储路径 (相对路径)")
    private String filePath;

    /** 完整访问URL */
    @Excel(name = "完整访问URL")
    private String fileUrl;

    /** 文件后缀 (jpg, pdf, png) */
    @Excel(name = "文件后缀 (jpg, pdf, png)")
    private String fileSuffix;

    /** 文件大小 */
    @Excel(name = "文件大小")
    private Long fileSize;

    /** 分类（1合同扫描件 2支付凭证 3其他） */
    @Excel(name = "分类", readConverterExp = "1=合同扫描件,2=支付凭证,3=其他")
    private String fileCategory;

    public void setFileId(Long fileId) 
    {
        this.fileId = fileId;
    }

    public Long getFileId() 
    {
        return fileId;
    }
    public void setContractId(Long contractId) 
    {
        this.contractId = contractId;
    }

    public Long getContractId() 
    {
        return contractId;
    }
    public void setFileName(String fileName) 
    {
        this.fileName = fileName;
    }

    public String getFileName() 
    {
        return fileName;
    }
    public void setFilePath(String filePath) 
    {
        this.filePath = filePath;
    }

    public String getFilePath() 
    {
        return filePath;
    }
    public void setFileUrl(String fileUrl) 
    {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() 
    {
        return fileUrl;
    }
    public void setFileSuffix(String fileSuffix) 
    {
        this.fileSuffix = fileSuffix;
    }

    public String getFileSuffix() 
    {
        return fileSuffix;
    }
    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }
    public void setFileCategory(String fileCategory) 
    {
        this.fileCategory = fileCategory;
    }

    public String getFileCategory() 
    {
        return fileCategory;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("fileId", getFileId())
            .append("contractId", getContractId())
            .append("fileName", getFileName())
            .append("filePath", getFilePath())
            .append("fileUrl", getFileUrl())
            .append("fileSuffix", getFileSuffix())
            .append("fileSize", getFileSize())
            .append("fileCategory", getFileCategory())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
