package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识库-文档附件对象 cms_kb_attachment
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public class CmsKbAttachment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 附件ID */
    private Long id;

    /** 文档ID */
    private Long documentId;

    /** 版本ID（可空，表示与文档本身绑定） */
    private Long versionId;

    /** 文件ID */
    private Long fileId;

    /** 显示名称（覆盖原始文件名） */
    private String displayName;

    /** 排序号 */
    private Integer sortNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getSortNum() {
        return sortNum;
    }

    public void setSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }
}
