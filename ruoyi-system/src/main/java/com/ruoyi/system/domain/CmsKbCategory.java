package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识库-目录对象 cms_kb_category
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public class CmsKbCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 目录ID */
    private Long id;

    /** 父目录ID（0=根目录） */
    private Long parentId;

    /** 目录名称 */
    private String name;

    /** 图标 */
    private String icon;

    /** 排序号 */
    private Integer orderNum;

    /** 是否必学目录（0否 1是） */
    private Integer isRequired;

    /** 状态（0禁用 1启用） */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Integer isRequired) {
        this.isRequired = isRequired;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
