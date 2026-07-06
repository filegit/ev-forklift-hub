import request from '@/utils/request'

// 获取评论列表
export const getCommentList = (params) => {
  return request({
    url: '/community/api/comment/list',
    method: 'get',
    params
  })
}

// 发表评论
export const createComment = (data) => {
  return request({
    url: '/community/api/comment',
    method: 'post',
    data
  })
}

// 删除评论
export const deleteComment = (id) => {
  return request({
    url: `/community/api/comment/${id}`,
    method: 'delete'
  })
}

export const getMyComments = (params) => {
  return request({
    url: '/community/api/comment/my',
    method: 'get',
    params
  })
}
