import request from './request'

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

export const searchUsersApi = (keyword: string) => {
  return request.get<any, SearchUserVO[]>('/friend/search', { params: { keyword } })
}

export const sendFriendRequestApi = (toUserId: number, message?: string) => {
  return request.post('/friend/request', { toUserId, message })
}

export const getFriendRequestsApi = () => {
  return request.get<any, FriendRequestVO[]>('/friend/requests')
}

export const handleFriendRequestApi = (requestId: number, status: number) => {
  return request.put(`/friend/request/${requestId}`, { status })
}

export const getFriendListApi = () => {
  return request.get<any, FriendGroupVO[]>('/friend/list')
}

export const deleteFriendApi = (friendId: number) => {
  return request.delete(`/friend/${friendId}`)
}

export const moveFriendGroupApi = (friendId: number, groupName: string) => {
  return request.put(`/friend/${friendId}/group`, { groupName })
}

export const updateFriendRemarkApi = (friendId: number, remark: string) => {
  return request.put(`/friend/${friendId}/remark`, null, { params: { remark } })
}