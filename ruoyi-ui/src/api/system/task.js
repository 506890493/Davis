import request from '@/utils/request'

// 查询任务管理列表
export function listTask(query) {
  return request({
    url: '/system/task/list',
    method: 'get',
    params: query
  })
}

// 查询任务管理详细
export function getTask(taskId) {
  return request({
    url: '/system/task/' + taskId,
    method: 'get'
  })
}

// 新增任务管理
export function addTask(data) {
  return request({
    url: '/system/task',
    method: 'post',
    data: data
  })
}

// 修改任务管理
export function updateTask(data) {
  return request({
    url: '/system/task',
    method: 'put',
    data: data
  })
}

// 删除任务管理
export function delTask(taskId) {
  return request({
    url: '/system/task/' + taskId,
    method: 'delete'
  })
}

// 创建催收任务
export function createCollectionTask(data) {
  return request({
    url: '/system/contract/collection',
    method: 'post',
    data: data
  })
}

// 完成催收任务
export function completeCollectionTask(data) {
  return request({
    url: '/system/task/completeCollection',
    method: 'post',
    data: data
  })
}
// 获取可分配会计列表
export function getAssignableUsers() {
  return request({
    url: '/system/task/assignableUsers',
    method: 'get'
  })
}
// 退回(讲价)
export function returnToAdmin(data) {
  return request({ url: '/cms/task/returnToAdmin', method: 'post', data: data })
}

// 重新派发
export function redispatch(data) {
  return request({ url: '/cms/task/redispatch', method: 'post', data: data })
}

// 申请终止
export function requestTermination(data) {
  return request({ url: '/cms/task/requestTermination', method: 'post', data: data })
}

// 确认终止
export function confirmTermination(params) {
  return request({ url: '/cms/task/confirmTermination', method: 'post', params: params })
}

// 完成续签
export function completeRenewal(data) {
  return request({ url: '/cms/task/completeRenewal', method: 'post', data: data })
}

