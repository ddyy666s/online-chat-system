<template>
  <div class="header">
    <div class="logo">
      <span>在线聊天系统</span>
    </div>

    <div class="actions">
      <el-badge :value="messageStore.unreadCount.total" :hidden="messageStore.unreadCount.total === 0">
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

    <el-drawer v-model="showMessageBox" title="消息盒子" direction="rtl" size="400px">
      <div v-for="detail in messageStore.unreadCount.details" :key="detail.friendId" class="unread-item">
        <el-avatar :size="40" :src="detail.friendAvatar">
          {{ detail.friendNickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="info">
          <div class="name">{{ detail.friendNickname }}</div>
          <div class="message">{{ detail.unreadCount }} 条未读消息</div>
        </div>
        <el-button size="small" type="primary" @click="jumpToChat(detail.friendId)">去回复</el-button>
      </div>
      <el-empty v-if="messageStore.unreadCount.details.length === 0" description="暂无未读消息" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()
const showMessageBox = ref(false)

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    ElMessage.info('功能开发中')
  }
}

const jumpToChat = (friendId: number) => {
  showMessageBox.value = false
  router.push(`/?friendId=${friendId}`)
}
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

.unread-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.unread-item .info {
  flex: 1;
}

.unread-item .name {
  font-weight: 500;
  margin-bottom: 4px;
}

.unread-item .message {
  font-size: 12px;
  color: #909399;
}
</style>