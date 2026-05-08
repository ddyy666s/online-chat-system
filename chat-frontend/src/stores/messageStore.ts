import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCountApi, type UnreadCountVO } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref<UnreadCountVO | null>(null)

  const loadUnreadCount = async () => {
    try {
      const res = await getUnreadCountApi()
      unreadCount.value = res
    } catch (error) {
      console.error('加载未读计数失败', error)
    }
  }

  const clearUnreadForFriend = (friendId: number) => {
    if (unreadCount.value) {
      unreadCount.value.messages = unreadCount.value.messages.filter(
        m => m.fromUserId !== friendId
      )
      const detail = unreadCount.value.details.find(d => d.friendId === friendId)
      if (detail) {
        unreadCount.value.total -= detail.unreadCount
        unreadCount.value.details = unreadCount.value.details.filter(d => d.friendId !== friendId)
      }
    }
  }

  return {
    unreadCount,
    loadUnreadCount,
    clearUnreadForFriend
  }
})