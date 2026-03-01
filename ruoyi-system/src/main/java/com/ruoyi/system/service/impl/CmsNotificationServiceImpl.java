package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.CmsNotification;
import com.ruoyi.system.mapper.CmsNotificationMapper;
import com.ruoyi.system.service.ICmsNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 通知提醒Service业务层处理
 *
 * @author ruoyi
 * @date 2026-03-01
 */
@Service
public class CmsNotificationServiceImpl implements ICmsNotificationService
{
    @Autowired
    private CmsNotificationMapper notificationMapper;

    /**
     * 创建通知（去重：相同用户+关联ID+类型的未读通知不重复创建）
     */
    @Override
    public void createNotification(Long userId, String title, String content, String type, Long relatedId)
    {
        // Avoid duplicate unread notifications for same user+contract+type
        int exists = notificationMapper.existsByUserAndRelated(userId, relatedId, type);
        if (exists > 0)
        {
            return;
        }
        CmsNotification notification = new CmsNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content != null ? content : "");
        notification.setNotificationType(type);
        notification.setRelatedId(relatedId);
        notificationMapper.insert(notification);
    }

    /**
     * 查询用户通知列表
     */
    @Override
    public List<CmsNotification> getNotificationsByUser(Long userId)
    {
        return notificationMapper.selectByUserId(userId);
    }

    /**
     * 获取用户未读通知数
     */
    @Override
    public int getUnreadCount(Long userId)
    {
        return notificationMapper.countUnread(userId);
    }

    /**
     * 标记通知为已读
     */
    @Override
    public int markRead(Long notificationId)
    {
        return notificationMapper.markRead(notificationId);
    }

    /**
     * 标记用户所有通知为已读
     */
    @Override
    public int markAllRead(Long userId)
    {
        return notificationMapper.markAllRead(userId);
    }
}
