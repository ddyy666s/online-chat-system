import request from '../request'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  nickname: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string | null
  signature: string | null
  role: 'user' | 'admin'
}

export interface LoginResponse {
  token: string
  user: UserInfo
}

export interface UpdateProfileParams {
  nickname?: string
  signature?: string | null
}

// 登录 - 返回 LoginResponse
export const loginApi = (data: LoginParams) => {
  return request.post<any, LoginResponse>('/user/login', data)
}

// 注册 - 返回 void
export const registerApi = (data: RegisterParams) => {
  return request.post('/user/register', data)
}

// 获取当前用户信息 - 返回 UserInfo
export const getMeApi = () => {
  return request.get<any, UserInfo>('/user/me')
}

// 更新个人资料 - 返回 UserInfo
export const updateProfileApi = (data: UpdateProfileParams) => {
  return request.put<any, UserInfo>('/user/profile', data)
}

// 上传头像 - 返回 string (头像URL)
export const updateAvatarApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, string>('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}