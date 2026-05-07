// API 通用类型定义
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = any> {
  total: number
  records: T[]
}

export interface ErrorResponse {
  code: number
  message: string
}