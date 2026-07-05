package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识库-文档对象 cms_kb_document
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public class CmsKbDocument extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 文档ID */
    private Long id;

    /** 所属目录ID */
    private Long categoryId;

    /** 文档标题 */
    private String title;

    /** 文档类型（如：article / video / link） */
    private String docType;

    /** 摘要 */
    private String summary;

    /** 标签，逗号分隔 */
    private String tags;

    /** 封面图文件ID */
    private Long coverImageId;

    /** 主文件ID */
    private Long primaryFileId;

    /** 是否必学（0否 1是） */
    private Integer isRequired;

    /** 是否置顶（0否 1是） */
    private Integer isPinned;

    /** 置顶时间（同置顶层级按此升序排，先置顶的排上） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pinnedAt;

    /** 状态（0草稿 1已发布 2已下线） */
    private Integer status;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishedTime;

    /** 浏览次数 */
    private Long viewCount;

    /** 当前版本号 */
    private Integer currentVersion;

    /** 删除标志（0代表存在 1代表删除） */
    private Integer delFlag;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deleteTime;

    /**
     * 当前版本正文（非持久化字段，运行时由 Service 层从 cms_kb_document_version 注入）
     * cms_kb_document 不保存富文本，正文存于 cms_kb_document_version。
     * 字段在 API 响应中以 "content" 暴露给前端（@JsonProperty 控制 JSON 字段名）。
     */
    @JsonProperty("content")
    private String newContent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
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

    public Long getCoverImageId() {
        return coverImageId;
    }

    public void setCoverImageId(Long coverImageId) {
        this.coverImageId = coverImageId;
    }

    public Long getPrimaryFileId() {
        return primaryFileId;
    }

    public void setPrimaryFileId(Long primaryFileId) {
        this.primaryFileId = primaryFileId;
    }

    public Integer getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Integer isRequired) {
        this.isRequired = isRequired;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }

    public Date getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(Date pinnedAt) {
        this.pinnedAt = pinnedAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(Date publishedTime) {
        this.publishedTime = publishedTime;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}
