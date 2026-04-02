import request from '@/utils/request'

export function getUnreadCount() {
  return request({ url: '/system/notification/unreadCount', method: 'get' })
}

export function getNotificationList() {
  return request({ url: '/system/notification/list', method: 'get' })
}

export function markRead(noticeId) {
  return request({ url: '/system/notification/read/' + noticeId, method: 'put' })
}

export function markAllRead() {
  return request({ url: '/system/notification/readAll', method: 'put' })
}
