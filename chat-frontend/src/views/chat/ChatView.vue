<template>
  <div class="chat-view">
    <ChatWindow v-if="currentChatUser && currentChatUser.userId && !currentGroup" :friend="currentChatUser"
      :key="currentChatUser.userId" />
    <GroupChatWindow v-else-if="currentGroup" :group="currentGroup" :key="currentGroup.id"
      @update:list="refreshGroupList" />
    <div v-else class="empty-chat">
      <el-empty description="选择好友或群聊开始聊天" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ChatWindow from '@/components/message/ChatWindow.vue'
import GroupChatWindow from '@/components/group/GroupChatWindow.vue'
import { useFriendStore } from '@/stores/friendStore'
import { getGroupDetailApi } from '@/api/group'

const route = useRoute()
const friendStore = useFriendStore()

const currentChatUser = ref<any>(null)
const currentGroup = ref<any>(null)

// 根据 friendId 加载好友
const loadFriendById = async (friendId: number) => {
  if (friendStore.friendList.length === 0) {
    await friendStore.loadFriendList()
  }
  const friend = friendStore.getFriendById(friendId)
  if (friend) {
    currentChatUser.value = friend
    currentGroup.value = null
  } else {
    currentChatUser.value = { userId: friendId, nickname: '好友' }
  }
}

// 根据 groupId 加载群聊
const loadGroupById = async (groupId: number) => {
  try {
    const group = await getGroupDetailApi(groupId)
    currentGroup.value = group
    currentChatUser.value = null
  } catch (error) {
    console.error('加载群聊失败', error)
  }
}

// 监听路由参数
watch(
  () => route.query.friendId,
  (friendId) => {
    if (friendId) {
      loadFriendById(Number(friendId))
    }
  },
  { immediate: true }
)

watch(
  () => route.query.groupId,
  (groupId) => {
    if (groupId) {
      loadGroupById(Number(groupId))
    }
  },
  { immediate: true }
)

const refreshGroupList = () => {
  // 刷新群聊列表的逻辑
}
</script>

<style scoped>
.chat-view {
  height: 100%;
}

.empty-chat {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>