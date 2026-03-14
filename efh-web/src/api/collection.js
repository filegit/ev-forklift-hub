import request from '@/utils/request'

// 收藏帖子
export const collectPost = (postId) => {
  return request({
    url: `/collection/${postId}`,
    method: 'post'
  })
}

// 检查帖子是否已收藏
export const checkCollection = (postId) => {
  return request({
    url: `/collection/check/${postId}`,
    method: 'get'
  })
}

// 获取我的收藏
export const getMyCollections = (params) => {
  return request({
    url: '/collection/my',
    method: 'get',
    params
  })
}
