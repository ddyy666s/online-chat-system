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
        <div class="group-header-actions">
          <el-button type="primary" size="small" @click="showCreateGroupDialog = true">
            <el-icon>
              <Plus />
            </el-icon>
            创建群聊
          </el-button>
        </div>

        <div v-for="group in groupList" :key="group.id" class="group-item"
          :class="{ active: currentGroupId === group.id }" @click="selectGroup(group)">
          <div class="group-avatar">
            <el-avatar :size="40" :src="group.avatar || ''">
              {{ group.name?.charAt(0) || '群' }}
            </el-avatar>
            <span v-if="group.unreadCount > 0" class="unread-dot">
              {{ group.unreadCount > 99 ? '99+' : group.unreadCount }}
            </span>
          </div>
          <div class="group-info">
            <div class="group-name">{{ group.name }}</div>
            <div class="group-desc">{{ group.memberCount }}人 · {{ formatGroupTime(group.createdAt) }}</div>
          </div>
        </div>

        <el-empty v-if="groupList.length === 0" description="暂无群聊" />
      </div>

      <!-- 好友申请列表 -->
      <div v-else-if="activeTab === 'requests'" class="request-list">
        <div v-for="req in friendStore.friendRequests" :key="req.id" class="request-item">
          <el-avatar :size="40" :src="req.fromUserAvatar || ''">
            {{ req.fromUserNickname?.charAt(0) || 'U' }}
          </el-avatar>
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

    <!-- 创建群聊弹窗 -->
    <el-dialog v-model="showCreateGroupDialog" title="创建群聊" width="500px">
      <el-form :model="createGroupForm" label-width="80px">
        <el-form-item label="群名称" required>
          <el-input v-model="createGroupForm.name" placeholder="请输入群名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="群公告">
          <el-input v-model="createGroupForm.notice" type="textarea" :rows="2" placeholder="选填" maxlength="100" />
        </el-form-item>
        <el-form-item label="邀请好友">
          <el-select v-model="createGroupForm.memberIds" multiple filterable placeholder="选择要邀请的好友" style="width: 100%">
            <el-option v-for="friend in friendListForGroup" :key="friend.userId"
              :label="friend.remark || friend.nickname" :value="friend.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateGroupDialog = false">取消</el-button>
        <el-button type="primary" :loading="creatingGroup" @click="handleCreateGroup">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Message, Star, Setting, ChatDotRound, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useFriendStore } from '@/stores/friendStore'
import { handleFriendRequestApi } from '@/api/friend'
import { getGroupListApi, createGroupApi, type GroupVO } from '@/api/group'
import { getFriendListApi, type FriendVO } from '@/api/friend'
import { websocketService } from '@/utils/websocket'
import FriendList from '@/components/FriendList.vue'
import ImpressionBoard from '@/components/ImpressionBoard.vue'

const emit = defineEmits(['selectChat', 'selectGroup'])

const router = useRouter()
const userStore = useUserStore()
const friendStore = useFriendStore()
const activeTab = ref('friends')
const currentChatUserId = ref<number | null>(null)
const currentGroupId = ref<number | null>(null)

// 群聊相关
const groupList = ref<GroupVO[]>([])
const showCreateGroupDialog = ref(false)
const creatingGroup = ref(false)
const friendListForGroup = ref<FriendVO[]>([])
const createGroupForm = ref({
  name: '',
  notice: '',
  memberIds: [] as number[]
})

// 加载群聊列表
const loadGroupList = async () => {
  try {
    const res = await getGroupListApi()
    groupList.value = res
  } catch (error) {
    console.error('加载群聊列表失败', error)
  }
}

// 选择群聊
const selectGroup = (group: GroupVO) => {
  currentGroupId.value = group.id
  emit('selectGroup', group)
}

// 创建群聊
const handleCreateGroup = async () => {
  if (!createGroupForm.value.name.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }

  creatingGroup.value = true
  try {
    const newGroup = await createGroupApi({
      name: createGroupForm.value.name,
      notice: createGroupForm.value.notice || undefined,
      memberIds: createGroupForm.value.memberIds
    })
    groupList.value.unshift(newGroup)
    ElMessage.success('群聊创建成功')
    showCreateGroupDialog.value = false
    createGroupForm.value = { name: '', notice: '', memberIds: [] }
    selectGroup(newGroup)
  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
  } finally {
    creatingGroup.value = false
  }
}

// 加载好友列表（用于创建群聊时邀请）
const loadFriendListForGroup = async () => {
  const res = await getFriendListApi()
  const friends: FriendVO[] = []
  for (const group of res) {
    friends.push(...group.friends)
  }
  friendListForGroup.value = friends
}

// 格式化时间
const formatGroupTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

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

// 选择好友
const handleSelectChat = (friend: any) => {
  const userId = friend?.userId || friend?.id
  if (userId) {
    currentChatUserId.value = userId
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

// 跳转管理后台
const goToAdmin = () => {
  router.push('/admin')
}

// 监听群消息更新未读数
const onGroupMessage = (data: any) => {
  const group = groupList.value.find(g => g.id === data.groupId)
  if (group && currentGroupId.value !== data.groupId) {
    group.unreadCount = (group.unreadCount || 0) + 1
  }
}

onMounted(() => {
  friendStore.loadFriendList()
  friendStore.loadFriendRequests()
  loadGroupList()
  loadFriendListForGroup()
  websocketService.onStatus((data: any) => {
    if (data.userId) {
      friendStore.updateFriendOnlineStatus?.(data.userId, data.online)
    }
  })
  websocketService.onGroupMessage(onGroupMessage)
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

/* 申请列表样式 */
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