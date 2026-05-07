import request from './request'

export interface ImpressionVO {
  id: number
  fromUserId: number
  fromUserNickname: string
  fromUserAvatar: string | null
  toUserId: number
  toUserNickname: string
  toUserAvatar: string | null
  content: string
  createdAt: string
}

export interface AddImpressionParams {
  toUserId: number
  content: string
}

// 添加评价
export const addImpressionApi = (toUserId: number, content: string) => {
  return request.post('/impression', { toUserId, content })
}

// 获取对我的评价
export const getImpressionsToMeApi = () => {
  return request.get<any, ImpressionVO[]>('/impression/to-me')
}

// 获取我给出的评价
export const getImpressionsByMeApi = () => {
  return request.get<any, ImpressionVO[]>('/impression/by-me')
}

// 删除评价
export const deleteImpressionApi = (impressionId: number) => {
  return request.delete(`/impression/${impressionId}`)
}