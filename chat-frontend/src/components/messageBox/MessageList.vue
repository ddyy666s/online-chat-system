<template>
  <div class="message-list">
    <template v-if="groupedMessages.length > 0">
      <template v-for="group in groupedMessages" :key="group.date">
        <div class="date-divider">
          <span>{{ group.date }}</span>
        </div>

        <MessageItem v-for="msg in group.messages" :key="msg.id" :message="msg" @click="handleClick" />
      </template>
    </template>

    <Empty v-else title="暂无未读消息" description="快去和好友聊天吧" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Empty from '@/components/common/Empty.vue'
import MessageItem from './MessageItem.vue'

// 定义消息类型
export interface Message {
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
  messages: Message[]
}>()

const emit = defineEmits<{
  click: [userId: number]
}>()

// 按日期分组
const groupedMessages = computed(() => {
  const groups: { date: string; messages: Message[] }[] = []
  const today = new Date().toDateString()
  const yesterday = new Date(Date.now() - 86400000).toDateString()

  for (const msg of props.messages) {
    const msgDate = new Date(msg.sendTime).toDateString()
    let dateLabel = ''

    if (msgDate === today) {
      dateLabel = '今天'
    } else if (msgDate === yesterday) {
      dateLabel = '昨天'
    } else {
      dateLabel = msg.sendTime.substring(0, 10)
    }

    const existingGroup = groups.find(g => g.date === dateLabel)
    if (existingGroup) {
      existingGroup.messages.push(msg)
    } else {
      groups.push({ date: dateLabel, messages: [msg] })
    }
  }

  return groups
})

const handleClick = (userId: number) => {
  emit('click', userId)
}
</script>

<style scoped>
.message-list {
  height: 100%;
  overflow-y: auto;
  padding: 0 12px;
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
</style>