import request from '@/utils/request'

// 查询合同管理列表
export function listContract(query) {
  return request({
    url: '/system/contract/list',
    method: 'get',
    params: query
  })
}

// 查询合同管理详细
export function getContract(contractId) {
  return request({
    url: '/system/contract/' + contractId,
    method: 'get'
  })
}

// 新增合同管理
export function addContract(data) {
  return request({
    url: '/system/contract',
    method: 'post',
    data: data
  })
}

// 修改合同管理
export function updateContract(data) {
  return request({
    url: '/system/contract',
    method: 'put',
    data: data
  })
}

// 删除合同管理
export function delContract(contractId) {
  return request({
    url: '/system/contract/' + contractId,
    method: 'delete'
  })
}
