import request from '@/utils/request'

export const likePost = (postId) => {
  return request({ url: `/community/api/like/post/${postId}`, method: 'post' })
}

export const checkPostLike = (postId) => {
  return request({ url: `/community/api/like/post/${postId}/check`, method: 'get' })
}

export const likeComment = (commentId) => {
  return request({ url: `/community/api/like/comment/${commentId}`, method: 'post' })
}

export const checkCommentLike = (commentId) => {
  return request({ url: `/community/api/like/comment/${commentId}/check`, method: 'get' })
}
