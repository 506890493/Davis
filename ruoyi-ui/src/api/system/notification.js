import request from '@/utils/request'

export function getUnreadCount() {
  return request({ url: '/system/notice/unreadCount', method: 'get' })
}

export function getNotificationList() {
  return request({ url: '/system/notice/list', method: 'get' })
}

export function markRead(noticeId) {
  return request({ url: '/system/notice/read/' + noticeId, method: 'put' })
}

export function markAllRead() {
  return request({ url: '/system/notice/readAll', method: 'put' })
}
