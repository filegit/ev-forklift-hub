import request from '@/utils/request'

// 点赞帖子
export const likePost = (postId) => {
  return request({
    url: `/like/post/${postId}`,
    method: 'post'
  })
}

// 检查帖子是否已点赞
export const checkPostLike = (postId) => {
  return request({
    url: `/like/post/${postId}/check`,
    method: 'get'
  })
}

// 点赞评论
export const likeComment = (commentId) => {
  return request({
    url: `/like/comment/${commentId}`,
    method: 'post'
  })
}

// 检查评论是否已点赞
export const checkCommentLike = (commentId) => {
  return request({
    url: `/like/comment/${commentId}/check`,
    method: 'get'
  })
}
