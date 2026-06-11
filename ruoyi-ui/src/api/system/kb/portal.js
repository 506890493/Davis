import request from '@/utils/request'

// 拉取目录树
export function treeCategory() {
  return request({
    url: '/kb/portal/tree',
    method: 'get'
  })
}

// 分类下文档分页
export function listPublished(query) {
  return request({
    url: '/kb/portal/list',
    method: 'get',
    params: query
  })
}

// 文档详情
export function getDetail(id) {
  return request({
    url: '/kb/portal/detail/' + id,
    method: 'get'
  })
}

// 新员工必读列表
export function listRequired(query) {
  return request({
    url: '/kb/portal/required',
    method: 'get',
    params: query
  })
}

// 搜索
export function searchDocs(query) {
  return request({
    url: '/kb/portal/search',
    method: 'get',
    params: query
  })
}
