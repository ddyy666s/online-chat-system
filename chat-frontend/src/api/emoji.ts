import request from './request'

export interface EmojiVO {
  id: number
  name: string
  url: string
  category: string
  userId?: number
  isSystem: boolean
  createdAt: string
}

// 获取系统表情包
export const getSystemEmojisApi = () => {
  return request.get<any, EmojiVO[]>('/emoji/system')
}

// 获取用户自定义表情包
export const getUserEmojisApi = () => {
  return request.get<any, EmojiVO[]>('/emoji/user')
}

// 上传自定义表情
export const uploadEmojiApi = (file: File, name: string, category?: string) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('name', name)
  if (category) formData.append('category', category)
  return request.post<any, EmojiVO>('/emoji/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除自定义表情
export const deleteEmojiApi = (emojiId: number) => {
  return request.delete(`/emoji/${emojiId}`)
}