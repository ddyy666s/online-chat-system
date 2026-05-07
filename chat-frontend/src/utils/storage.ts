// 存储前缀
const PREFIX = 'chat_'

export const storage = {
  // 设置
  set<T>(key: string, value: T): void {
    try {
      const data = JSON.stringify(value)
      localStorage.setItem(PREFIX + key, data)
    } catch (e) {
      console.error('存储失败', e)
    }
  },
  
  // 获取
  get<T>(key: string, defaultValue?: T): T | null {
    try {
      const data = localStorage.getItem(PREFIX + key)
      if (data === null) return defaultValue || null
      return JSON.parse(data) as T
    } catch (e) {
      console.error('读取失败', e)
      return defaultValue || null
    }
  },
  
  // 删除
  remove(key: string): void {
    localStorage.removeItem(PREFIX + key)
  },
  
  // 清空所有
  clear(): void {
    Object.keys(localStorage).forEach(key => {
      if (key.startsWith(PREFIX)) {
        localStorage.removeItem(key)
      }
    })
  }
}