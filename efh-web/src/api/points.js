import request from '@/utils/request'

// 获取用户积分
export const getUserPoints = () => {
  return request({
    url: '/points',
    method: 'get'
  })
}

// 兑换积分
export const exchangePoints = (exchangeId) => {
  return request({
    url: `/points/exchange/${exchangeId}`,
    method: 'post'
  })
}

// 获取兑换记录
export const getExchangeRecords = (params) => {
  return request({
    url: '/points/exchanges',
    method: 'get',
    params
  })
}
