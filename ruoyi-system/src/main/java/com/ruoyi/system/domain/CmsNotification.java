package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 通知提醒实体
 *
 * @author ruoyi
 * @date 2026-03-01
 */
public class CmsNotification extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "通知ID")
    private Long notificationId;

    @Excel(name = "接收人ID")
    private Long userId;

    @Excel(name = "通知标题")
    private String title;

    @Excel(name = "通知内容")
    private String content;

    @Excel(name = "通知类型")
    private String notificationType;

    @Excel(name = "关联业务ID")
    private Long relatedId;

    @Excel(name = "是否已读")
    private String isRead;

    public Long getNotificationId()
    {
        return notificationId;
    }

    public void setNotificationId(Long notificationId)
    {
        this.notificationId = notificationId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getNotificationType()
    {
        return notificationType;
    }

    public void setNotificationType(String notificationType)
    {
        this.notificationType = notificationType;
    }

    public Long getRelatedId()
    {
        return relatedId;
    }

    public void setRelatedId(Long relatedId)
    {
        this.relatedId = relatedId;
    }

    public String getIsRead()
    {
        return isRead;
    }

    public void setIsRead(String isRead)
    {
        this.isRead = isRead;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("notificationId", getNotificationId())
            .append("userId", getUserId())
            .append("title", getTitle())
            .append("content", getContent())
            .append("notificationType", getNotificationType())
            .append("relatedId", getRelatedId())
            .append("isRead", getIsRead())
            .append("createTime", getCreateTime())
            .toString();
    }
}
