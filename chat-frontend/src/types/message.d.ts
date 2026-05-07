// 消息模块类型定义
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

export interface SendMessageParams {
  toUserId: number
  content: string
  messageType?: number
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

export interface ChatHistoryResponse {
  total: number
  records: MessageVO[]
}