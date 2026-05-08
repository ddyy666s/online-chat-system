<template>
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
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import ChatWindow from '@/components/ChatWindow.vue'
import GroupChatWindow from '@/components/GroupChatWindow.vue'

const route = useRoute()
const router = useRouter()
const currentChatUser = ref<any>(null)
const currentGroup = ref<any>(null)

const handleSelectChat = (friend: any) => {
  currentGroup.value = null
  currentChatUser.value = friend
}

const handleSelectGroup = (group: any) => {
  currentChatUser.value = null
  currentGroup.value = group
}

const refreshGroupList = () => {
  // 刷新群聊列表（由 Sidebar 自己处理）
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