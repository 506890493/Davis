import request from '@/utils/request'

// 查询知识库文档列表
export function listDocument(query) {
  return request({
    url: '/kb/document/list',
    method: 'get',
    params: query
  })
}

// 查询知识库文档详细
export function getDocument(id) {
  return request({
    url: '/kb/document/' + id,
    method: 'get'
  })
}

// 新增知识库文档
export function addDocument(data) {
  return request({
    url: '/kb/document',
    method: 'post',
    data: data
  })
}

// 修改知识库文档
export function updateDocument(data) {
  return request({
    url: '/kb/document',
    method: 'put',
    data: data
  })
}

// 删除知识库文档
export function delDocument(ids) {
  return request({
    url: '/kb/document/' + ids,
    method: 'delete'
  })
}

// 发布文档
export function publishDocument(data) {
  return request({
    url: '/kb/document/publish',
    method: 'put',
    data: data
  })
}

// 下线文档
export function offlineDocument(data) {
  return request({
    url: '/kb/document/offline',
    method: 'put',
    data: data
  })
}
