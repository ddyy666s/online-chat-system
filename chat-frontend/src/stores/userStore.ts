import { defineStore } from 'pinia'
import { ref } from 'vue'
import { storage } from '@/utils/storage'

interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string | null
  signature: string | null
  role: 'user' | 'admin'
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(storage.get<string>('token'))
  const userInfo = ref<UserInfo | null>(storage.get<UserInfo>('userInfo'))

  // 设置 token
  const setToken = (newToken: string) => {
    token.value = newToken
    storage.set('token', newToken)
  }

  // 设置用户信息
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    storage.set('userInfo', info)
  }

  // 退出登录
  const logout = () => {
    token.value = null
    userInfo.value = null
    storage.clear()
  }

  // 是否已登录
  const isLoggedIn = () => {
    return !!token.value
  }

  // 是否是管理员
  const isAdmin = () => {
    return userInfo.value?.role === 'admin'
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    logout,
    isLoggedIn,
    isAdmin
  }
})