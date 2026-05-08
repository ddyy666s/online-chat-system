<template>
  <div class="app-container" @click="unlockAudio">
    <div class="main-layout">
      <Sidebar @select-chat="handleSelectChat" @select-group="handleSelectGroup" />
      <div class="right-panel">
        <Header />
        <div class="content">
          <!-- 单聊窗口 -->
          <ChatWindow v-if="currentChatUser && currentChatUser.userId && !currentGroup" :friend="currentChatUser"
            :key="currentChatUser.userId" />
          <!-- 群聊窗口 -->
          <GroupChatWindow v-else-if="currentGroup" :group="currentGroup" :key="currentGroup.id"
            @update:list="refreshGroupList" />
          <div v-else class="empty-chat">
            <el-empty description="选择好友或群聊开始聊天" />
          </div>
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
import GroupChatWindow from '@/components/GroupChatWindow.vue'
import { useFriendStore } from '@/stores/friendStore'

const route = useRoute()
const router = useRouter()
const friendStore = useFriendStore()
const currentChatUser = ref<any>(null)
const currentGroup = ref<any>(null)

// 根据 friendId 获取好友信息
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

// 监听路由参数变化
watch(() => route.query.friendId, (newFriendId) => {
  if (newFriendId) {
    loadFriendById(Number(newFriendId))
  }
}, { immediate: true })

// 从侧边栏选择好友
const handleSelectChat = (friend: any) => {
  currentGroup.value = null
  currentChatUser.value = friend
  router.push('/')
}

// 从侧边栏选择群聊
const handleSelectGroup = (group: any) => {
  currentChatUser.value = null
  currentGroup.value = group
  router.push('/')
}

// 刷新群聊列表
const refreshGroupList = () => {
  // 由 Sidebar 自己处理
}

// 用户首次点击页面时，静默请求音频权限
const unlockAudio = () => {
  if (navigator.mediaDevices) {
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(stream => {
        stream.getTracks().forEach(track => track.stop())
        console.log('音频权限已获取')
      })
      .catch(err => console.log('权限获取失败', err))
  }
}

onMounted(() => {
  friendStore.loadFriendList()
})
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

.app-container {
  height: 100%;
  width: 100%;
}

.empty-chat {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>