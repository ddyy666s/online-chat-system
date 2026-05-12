/** 印象（评价）模块API @module impression */
import request from '../request'

/** 印象/评价视图对象 */
export interface ImpressionVO {
  /** 评价ID */
  id: number
  /** 评价者用户ID */
  fromUserId: number
  /** 评价者昵称 */
  fromUserNickname: string
  /** 评价者头像 */
  fromUserAvatar: string | null
  /** 被评价者用户ID */
  toUserId: number
  /** 被评价者昵称 */
  toUserNickname: string
  /** 被评价者头像 */
  toUserAvatar: string | null
  /** 评价内容 */
  content: string
  /** 创建时间 */
  createdAt: string
}

/** 添加印象/评价参数 */
export interface AddImpressionParams {
  /** 被评价者用户ID */
  toUserId: number
  /** 评价内容 */
  content: string
}

/** 添加评价 @param toUserId 被评价用户ID @param content 评价内容 @returns 操作结果 */
export const addImpressionApi = (toUserId: number, content: string) => {
  return request.post('/impression', { toUserId, content })
}

/** 获取对我的评价 @returns 我收到的评价列表 */
export const getImpressionsToMeApi = () => {
  return request.get<any, ImpressionVO[]>('/impression/to-me')
}

/** 获取我给出的评价 @returns 我给出的评价列表 */
export const getImpressionsByMeApi = () => {
  return request.get<any, ImpressionVO[]>('/impression/by-me')
}

/** 删除评价 @param impressionId 评价ID @returns 操作结果 */
export const deleteImpressionApi = (impressionId: number) => {
  return request.delete(`/impression/${impressionId}`)
}
