import request from '@/utils/request'

// 获取帖子列表
export const getPostList = (params) => {
  return request({
    url: '/community/api/post/list',
    method: 'get',
    params
  })
}

// 获取帖子详情
export const getPostDetail = (id) => {
  return request({
    url: `/community/api/post/${id}`,
    method: 'get'
  })
}

// 发布帖子
export const createPost = (data) => {
  return request({
    url: '/community/api/post',
    method: 'post',
    data
  })
}

// 点赞帖子
export const likePost = (id) => {
  return request({
    url: `/community/api/post/${id}/like`,
    method: 'post'
  })
}

export const getMyPosts = (params) => {
  return request({
    url: '/community/api/post/my',
    method: 'get',
    params
  })
}
