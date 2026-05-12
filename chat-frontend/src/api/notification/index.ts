import request from '../request'
import axios from 'axios'
import { storage } from '@/utils/storage'

export interface SystemNotification {
  id: number
  title: string
  content: string
  adminId: number
  adminNickname: string
  createdAt: string
}

const silentRequest = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

silentRequest.interceptors.request.use((config) => {
  const token = storage.get<string>('token')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

silentRequest.interceptors.response.use(
  (response) => response.data?.data,
  () => null
)

// 获取未读系统通知（静默，不弹出错误提示）
export const getUnreadNotificationsApi = async () => {
  const res = await silentRequest.get('/system-notification/unread')
  return (res as any) || { total: 0, notifications: [] }
}

// 标记系统通知为已读
export const markNotificationAsReadApi = (notificationId: number) => {
  return request.put(`/system-notification/read/${notificationId}`)
}

// 管理员发送系统通知
export const sendNotificationApi = (title: string, content: string) => {
  return request.post('/system-notification/send', { title, content })
}

// 管理员获取已发送通知列表
export const getAdminNotificationsApi = async () => {
  const res = await silentRequest.get('/admin/notifications')
  return (res as any) || []
}
