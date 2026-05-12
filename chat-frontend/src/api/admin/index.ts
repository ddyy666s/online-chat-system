/** 管理员API模块 @module admin */
import request from '../request'

/** 用户管理视图对象 */
export interface UserManageVO {
  /** 用户ID */
  id: number
  /** 用户名 */
  username: string
  /** 昵称 */
  nickname: string
  /** 角色 */
  role: string
  /** 状态：0-禁用 1-启用 */
  status: number
  /** 创建时间 */
  createdAt: string
}

/** 消息审计视图对象 */
export interface MessageAuditVO {
  /** 消息ID */
  id: number
  /** 发送者用户ID */
  fromUserId: number
  /** 发送者昵称 */
  fromUserNickname: string
  /** 接收者用户ID */
  toUserId: number
  /** 接收者昵称 */
  toUserNickname: string
  /** 消息内容 */
  content: string
  /** 发送时间 */
  sendTime: string
}

/** 统计信息视图对象 */
export interface StatisticsVO {
  /** 用户总数 */
  totalUsers: number
  /** 今日活跃用户数 */
  todayActiveUsers: number
  /** 今日消息数 */
  todayMessages: number
  /** 当前在线用户数 */
  onlineUsers: number
}

/** 分页结果泛型接口 */
export interface PageResult<T> {
  /** 总记录数 */
  total: number
  /** 当前页数据列表 */
  records: T[]
}

/** 获取用户列表 @param page 页码 @param size 每页条数 @param keyword 搜索关键词 @returns 分页用户管理列表 */
export const getAdminUsersApi = (page: number, size: number, keyword?: string) => {
  return request.get<any, PageResult<UserManageVO>>('/admin/users', { params: { page, size, keyword } })
}

/** 禁用/启用用户 @param userId 用户ID @param status 状态值 @returns 操作结果 */
export const updateUserStatusApi = (userId: number, status: number) => {
  return request.put(`/admin/user/${userId}/status`, null, { params: { status } })
}

/** 获取聊天记录（审计） @param params 查询参数 @returns 分页消息审计列表 */
export const getAdminMessagesApi = (params: any) => {
  return request.get<any, PageResult<MessageAuditVO>>('/admin/messages', { params })
}

/** 获取统计数据 @returns 统计信息视图对象 */
export const getAdminStatsApi = () => {
  return request.get<any, StatisticsVO>('/admin/stats')
}
