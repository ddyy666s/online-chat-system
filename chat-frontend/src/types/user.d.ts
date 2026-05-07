// 用户模块类型定义
export interface User {
  id: number
  username: string
  nickname: string
  avatar: string | null
  signature: string | null
  role: 'user' | 'admin'
  status: number
  createdAt: string
  updatedAt: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  nickname: string
}

export interface LoginResponse {
  token: string
  user: User
}

export interface UserInfoResponse {
  id: number
  username: string
  nickname: string
  avatar: string | null
  signature: string | null
  role: string
}