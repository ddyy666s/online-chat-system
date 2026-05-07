<template>
  <div class="friend-list">
    <div class="friend-header">
      <h3>好友列表</h3>
      <el-button :icon="Plus" size="small" circle @click="showAddFriendDialog = true" />
    </div>

    <el-scrollbar class="friend-scroll">
      <div v-for="group in friendStore.friendList" :key="group.groupName" class="friend-group">
        <div class="group-header">
          <span>{{ group.groupName }}</span>
          <span class="count">{{ group.friends.length }}</span>
        </div>

        <div v-for="friend in group.friends" :key="friend.userId" class="friend-item"
          :class="{ active: currentChatUserId === friend.userId }" @click="selectChat(friend.userId)">
          <div class="avatar">
            <el-avatar :size="40" :src="friend.avatar || defaultAvatar">
              {{ friend.nickname.charAt(0) }}
            </el-avatar>
            <span class="online-dot" :class="{ online: friend.isOnline }" />
          </div>

          <div class="friend-info">
            <div class="name">{{ friend.remark || friend.nickname }}</div>
            <div class="message">{{ friend.signature || '这个人很懒，什么都没写' }}</div>
          </div>

          <div v-if="friend.unreadCount > 0" class="unread-badge">
            {{ friend.unreadCount > 99 ? '99+' : friend.unreadCount }}
          </div>

          <el-dropdown trigger="click" @command="handleCommand($event, friend)">
            <el-button :icon="MoreFilled" size="small" text @click.stop />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="remark">修改备注</el-dropdown-item>
                <el-dropdown-item command="move">移动分组</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除好友</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-scrollbar>

    <!-- 添加好友弹窗 -->
    <el-dialog v-model="showAddFriendDialog" title="添加好友" width="500px">
      <el-input v-model="searchKeyword" placeholder="请输入用户名或昵称搜索" clearable @keyup.enter="searchUsers">
        <template #append>
          <el-button @click="searchUsers">搜索</el-button>
        </template>
      </el-input>

      <div v-if="searchResults.length > 0" class="search-results">
        <div v-for="user in searchResults" :key="user.userId" class="search-item">
          <div class="user-info">
            <el-avatar :size="36" :src="user.avatar || defaultAvatar" />
            <div class="user-detail">
              <div class="username">{{ user.nickname }}</div>
              <div class="signature">{{ user.signature || '暂无签名' }}</div>
            </div>
          </div>
          <el-button v-if="user.remark" type="info" size="small" disabled>
            已是好友
          </el-button>
          <el-button v-else type="primary" size="small" @click="sendRequest(user.userId)">
            添加好友
          </el-button>
        </div>
      </div>

      <div v-else-if="searched && searchResults.length === 0" class="empty">
        未找到相关用户
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled } from '@element-plus/icons-vue'
import { useFriendStore } from '@/stores/friendStore'
import { searchUsersApi, sendFriendRequestApi, deleteFriendApi } from '@/api/friend'
import defaultAvatar from '@/assets/images/default-avatar.png'

const props = defineProps<{
  currentChatUserId: number | null
}>()

const emit = defineEmits<{
  (e: 'selectChat', userId: number): void
}>()

const friendStore = useFriendStore()
const showAddFriendDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const searched = ref(false)

onMounted(() => {
  friendStore.loadFriendList()
  friendStore.loadFriendRequests()
})

const selectChat = (userId: number) => {
  emit('selectChat', userId)
}

const searchUsers = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  searched.value = true
  searchResults.value = await searchUsersApi(searchKeyword.value)
}

const sendRequest = async (toUserId: number) => {
  try {
    await sendFriendRequestApi(toUserId)
    ElMessage.success('好友申请已发送')
    searchKeyword.value = ''
    searchResults.value = []
    searched.value = false
    showAddFriendDialog.value = false
  } catch (error) {
    console.error(error)
  }
}

const handleCommand = async (command: string, friend: any) => {
  if (command === 'remark') {
    const { value } = await ElMessageBox.prompt('请输入备注', '修改备注', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: friend.remark || '',
      inputPlaceholder: '请输入备注名称'
    })
    if (value !== undefined) {
      // TODO: 调用修改备注API
      ElMessage.success('修改成功')
    }
  } else if (command === 'move') {
    const groups = friendStore.getGroupNames()
    const { value } = await ElMessageBox.prompt('请输入分组名称', '移动分组', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: friend.groupName,
      inputPlaceholder: '请输入分组名称'
    })
    if (value && value !== friend.groupName) {
      // TODO: 调用移动分组API
      ElMessage.success('移动成功')
    }
  } else if (command === 'delete') {
    await ElMessageBox.confirm(`确定要删除好友 ${friend.nickname} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteFriendApi(friend.userId)
    ElMessage.success('删除成功')
    friendStore.loadFriendList()
  }
}
</script>

<style scoped>
.friend-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.friend-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
}

.friend-header h3 {
  margin: 0;
  font-size: 16px;
}

.friend-scroll {
  flex: 1;
  overflow-y: auto;
}

.friend-group {
  margin-bottom: 8px;
}

.group-header {
  padding: 8px 16px;
  font-size: 13px;
  color: #909399;
  background: #f5f5f5;
}

.count {
  margin-left: 8px;
  font-size: 12px;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}

.friend-item:hover {
  background: #e4e7ed;
}

.friend-item.active {
  background: #e6f7ff;
}

.avatar {
  position: relative;
  margin-right: 12px;
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #c0c4cc;
  border: 1px solid #fff;
}

.online-dot.online {
  background: #67c23a;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.message {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: #f56c6c;
  color: white;
  font-size: 11px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8px;
}

.search-results {
  margin-top: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.search-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.signature {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>