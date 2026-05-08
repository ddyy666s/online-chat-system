<template>
  <el-drawer v-model="visible" title="消息盒子" direction="rtl" size="400px" :before-close="handleClose">
    <div class="message-box">
      <div v-if="unreadMessages.length > 0">
        <!-- 按日期分组显示 -->
        <template v-for="(group, date) in groupedMessages" :key="date">
          <div class="date-divider">
            <span>{{ date }}</span>
          </div>

          <div v-for="msg in group" :key="msg.id" class="message-item"
            :class="{ 'is-impression': msg.messageType === 3 }" @click="jumpToChat(msg.fromUserId)">
            <div class="avatar-wrapper">
              <el-avatar :size="48" :src="msg.fromUserAvatar || ''">
                {{ msg.fromUserNickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="online-dot" v-if="msg.isOnline"></span>
            </div>

            <div class="message-content">
              <div class="message-header">
                <span class="name">{{ msg.fromUserNickname }}</span>
                <span class="time">{{ formatTime(msg.sendTime) }}</span>
              </div>

              <div class="message-preview">
                <el-icon v-if="msg.messageType === 3" class="impression-icon">
                  <Star />
                </el-icon>
                <el-icon v-else class="message-icon">
                  <ChatDotRound />
                </el-icon>
                <span class="content">{{ msg.content }}</span>
              </div>
            </div>

            <div class="message-badge">
              <el-badge :value="msg.unreadCount || 1" :hidden="false" />
            </div>
          </div>
        </template>
      </div>

      <div v-else class="empty-state">
        <el-icon :size="60">
          <Message />
        </el-icon>
        <p>暂无未读消息</p>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/messageStore'
import { markAsReadApi } from '@/api/message'
import { formatRelativeTime } from '@/utils/date'
import { Star, ChatDotRound, Message } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue'])
const router = useRouter()
const messageStore = useMessageStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const unreadMessages = computed(() => messageStore.unreadCount?.messages || [])

// 按日期分组
const groupedMessages = computed(() => {
  const groups: Record<string, any[]> = {}
  const today = new Date().toDateString()
  const yesterday = new Date(Date.now() - 86400000).toDateString()

  for (const msg of unreadMessages.value) {
    const msgDate = new Date(msg.sendTime).toDateString()
    let groupKey = ''

    if (msgDate === today) {
      groupKey = '今天'
    } else if (msgDate === yesterday) {
      groupKey = '昨天'
    } else {
      groupKey = msg.sendTime.substring(0, 10)
    }

    if (!groups[groupKey]) {
      groups[groupKey] = []
    }
    groups[groupKey].push(msg)
  }

  return groups
})

// 格式化时间（显示具体时间）
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()

  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return formatRelativeTime(time)
}

const handleClose = () => {
  visible.value = false
}

const jumpToChat = async (friendId: number) => {
  visible.value = false
  try {
    await markAsReadApi(friendId)
    messageStore.loadUnreadCount()
  } catch (error) {
    console.error(error)
  }
  router.push(`/?friendId=${friendId}`)
}
</script>

<style scoped>
.message-box {
  padding: 0 12px;
  height: 100%;
  overflow-y: auto;
}

/* 日期分割线 */
.date-divider {
  text-align: center;
  margin: 16px 0;
  position: relative;
}

.date-divider span {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 4px 12px;
  border-radius: 20px;
}

/* 消息条目 */
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #c0c4cc;
}

.empty-state .el-icon {
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}
</style>