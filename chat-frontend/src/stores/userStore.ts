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

  const setToken = (newToken: string) => {
    token.value = newToken
    storage.set('token', newToken)
    console.log('token已存储:', newToken.substring(0, 30) + '...')
  }

  const setUserInfo = (info: UserInfo) => {
    console.log('setUserInfo 接收到的数据:', JSON.stringify(info, null, 2))
    userInfo.value = info
    storage.set('userInfo', info)
    // 立即验证是否存储成功
    const saved = storage.get<UserInfo>('userInfo')
    console.log('验证存储结果:', saved)
  }

  const logout = () => {
    token.value = null
    userInfo.value = null
    storage.clear()
    console.log('已退出登录')
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