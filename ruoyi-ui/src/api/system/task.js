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