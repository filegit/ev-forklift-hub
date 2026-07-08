import request from '@/utils/request'

export const getPartsList = (params) => {
  return request({ url: '/parts/api/parts/list', method: 'get', params })
}

export const publishParts = (data) => {
  return request({ url: '/parts/api/parts', method: 'post', data })
}

export const getPartsDetail = (id) => {
  return request({ url: `/parts/api/parts/${id}`, method: 'get' })
}

export const getCartList = () => {
  return request({ url: '/parts/api/parts/cart', method: 'get' })
}

export const getCartCount = () => {
  return request({ url: '/parts/api/parts/cart/count', method: 'get' })
}

export const addToCart = (data) => {
  return request({ url: '/parts/api/parts/cart', method: 'post', data })
}

export const updateCartQuantity = (partsId, quantity) => {
  return request({ url: `/parts/api/parts/cart/${partsId}`, method: 'put', params: { quantity } })
}

export const removeFromCart = (partsId) => {
  return request({ url: `/parts/api/parts/cart/${partsId}`, method: 'delete' })
}

export const previewOrder = (data) => {
  return request({ url: '/parts/api/parts/order/preview', method: 'post', data })
}

export const submitOrder = (data) => {
  return request({ url: '/parts/api/parts/order/submit', method: 'post', data })
}

export const getOrderList = (params) => {
  return request({ url: '/parts/api/parts/order/list', method: 'get', params })
}

export const getOrderDetail = (id) => {
  return request({ url: `/parts/api/parts/order/${id}`, method: 'get' })
}

export const getOrderByNo = (orderNo) => {
  return request({ url: `/parts/api/parts/order/no/${orderNo}`, method: 'get' })
}

export const cancelOrder = (id) => {
  return request({ url: `/parts/api/parts/order/${id}/cancel`, method: 'post' })
}

export const confirmReceive = (id) => {
  return request({ url: `/parts/api/parts/order/${id}/confirm`, method: 'post' })
}

export const shipOrder = (id, data) => {
  return request({ url: `/parts/api/parts/order/${id}/ship`, method: 'post', data })
}

export const appendShipmentTrace = (id, data) => {
  return request({ url: `/parts/api/parts/order/${id}/trace`, method: 'post', data })
}

export const createAlipayPagePay = (payNo) => {
  return request({ url: '/parts/api/parts/pay/alipay/page', method: 'post', params: { payNo } })
}

export const getPayStatus = (payNo) => {
  return request({ url: '/parts/api/parts/pay/status', method: 'get', params: { payNo } })
}
