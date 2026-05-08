import request from './request'

export interface GroupVO {
  id: number
  name: string
  avatar: string | null
  notice: string | null
  ownerId: number
  memberCount: number
  unreadCount: number
  createdAt: string
}

export interface GroupMessageVO {
  id: number
  groupId: number
  fromUserId: number
  fromUserNickname: string
  fromUserAvatar: string | null
  content: string
  messageType: number
  sendTime: string
}

export interface GroupMemberVO {
  userId: number
  nickname: string
  avatar: string | null
  groupNickname: string | null
  role: number  // 0:成员, 1:管理员, 2:群主
}

export interface CreateGroupParams {
  name: string
  avatar?: string
  notice?: string
  memberIds?: number[]
}

export interface InviteMemberParams {
  groupId: number
  userId: number
}

// 创建群聊
export const createGroupApi = (data: CreateGroupParams) => {
  return request.post<any, GroupVO>('/group', data)
}

// 获取群聊列表
export const getGroupListApi = () => {
  return request.get<any, GroupVO[]>('/group/list')
}

// 获取群详情
export const getGroupDetailApi = (groupId: number) => {
  return request.get<any, GroupVO>(`/group/${groupId}`)
}

// 获取群聊天记录
export const getGroupHistoryApi = (groupId: number, page: number = 1, size: number = 20) => {
  return request.get<any, { total: number, records: GroupMessageVO[] }>(`/group/message/${groupId}`, { params: { page, size } })
}

// 获取群成员列表
export const getGroupMembersApi = (groupId: number) => {
  return request.get<any, GroupMemberVO[]>(`/group/${groupId}/members`)
}

// 邀请成员
export const inviteMemberApi = (data: InviteMemberParams) => {
  return request.post('/group/invite', data)
}

// 退出群聊
export const quitGroupApi = (groupId: number) => {
  return request.delete(`/group/${groupId}/quit`)
}

// 解散群聊
export const disbandGroupApi = (groupId: number) => {
  return request.delete(`/group/${groupId}/disband`)
}

// 清除群未读计数
export const clearGroupUnreadApi = (groupId: number) => {
  return request.put(`/group/${groupId}/read`)
}

// 更新群公告
export const updateGroupNoticeApi = (groupId: number, notice: string) => {
  return request.put(`/group/${groupId}/notice`, { notice })
}