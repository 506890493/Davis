package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.CmsNotification;
import java.util.List;

/**
 * 通知提醒Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-01
 */
public interface CmsNotificationMapper
{
    /**
     * 新增通知
     *
     * @param notification 通知信息
     * @return 结果
     */
    public int insert(CmsNotification notification);

    /**
     * 查询用户通知列表
     *
     * @param userId 用户ID
     * @return 通知列表
     */
    public List<CmsNotification> selectByUserId(Long userId);

    /**
     * 统计用户未读通知数
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    public int countUnread(Long userId);

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

    /**
     * 检查是否存在相同未读通知
     *
     * @param userId 用户ID
     * @param relatedId 关联业务ID
     * @param notificationType 通知类型
     * @return 数量
     */
    public int existsByUserAndRelated(Long userId, Long relatedId, String notificationType);
}
