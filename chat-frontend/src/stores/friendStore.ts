import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { FriendGroupVO, FriendRequestVO } from '@/api/friend'
import { getFriendListApi, getFriendRequestsApi } from '@/api/friend'

export const useFriendStore = defineStore('friend', () => {
  const friendList = ref<FriendGroupVO[]>([])
  const friendRequests = ref<FriendRequestVO[]>([])
  
  const loadFriendList = async () => {
    const res = await getFriendListApi()
    friendList.value = res
  }
  
  const loadFriendRequests = async () => {
    const res = await getFriendRequestsApi()
    friendRequests.value = res
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
  
  return {
    friendList,
    friendRequests,
    loadFriendList,
    loadFriendRequests,
    getGroupNames,
    getFriendById
  }
})