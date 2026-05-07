import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UnreadCountVO } from '@/api/message'
import { getUnreadCountApi } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref<UnreadCountVO>({ total: 0, details: [] })
  
  const loadUnreadCount = async () => {
    try {
      const res = await getUnreadCountApi()
      unreadCount.value = res
    } catch (error) {
      console.error('加载未读计数失败', error)
    }
  }
  
  const getUnreadCountByFriend = (friendId: number): number => {
    const detail = unreadCount.value.details.find(d => d.friendId === friendId)
    return detail?.unreadCount || 0
  }
  
  const clearUnreadForFriend = (friendId: number) => {
    unreadCount.value.total -= getUnreadCountByFriend(friendId)
    unreadCount.value.details = unreadCount.value.details.filter(d => d.friendId !== friendId)
  }
  
  return {
    unreadCount,
    loadUnreadCount,
    getUnreadCountByFriend,
    clearUnreadForFriend
  }
})