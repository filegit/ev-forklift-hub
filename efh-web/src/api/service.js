import request from '@/utils/request'

export const getServiceOrders = () => {
  return request({ url: '/service/api/service/order/list', method: 'get' })
}

export const createServiceOrder = (data) => {
  return request({ url: '/service/api/service/order', method: 'post', data })
}

export const cancelServiceOrder = (id) => {
  return request({ url: `/service/api/service/order/${id}/cancel`, method: 'post' })
}

export const getServiceOrderDetail = (id) => {
  return request({ url: `/service/api/service/order/${id}`, method: 'get' })
}
