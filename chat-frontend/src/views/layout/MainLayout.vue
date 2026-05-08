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
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import ChatWindow from '@/components/ChatWindow.vue'

const currentChatUser = ref<any>(null)

const handleSelectChat = (friend: any) => {
  console.log('MainLayout 收到选择好友:', friend)
  currentChatUser.value = friend
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