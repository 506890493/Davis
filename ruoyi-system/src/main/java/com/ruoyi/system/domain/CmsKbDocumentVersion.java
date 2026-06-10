package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识库-文档版本对象 cms_kb_document_version
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public class CmsKbDocumentVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    private Long id;

    /** 文档ID */
    private Long documentId;

    /** 版本号 */
    private Integer versionNo;

    /** 该版本的标题 */
    private String title;

    /** 富文本正文 */
    private String content;

    /** 主文件ID */
    private Long primaryFileId;

    /** 摘要 */
    private String summary;

    /** 标签 */
    private String tags;

    /** 保存原因（auto / manual / rollback） */
    private String saveReason;

    /** 是否当前版本（0否 1是） */
    private Integer isCurrent;

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

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getPrimaryFileId() {
        return primaryFileId;
    }

    public void setPrimaryFileId(Long primaryFileId) {
        this.primaryFileId = primaryFileId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSaveReason() {
        return saveReason;
    }

    public void setSaveReason(String saveReason) {
        this.saveReason = saveReason;
    }

    public Integer getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Integer isCurrent) {
        this.isCurrent = isCurrent;
    }
}
