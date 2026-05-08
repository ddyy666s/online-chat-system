import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { FriendGroupVO, FriendRequestVO } from '@/api/friend'
import { getFriendListApi, getFriendRequestsApi } from '@/api/friend'

export const useFriendStore = defineStore('friend', () => {
  const friendList = ref<FriendGroupVO[]>([])
  const friendRequests = ref<FriendRequestVO[]>([])
  
  const loadFriendList = async () => {
    try {
      const res = await getFriendListApi()
      console.log('加载好友列表:', res)
      friendList.value = res
    } catch (error) {
      console.error('加载好友列表失败', error)
    }
  }
  
  const loadFriendRequests = async () => {
    try {
      const res = await getFriendRequestsApi()
      friendRequests.value = res
    } catch (error) {
      console.error('加载好友申请失败', error)
    }
  }
  
  const getGroupNames = () => {
    return [...new Set(friendList.value.map(g => g.groupName))]
  }
  
  const getFriendById = (userId: number) => {
    for (const group of friendList.value) {
      const friend = group.friends.find(f => f.userId === userId)
      if (friend) return friend
    }
    return null
  }
  
  // 添加：更新好友在线状态
  const updateFriendOnlineStatus = (userId: number, isOnline: boolean) => {
    console.log('updateFriendOnlineStatus 被调用:', userId, isOnline)
    for (const group of friendList.value) {
      const friend = group.friends.find(f => f.userId === userId)
      if (friend) {
        friend.isOnline = isOnline
        console.log(`✅ 更新 ${friend.nickname} => ${isOnline ? '在线' : '离线'}`)
        break
      }
    }
  }
  
  return {
    friendList,
    friendRequests,
    loadFriendList,
    loadFriendRequests,
    getGroupNames,
    getFriendById,
    updateFriendOnlineStatus
  }
})