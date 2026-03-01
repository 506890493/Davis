package com.ruoyi.web.controller.davis;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.CmsNotification;
import com.ruoyi.system.service.ICmsNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知提醒Controller
 *
 * @author ruoyi
 * @date 2026-03-01
 */
@RestController
@RequestMapping("/system/notification")
public class CmsNotificationController extends BaseController
{
    @Autowired
    private ICmsNotificationService notificationService;

    /**
     * 获取未读通知数量
     */
    @PreAuthorize("@ss.hasPermi('system:notification:list')")
    @GetMapping("/unreadCount")
    public AjaxResult unreadCount()
    {
        Long userId = SecurityUtils.getUserId();
        int count = notificationService.getUnreadCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return success(data);
    }

    /**
     * 获取通知列表
     */
    @PreAuthorize("@ss.hasPermi('system:notification:list')")
    @GetMapping("/list")
    public AjaxResult list()
    {
        Long userId = SecurityUtils.getUserId();
        List<CmsNotification> list = notificationService.getNotificationsByUser(userId);
        return success(list);
    }

    /**
     * 标记通知为已读
     */
    @PreAuthorize("@ss.hasPermi('system:notification:list')")
    @PutMapping("/read/{notificationId}")
    public AjaxResult markRead(@PathVariable Long notificationId)
    {
        return toAjax(notificationService.markRead(notificationId));
    }

    /**
     * 标记所有通知为已读
     */
    @PreAuthorize("@ss.hasPermi('system:notification:list')")
    @PutMapping("/readAll")
    public AjaxResult markAllRead()
    {
        Long userId = SecurityUtils.getUserId();
        return toAjax(notificationService.markAllRead(userId));
    }
}
