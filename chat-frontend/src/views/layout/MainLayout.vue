<template>
  <div class="main-layout">
    <Sidebar @select-chat="handleSelectChat" />
    <div class="right-panel">
      <Header />
      <div class="content">
        <ChatWindow v-if="currentChatUser && currentChatUser.userId" :friend="currentChatUser"
          :key="currentChatUser.userId" />
        <div v-else class="empty-chat">
          <el-empty description="选择好友开始聊天" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import { useFriendStore } from '@/stores/friendStore'

const route = useRoute()
const router = useRouter()
const friendStore = useFriendStore()
const currentChatUser = ref<any>(null)

// 根据 friendId 获取好友信息
const loadFriendById = async (friendId: number) => {
  // 确保好友列表已加载
  if (friendStore.friendList.length === 0) {
    await friendStore.loadFriendList()
  }
  const friend = friendStore.getFriendById(friendId)
  if (friend) {
    currentChatUser.value = friend
  } else {
    // 如果找不到，创建一个临时对象
    currentChatUser.value = { userId: friendId, nickname: '好友' }
  }
}

// 监听路由参数变化
watch(() => route.query.friendId, (newFriendId) => {
  if (newFriendId) {
    loadFriendById(Number(newFriendId))
  }
}, { immediate: true })

// 从侧边栏选择好友
const handleSelectChat = (friend: any) => {
  currentChatUser.value = friend
  // 清除 URL 中的 friendId 参数（可选）
  router.push('/')
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content {
  flex: 1;
  overflow: hidden;
}

.empty-chat {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>