import request from '@/utils/request'

export function getUnreadCount() {
  return request({ url: '/system/notification/unreadCount', method: 'get' })
}

export function getNotificationList() {
  return request({ url: '/system/notification/list', method: 'get' })
}

export function markRead(notificationId) {
  return request({ url: '/system/notification/read/' + notificationId, method: 'put' })
}

export function markAllRead() {
  return request({ url: '/system/notification/readAll', method: 'put' })
}
