import request from '@/utils/request'

// 获取个人信息
export const getProfile = () => {
  return request({
    url: '/profile',
    method: 'get'
  })
}

// 更新个人信息
export const updateProfile = (data) => {
  return request({
    url: '/profile',
    method: 'put',
    data
  })
}

// 获取我的帖子
export const getMyPosts = (params) => {
  return request({
    url: '/profile/posts',
    method: 'get',
    params
  })
}

// 获取我的评论
export const getMyComments = (params) => {
  return request({
    url: '/profile/comments',
    method: 'get',
    params
  })
}
