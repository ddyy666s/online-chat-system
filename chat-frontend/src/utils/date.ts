import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

export const formatDate = (date: string | Date, format = 'YYYY-MM-DD HH:mm:ss'): string => {
  return dayjs(date).format(format)
}

export const formatRelativeTime = (date: string | Date): string => {
  const now = dayjs()
  const target = dayjs(date)
  const diffMinutes = now.diff(target, 'minute')
  
  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffMinutes < 1440) return `${Math.floor(diffMinutes / 60)}小时前`
  
  return target.format('MM-DD HH:mm')
}

export const isToday = (date: string | Date): boolean => {
  return dayjs(date).isSame(dayjs(), 'day')
}

// 格式化语音时长（秒 -> mm:ss）
export const formatVoiceDuration = (seconds?: number): string => {
  if (!seconds || seconds <= 0) return '0:05'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 通用时长格式化（别名，避免与 formatVoiceDuration 重复）
export const formatDuration = (seconds: number): string => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}