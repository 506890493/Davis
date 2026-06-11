import request from '@/utils/request'

// 上传文件（图片/视频/附件）
export function uploadFile(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request({
    url: '/kb/file/upload',
    method: 'post',
    data: fd,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取文件原始访问 URL
export function rawFileUrl(id) {
  return process.env.VUE_APP_BASE_API + '/kb/file/raw/' + id
}
