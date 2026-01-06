import request from '@/utils/request'

// 查询业务审批列表
export function listApproval(query) {
  return request({
    url: '/system/approval/list',
    method: 'get',
    params: query
  })
}

// 查询业务审批详细
export function getApproval(approvalId) {
  return request({
    url: '/system/approval/' + approvalId,
    method: 'get'
  })
}

// 新增业务审批
export function addApproval(data) {
  return request({
    url: '/system/approval',
    method: 'post',
    data: data
  })
}

// 修改业务审批
export function updateApproval(data) {
  return request({
    url: '/system/approval',
    method: 'put',
    data: data
  })
}

// 删除业务审批
export function delApproval(approvalId) {
  return request({
    url: '/system/approval/' + approvalId,
    method: 'delete'
  })
}
