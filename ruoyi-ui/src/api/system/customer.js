import request from '@/utils/request'

export function listCustomer(query) {
  return request({
    url: '/system/customer/list',
    method: 'get',
    params: query
  })
}

export function getCustomer(customerId) {
  return request({
    url: '/system/customer/' + customerId,
    method: 'get'
  })
}

export function getCustomerDetail(customerId) {
  return request({
    url: '/system/customer/detail/' + customerId,
    method: 'get'
  })
}

export function addCustomer(data) {
  return request({
    url: '/system/customer',
    method: 'post',
    data: data
  })
}

export function updateCustomer(data) {
  return request({
    url: '/system/customer',
    method: 'put',
    data: data
  })
}

export function delCustomer(customerId) {
  return request({
    url: '/system/customer/' + customerId,
    method: 'delete'
  })
}

export function exportCustomer(query) {
  return request({
    url: '/system/customer/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
