import request from '@/utils/request'

// 获取总账汇总数据
export function getLedgerSummary(params) {
  return request({ url: '/system/ledger/summary', method: 'get', params })
}

// 按人员统计总账数据
export function getLedgerByPerson(params) {
  return request({ url: '/system/ledger/byPerson', method: 'get', params })
}

// 获取年度趋势数据
export function getLedgerTrend(params) {
  return request({ url: '/system/ledger/trend', method: 'get', params })
}

// 导出总账报表数据
export function exportLedger(params) {
  return request({ url: '/system/ledger/export', method: 'post', params, responseType: 'blob' })
}
