<template>
  <div class="friend-request-item">
    <div class="avatar">
      <el-avatar :size="44" :src="request.fromUserAvatar || defaultAvatar">
        {{ request.fromUserNickname?.charAt(0) || 'U' }}
      </el-avatar>
    </div>
    <div class="info">
      <div class="nickname">{{ request.fromUserNickname }}</div>
      <div class="message">{{ request.message || '申请添加好友' }}</div>
      <div class="time">{{ formatTime(request.createdAt) }}</div>
    </div>
    <div class="actions" v-if="request.status === 0">
      <el-button size="small" type="success" @click="handleAccept">同意</el-button>
      <el-button size="small" type="danger" @click="handleReject">拒绝</el-button>
    </div>
    <div class="status-badge" v-else>
      <el-tag :type="request.status === 1 ? 'success' : 'danger'" size="small">
        {{ request.status === 1 ? '已同意' : '已拒绝' }}
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 好友申请项组件，支持同意/拒绝操作 @component */
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { handleFriendRequestApi } from '@/api/friend'
import defaultAvatar from '@/assets/images/default-avatar.png'

/** 组件属性：好友申请对象 */
const props = defineProps<{
  request: {
    id: number
    fromUserId: number
    fromUserNickname: string
    fromUserAvatar: string | null
    message: string | null
    status: number
    createdAt: string
  }
}>()

/** 组件事件：刷新列表 */
const emit = defineEmits(['refresh'])

/** 格式化时间显示 @param time ISO 时间字符串 @returns 格式化后的时间文本 */
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  }
  return date.toLocaleDateString()
}

/** 同意好友申请 @returns Promise<void> */
const handleAccept = async () => {
  try {
    await handleFriendRequestApi(props.request.id, 1)
    ElMessage.success('已添加好友')
    emit('refresh')
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

/** 拒绝好友申请 @returns Promise<void> */
const handleReject = async () => {
  try {
    await handleFriendRequestApi(props.request.id, 2)
    ElMessage.success('已拒绝')
    emit('refresh')
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.friend-request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 8px;
  transition: all 0.2s;
  border: 1px solid #f0f0f0;
}

.friend-request-item:hover {
  background: #fafafa;
  transform: translateX(2px);
}

.info {
  flex: 1;
  min-width: 0;
}

.nickname {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.message {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 11px;
  color: #c0c4cc;
}

.actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.status-badge {
  flex-shrink: 0;
}
</style>
