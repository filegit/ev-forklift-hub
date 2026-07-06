import request from '@/utils/request'

export const getProfile = () => {
  return request({ url: '/user/api/profile', method: 'get' })
}

export const updateProfile = (data) => {
  return request({ url: '/user/api/profile', method: 'put', data })
}
