<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <h3>聊客</h3>
    </div>

    <div class="sidebar-tabs">
      <div class="tab-item" :class="{ active: activeTab === 'friends' }" @click="activeTab = 'friends'">
        <el-icon>
          <User />
        </el-icon>
        <span>好友</span>
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'requests' }" @click="activeTab = 'requests'">
        <el-icon>
          <Message />
        </el-icon>
        <span>申请</span>
        <span v-if="friendStore.friendRequests.length > 0" class="badge">
          {{ friendStore.friendRequests.length }}
        </span>
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'impressions' }" @click="activeTab = 'impressions'">
        <el-icon>
          <Star />
        </el-icon>
        <span>印象</span>
      </div>
      <div v-if="userStore.isAdmin()" class="tab-item" :class="{ active: activeTab === 'admin' }" @click="goToAdmin">
        <el-icon>
          <Setting />
        </el-icon>
        <span>管理</span>
      </div>
    </div>

    <div class="sidebar-content">
      <!-- 好友列表 -->
      <FriendList v-if="activeTab === 'friends'" :current-chat-user-id="currentChatUserId"
        @select-chat="emit('selectChat', $event)" />

      <!-- 好友申请列表 -->
      <div v-else-if="activeTab === 'requests'" class="request-list">
        <div v-for="req in friendStore.friendRequests" :key="req.id" class="request-item">
          <el-avatar :size="40" :src="req.fromUserAvatar || defaultAvatar" />
          <div class="info">
            <div class="name">{{ req.fromUserNickname }}</div>
            <div class="message">{{ req.message || '申请添加好友' }}</div>
          </div>
          <div class="actions">
            <el-button size="small" type="success" @click="handleRequest(req.id, 1)">同意</el-button>
            <el-button size="small" type="danger" @click="handleRequest(req.id, 2)">拒绝</el-button>
          </div>
        </div>
        <el-empty v-if="friendStore.friendRequests.length === 0" description="暂无申请" />
      </div>

      <!-- 印象板 -->
      <ImpressionBoard v-else-if="activeTab === 'impressions'" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Message, Star, Setting } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useFriendStore } from '@/stores/friendStore'
import { handleFriendRequestApi } from '@/api/friend'
import FriendList from '@/components/FriendList.vue'
import ImpressionBoard from '@/components/ImpressionBoard.vue'
import defaultAvatar from '@/assets/images/default-avatar.png'

const emit = defineEmits(['selectChat'])
const router = useRouter()
const userStore = useUserStore()
const friendStore = useFriendStore()
const activeTab = ref('friends')
const currentChatUserId = ref<number | null>(null)

const handleRequest = async (requestId: number, status: number) => {
  try {
    await handleFriendRequestApi(requestId, status)
    ElMessage.success(status === 1 ? '已同意' : '已拒绝')
    friendStore.loadFriendRequests()
    friendStore.loadFriendList()
  } catch (error) {
    console.error(error)
  }
}

const goToAdmin = () => {
  router.push('/admin')
}
</script>

<style scoped>
.sidebar {
  width: 300px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
}

.sidebar-tabs {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 12px 0;
  cursor: pointer;
  position: relative;
}

.tab-item.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
}

.badge {
  position: absolute;
  top: 6px;
  right: 20px;
  background: #f56c6c;
  color: white;
  font-size: 10px;
  padding: 0 4px;
  border-radius: 8px;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.request-list {
  padding: 8px;
}

.request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.request-item .info {
  flex: 1;
}

.request-item .name {
  font-weight: 500;
  margin-bottom: 4px;
}

.request-item .message {
  font-size: 12px;
  color: #909399;
}

.request-item .actions {
  display: flex;
  gap: 8px;
}
</style>