import request from '@/utils/request'

// 查询知识库分类列表
export function listCategory(query) {
  return request({
    url: '/kb/category/list',
    method: 'get',
    params: query
  })
}

// 查询知识库分类详细
export function getCategory(id) {
  return request({
    url: '/kb/category/' + id,
    method: 'get'
  })
}

// 新增知识库分类
export function addCategory(data) {
  return request({
    url: '/kb/category',
    method: 'post',
    data: data
  })
}

// 修改知识库分类
export function updateCategory(data) {
  return request({
    url: '/kb/category',
    method: 'put',
    data: data
  })
}

// 调整分类排序
export function orderCategory(data) {
  return request({
    url: '/kb/category/order',
    method: 'put',
    data: data
  })
}

// 删除知识库分类
export function delCategory(ids) {
  return request({
    url: '/kb/category/' + ids,
    method: 'delete'
  })
}
