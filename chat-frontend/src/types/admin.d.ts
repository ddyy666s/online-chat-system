// 管理员模块类型定义
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

export interface UserListResponse {
  total: number
  records: UserManageVO[]
}

export interface MessageAuditResponse {
  total: number
  records: MessageAuditVO[]
}