import request from '@/utils/request'

// 查询文档版本历史
export function listVersions(docId) {
  return request({
    url: '/kb/version/' + docId,
    method: 'get'
  })
}

// 查看指定版本
export function getVersion(docId, ver) {
  return request({
    url: `/kb/version/${docId}/${ver}`,
    method: 'get'
  })
}

// 回滚到指定版本
export function rollbackVersion(docId, ver) {
  return request({
    url: `/kb/version/${docId}/${ver}/rollback`,
    method: 'post'
  })
}
