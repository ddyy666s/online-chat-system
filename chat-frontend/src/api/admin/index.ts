import request from '../request'

export interface UserManageVO {
  id: number
  username: string
  nickname: string
  role: string
  status: number
  createdAt: string
}

export interface MessageAuditVO {
  id: number
  fromUserId: number
  fromUserNickname: string
  toUserId: number
  toUserNickname: string
  content: string
  sendTime: string
}

export interface StatisticsVO {
  totalUsers: number
  todayActiveUsers: number
  todayMessages: number
  onlineUsers: number
}

export interface PageResult<T> {
  total: number
  records: T[]
}

// 获取用户列表
export const getAdminUsersApi = (page: number, size: number, keyword?: string) => {
  return request.get<any, PageResult<UserManageVO>>('/admin/users', { params: { page, size, keyword } })
}

// 禁用/启用用户
export const updateUserStatusApi = (userId: number, status: number) => {
  return request.put(`/admin/user/${userId}/status`, null, { params: { status } })
}

// 获取聊天记录（审计）
export const getAdminMessagesApi = (params: any) => {
  return request.get<any, PageResult<MessageAuditVO>>('/admin/messages', { params })
}

// 获取统计数据
export const getAdminStatsApi = () => {
  return request.get<any, StatisticsVO>('/admin/stats')
}