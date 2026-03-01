package com.ruoyi.system.service;

import com.ruoyi.system.domain.CmsNotification;
import java.util.List;

/**
 * 通知提醒Service接口
 *
 * @author ruoyi
 * @date 2026-03-01
 */
public interface ICmsNotificationService
{
    /**
     * 创建通知（去重：相同用户+关联ID+类型的未读通知不重复创建）
     *
     * @param userId 接收人用户ID
     * @param title 通知标题
     * @param content 通知内容
     * @param type 通知类型
     * @param relatedId 关联业务ID
     */
    public void createNotification(Long userId, String title, String content, String type, Long relatedId);

    /**
     * 查询用户通知列表
     *
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<CmsNotification> getNotificationsByUser(Long userId);

    /**
     * 获取用户未读通知数
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    public int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @return 结果
     */
    public int markRead(Long notificationId);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int markAllRead(Long userId);
}
