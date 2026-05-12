/** 用户模块API @module user */
import request from '../request'

/** 登录参数 */
export interface LoginParams {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
}

/** 注册参数 */
export interface RegisterParams {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 昵称 */
  nickname: string
}

/** 用户信息 */
export interface UserInfo {
  /** 用户ID */
  id: number
  /** 用户名 */
  username: string
  /** 昵称 */
  nickname: string
  /** 头像URL */
  avatar: string | null
  /** 个性签名 */
  signature: string | null
  /** 角色：user-普通用户 admin-管理员 */
  role: 'user' | 'admin'
}

/** 登录响应 */
export interface LoginResponse {
  /** JWT Token */
  token: string
  /** 用户信息 */
  user: UserInfo
}

/** 更新个人资料参数 */
export interface UpdateProfileParams {
  /** 昵称 */
  nickname?: string
  /** 个性签名 */
  signature?: string | null
}

/** 登录 @param data 登录参数 @returns 登录响应（Token + 用户信息） */
export const loginApi = (data: LoginParams) => {
  return request.post<any, LoginResponse>('/user/login', data)
}

/** 注册 @param data 注册参数 @returns 操作结果 */
export const registerApi = (data: RegisterParams) => {
  return request.post('/user/register', data)
}

/** 获取当前用户信息 @returns 当前用户信息 */
export const getMeApi = () => {
  return request.get<any, UserInfo>('/user/me')
}

/** 更新个人资料 @param data 更新参数 @returns 更新后的用户信息 */
export const updateProfileApi = (data: UpdateProfileParams) => {
  return request.put<any, UserInfo>('/user/profile', data)
}

/** 上传头像 @param file 头像图片文件 @returns 头像URL */
export const updateAvatarApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, string>('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 获取指定用户公开资料 @param userId 用户ID @returns 用户公开资料 */
export const getUserProfileApi = (userId: number) => {
  return request.get<any, UserInfo>(`/user/${userId}`)
}
