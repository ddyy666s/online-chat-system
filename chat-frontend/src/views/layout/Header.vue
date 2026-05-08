<template>
  <div class="header">
    <div class="logo">
      <span>在线聊天系统</span>
    </div>

    <div class="actions">
      <!-- 消息盒子图标 -->
      <el-badge :value="messageStore.unreadCount?.total || 0" :hidden="!messageStore.unreadCount?.total">
        <el-button :icon="Bell" circle @click="showMessageBox = true" />
      </el-badge>

      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <span>{{ userStore.userInfo?.nickname }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <MessageBox v-model="showMessageBox" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'
import MessageBox from '@/components/MessageBox.vue'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
const showMessageBox = ref(false)
let intervalId: number | null = null

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示')
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

// 定时刷新未读消息
onMounted(() => {
  // 立即加载一次
  messageStore.loadUnreadCount()

  // 每3秒刷新一次
  intervalId = setInterval(() => {
    messageStore.loadUnreadCount()
  }, 3000)
})

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId)
  }
})
</script>

<style scoped>
.header {
  height: 60px;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}

.logo {
  font-size: 18px;
  font-weight: 500;
  color: #409eff;
}

.actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
</style>