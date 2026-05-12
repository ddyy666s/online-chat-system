<template>
  <div class="header">
    <div class="logo">
      <span>快来聊天吧</span>
    </div>

    <div class="actions">
      <el-badge :value="messageStore.unreadCount?.total || 0" :hidden="!messageStore.unreadCount?.total">
        <el-button :icon="Bell" circle @click="showMessageBox = true" />
      </el-badge>

      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="36" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <span>{{ userStore.userInfo?.nickname }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item v-if="userStore.isAdmin()" command="admin">管理后台</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <MessageBox v-model="showMessageBox" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'
import { websocketService } from '@/utils/websocket'
import MessageBox from '@/components/MessageBox.vue'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
const showMessageBox = ref(false)

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}

const onNewMessageOrNotification = () => { messageStore.loadUnreadCount() }

onMounted(() => {
  messageStore.loadUnreadCount()
  websocketService.onMessage(onNewMessageOrNotification)
  websocketService.onGroupMessage(onNewMessageOrNotification)
  websocketService.onNotification(onNewMessageOrNotification)
})
</script>

<style scoped>
.header {
  height: 60px;
  padding: 0 24px;
  border-bottom: 1px solid #e8ecf0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  flex-shrink: 0;
}
.logo {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff, #67c23a);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.actions { display: flex; align-items: center; gap: 16px; }
.user-info {
  display: flex; align-items: center; gap: 10px; cursor: pointer;
  padding: 6px 12px; border-radius: 24px; transition: background 0.2s;
}
.user-info:hover { background: #f5f7fa; }
.user-info span { font-size: 14px; font-weight: 500; color: #303133; }
</style>