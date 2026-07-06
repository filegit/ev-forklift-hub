import request from '@/utils/request'

export const collectPost = (postId) => {
  return request({ url: `/community/api/collection/${postId}`, method: 'post' })
}

export const checkCollection = (postId) => {
  return request({ url: `/community/api/collection/check/${postId}`, method: 'get' })
}

export const getMyCollections = (params) => {
  return request({ url: '/community/api/collection/my', method: 'get', params })
}
