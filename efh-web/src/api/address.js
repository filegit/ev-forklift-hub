import request from '@/utils/request'

export const getAddressList = () => {
  return request({ url: '/user/api/address/list', method: 'get' })
}

export const getDefaultAddress = () => {
  return request({ url: '/user/api/address/default', method: 'get' })
}

export const addAddress = (data) => {
  return request({ url: '/user/api/address', method: 'post', data })
}

export const updateAddress = (id, data) => {
  return request({ url: `/user/api/address/${id}`, method: 'put', data })
}

export const deleteAddress = (id) => {
  return request({ url: `/user/api/address/${id}`, method: 'delete' })
}

export const setDefaultAddress = (id) => {
  return request({ url: `/user/api/address/${id}/default`, method: 'post' })
}

export const formatAddress = (addr) => {
  if (!addr) return ''
  return `${addr.province || ''}${addr.city || ''}${addr.district || ''}${addr.detail || ''}`
}
