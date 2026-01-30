import request from '@/utils/request'

// 获取Dashboard统计数据
export function getDashboardStats() {
  return request({
    url: '/system/dashboard/stats',
    method: 'get'
  })
}
