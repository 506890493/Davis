import request from '@/utils/request'

// 查询回收站列表
export function listRecycle(query) {
  return request({
    url: '/kb/recycle/list',
    method: 'get',
    params: query
  })
}

// 恢复回收站文档
export function restoreRecycle(ids) {
  return request({
    url: '/kb/recycle/restore',
    method: 'post',
    data: ids
  })
}

// 彻底删除回收站文档
export function purgeRecycle(ids) {
  return request({
    url: '/kb/recycle/purge',
    method: 'delete',
    data: ids
  })
}
