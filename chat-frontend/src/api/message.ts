import request from './request'
import axios from 'axios'

export interface MessageVO {
  id: number
  fromUserId: number
  fromUserNickname: string
  fromUserAvatar?: string | null
  toUserId: number
  toUserNickname: string
  messageType: number
  content: string
  isRead: boolean
  isRecalled: boolean
  sendTime: string
}

export interface UnreadDetail {
  friendId: number
  friendNickname: string
  friendAvatar: string | null
  unreadCount: number
}

export interface UnreadCountVO {
  total: number
  details: UnreadDetail[]
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export const getChatHistoryApi = (friendId: number, page: number = 1, size: number = 20) => {
  return request.get<any, PageResult<MessageVO>>(`/message/history/${friendId}`, { params: { page, size } })
}

export const downloadChatHistoryApi = async (friendId: number, friendName: string) => {
  const token = localStorage.getItem('chat_token')
  const response = await axios.get(`/api/message/download/${friendId}`, {
    responseType: 'blob',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  
  const blob = response.data
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `chat_${friendName}_${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

export const markAsReadApi = (friendId: number) => {
  return request.put(`/message/read/${friendId}`)
}

export const getUnreadCountApi = () => {
  return request.get<any, UnreadCountVO>('/message/unread/count')
}

export const recallMessageApi = (messageId: number) => {
  return request.put(`/message/recall/${messageId}`)
}