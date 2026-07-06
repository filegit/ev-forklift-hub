import request from '@/utils/request'

export const getUserPoints = () => {
  return request({ url: '/user/api/points', method: 'get' })
}

export const exchangePoints = (exchangeId) => {
  return request({ url: `/user/api/points/exchange/${exchangeId}`, method: 'post' })
}

export const getExchangeRecords = (params) => {
  return request({ url: '/user/api/points/exchanges', method: 'get', params })
}

export const purchasePoints = (packageId) => {
  return request({ url: '/user/api/points/purchase', method: 'post', data: { packageId } })
}
