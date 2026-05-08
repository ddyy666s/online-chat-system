import { defineStore } from 'pinia'
import { ref } from 'vue'
import { storage } from '@/utils/storage'

export interface UserInfo {
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

  const setToken = (newToken: string) => {
    token.value = newToken
    storage.set('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    console.log('setUserInfo 被调用:', info)  // 调试日志
    userInfo.value = info
    storage.set('userInfo', info)
  }

  const logout = () => {
    token.value = null
    userInfo.value = null
    storage.clear()
  }

  const isLoggedIn = () => {
    return !!token.value
  }

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