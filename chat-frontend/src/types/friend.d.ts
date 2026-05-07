// 好友模块类型定义
export interface FriendVO {
  id: number
  userId: number
  nickname: string
  avatar: string | null
  signature: string | null
  remark: string | null
  groupName: string
  isOnline: boolean
  unreadCount: number
}

export interface FriendGroupVO {
  groupName: string
  friends: FriendVO[]
}

export interface FriendRequestVO {
  id: number
  fromUserId: number
  fromUserNickname: string
  fromUserAvatar: string | null
  message: string | null
  status: number
  createdAt: string
}

export interface SearchUserVO {
  userId: number
  nickname: string
  avatar: string | null
  signature: string | null
  remark: string | null
  isOnline: boolean
}

export interface SendFriendRequestParams {
  toUserId: number
  message?: string
}

export interface HandleFriendRequestParams {
  status: number
}

export interface MoveFriendGroupParams {
  groupName: string
}