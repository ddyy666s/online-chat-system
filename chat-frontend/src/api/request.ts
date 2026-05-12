/** Axios HTTP 请求封装模块 @module request */
import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'

/** API 通用响应结构 */
interface ApiResponse<T = any> {
  /** 状态码 */
  code: number
  /** 提示消息 */
  message: string
  /** 响应数据 */
  data: T
}

/** 已配置的 Axios 请求实例 */
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/** 请求拦截器：自动注入 Token */
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    const token = userStore.token
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/** 响应拦截器：统一处理状态码，业务错误交由调用方自行处理避免重复弹窗 */
request.interceptors.response.use(
  (response): any => {
    const { code, message, data } = response.data as ApiResponse
    
    if (code === 200) {
      return data
    }
    
    if (code === 1005) {
      ElMessage.error('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
      return Promise.reject(new Error(message))
    }
    
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default request
