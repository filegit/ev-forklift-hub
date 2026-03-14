import request from '@/utils/request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/user/api/login',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data) => {
  return request({
    url: '/user/api/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/user/api/info',
    method: 'get'
  })
}
