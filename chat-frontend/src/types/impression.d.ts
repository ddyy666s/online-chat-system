// 印象模块类型定义
export interface ImpressionVO {
  id: number
  fromUserId: number
  fromUserNickname: string
  fromUserAvatar: string | null
  toUserId: number
  toUserNickname: string
  toUserAvatar: string | null  // 添加这个
  content: string
  createdAt: string
}

export interface AddImpressionParams {
  toUserId: number
  content: string
}