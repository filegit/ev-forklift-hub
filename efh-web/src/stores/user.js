import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // 登录
  const loginAction = async (loginForm) => {
    const res = await login(loginForm)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
  }

  // 注册
  const registerAction = async (registerForm) => {
    const res = await register(registerForm)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    loginAction,
    registerAction,
    fetchUserInfo,
    logout
  }
})
