import request from '../request'
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
  duration?: number
}

export interface UnreadDetail {
  friendId: number
  friendNickname: string
  friendAvatar: string | null
  unreadCount: number
}

export interface UnreadMessage {
  id: number
  fromUserId: number
  fromUserNickname: string
  fromUserAvatar: string | null
  content: string
  sendTime: string
  messageType?: number
}

export interface UnreadCountVO {
  total: number
  details: UnreadDetail[]
  messages: UnreadMessage[]
}

export interface PageResult<T> {
  total: number
  records: T[]
}

// 获取聊天记录
export const getChatHistoryApi = (friendId: number, page: number = 1, size: number = 20) => {
  return request.get<any, PageResult<MessageVO>>(`/message/history/${friendId}`, { params: { page, size } })
}

// 下载聊天记录
export const downloadChatHistoryApi = async (friendId: number, friendName: string, limit: number = 100) => {
  const token = localStorage.getItem('chat_token')
  // 去掉可能存在的引号
  const cleanToken = token ? token.replace(/"/g, '') : ''
  
  const response = await axios.get(`/api/message/download/${friendId}`, {
    params: { limit },
    responseType: 'blob',
    headers: { 
      'Authorization': `Bearer ${cleanToken}`
    }
  })
  
  const blob = response.data
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${friendName}_聊天记录_${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

// 标记消息已读
export const markAsReadApi = (friendId: number) => {
  return request.put(`/message/read/${friendId}`)
}

// 获取未读消息统计
export const getUnreadCountApi = () => {
  return request.get<any, UnreadCountVO>('/message/unread/count')
}

// 撤回消息
export const recallMessageApi = (messageId: number) => {
  return request.put(`/message/recall/${messageId}`)
}

// 上传图片
export const uploadImageApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, string>('/message/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 上传语音
export const uploadVoiceApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, string>('/message/upload/voice', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}