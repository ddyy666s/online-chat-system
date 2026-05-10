<template>
  <div class="message-item" :class="{ 'is-impression': message.messageType === 3 }"
    @click="$emit('click', message.fromUserId)">
    <div class="avatar-wrapper">
      <el-avatar :size="48" :src="message.fromUserAvatar || ''">
        {{ message.fromUserNickname?.charAt(0) || 'U' }}
      </el-avatar>
      <span class="online-dot" v-if="message.isOnline"></span>
    </div>

    <div class="message-content">
      <div class="message-header">
        <span class="name">{{ message.fromUserNickname }}</span>
        <span class="time">{{ formatTime(message.sendTime) }}</span>
      </div>

      <div class="message-preview">
        <el-icon v-if="message.messageType === 3" class="impression-icon">
          <Star />
        </el-icon>
        <el-icon v-else class="message-icon">
          <ChatDotRound />
        </el-icon>
        <span class="content">{{ message.content }}</span>
      </div>
    </div>

    <div class="message-badge">
      <el-badge :value="message.unreadCount || 1" :hidden="false" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Star, ChatDotRound } from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/date'

// 定义消息类型
interface Message {
  id: number
  fromUserId: number
  fromUserNickname: string
  fromUserAvatar?: string
  content: string
  messageType: number
  sendTime: string
  unreadCount?: number
  isOnline?: boolean
}

const props = defineProps<{
  message: Message
}>()

defineEmits<{
  click: [userId: number]
}>()

// 格式化时间
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()

  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return formatRelativeTime(time)
}
</script>

<style scoped>
.message-item {
  display: flex;
  gap: 12px;
  padding: 14px 12px;
  border-radius: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  border: 1px solid #f0f0f0;
  position: relative;
}

.message-item:hover {
  background: #f5f7fa;
  transform: translateX(2px);
}

.message-item.is-impression {
  background: #fff9f0;
  border-color: #ffe7ba;
}

.message-item.is-impression:hover {
  background: #fff5e6;
}

/* 头像区域 */
.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.online-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #67c23a;
  border: 2px solid #fff;
}

/* 消息内容 */
.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}

.message-header .name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.message-header .time {
  font-size: 11px;
  color: #c0c4cc;
}

/* 消息预览 */
.message-preview {
  display: flex;
  align-items: center;
  gap: 6px;
  overflow: hidden;
}

.message-preview .impression-icon {
  color: #e6a23c;
  font-size: 14px;
  flex-shrink: 0;
}

.message-preview .message-icon {
  color: #409eff;
  font-size: 14px;
  flex-shrink: 0;
}

.message-preview .content {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

/* 消息角标 */
.message-badge {
  flex-shrink: 0;
  margin-left: 8px;
}
</style>