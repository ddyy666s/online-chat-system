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
      <div class="tab-item" :class="{ active: activeTab === 'groups' }" @click="activeTab = 'groups'">
        <el-icon>
          <ChatDotRound />
        </el-icon>
        <span>群聊</span>
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
        @select-chat="handleSelectChat" />

      <!-- 群聊列表 -->
      <div v-else-if="activeTab === 'groups'" class="group-list">
        <!-- 群聊列表内容保持不变... -->
      </div>

      <!-- 好友申请列表 -->
      <div v-else-if="activeTab === 'requests'" class="request-list">
        <!-- 申请列表内容保持不变... -->
      </div>

      <!-- 印象板 -->
      <ImpressionBoard v-else-if="activeTab === 'impressions'" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Message, Star, Setting, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useFriendStore } from '@/stores/friendStore'
import { handleFriendRequestApi } from '@/api/friend'
import { websocketService } from '@/utils/websocket'
import FriendList from '@/components/FriendList.vue'
import ImpressionBoard from '@/components/ImpressionBoard.vue'

const emit = defineEmits(['selectChat', 'selectGroup'])

const router = useRouter()
const userStore = useUserStore()
const friendStore = useFriendStore()
const activeTab = ref('friends')
const currentChatUserId = ref<number | null>(null)

// 处理好友申请
const handleRequest = async (requestId: number, status: number) => {
  try {
    await handleFriendRequestApi(requestId, status)
    ElMessage.success(status === 1 ? '已同意' : '已拒绝')
    friendStore.loadFriendRequests()
    friendStore.loadFriendList()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

// 选择好友时，传递完整的 friend 对象，包含 id 和 userId
const handleSelectChat = (friend: any) => {
  const userId = friend?.userId || friend?.id
  if (userId) {
    currentChatUserId.value = userId
    // 同时传递 id 和 userId，确保 CallDialog 能正确获取
    emit('selectChat', {
      id: userId,
      userId: userId,
      nickname: friend?.nickname || friend?.name || '好友',
      avatar: friend?.avatar,
      isOnline: friend?.isOnline,
      remark: friend?.remark
    })
  }
}

// 选择群聊
const handleSelectGroup = (group: any) => {
  emit('selectGroup', group)
}

// 跳转管理后台
const goToAdmin = () => {
  router.push('/admin')
}

// 监听状态更新
onMounted(() => {
  friendStore.loadFriendList()
  friendStore.loadFriendRequests()
  websocketService.onStatus((data: any) => {
    if (data.userId) {
      friendStore.updateFriendOnlineStatus?.(data.userId, data.online)
    }
  })
})
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

/* 群聊列表样式 */
.group-list {
  padding: 8px;
}

.group-header-actions {
  padding: 8px;
  margin-bottom: 8px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.group-item:hover {
  background: #f5f5f5;
}

.group-item.active {
  background: #e6f7ff;
}

.group-avatar {
  position: relative;
  flex-shrink: 0;
}

.unread-dot {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #f56c6c;
  color: white;
  font-size: 10px;
  padding: 0 4px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.group-info {
  flex: 1;
  min-width: 0;
}

.group-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.group-desc {
  font-size: 12px;
  color: #909399;
}
</style>