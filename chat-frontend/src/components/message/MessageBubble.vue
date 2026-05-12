<template>
  <div class="message-bubble-wrapper" :class="{ own: isOwn }">
    <div class="avatar">
      <el-avatar :size="36" :src="avatarUrl">
        {{ message.fromUserNickname?.charAt(0) || 'U' }}
      </el-avatar>
    </div>

    <div class="message-container">
      <div class="message-info" v-if="showInfo">
        <span class="nickname">{{ message.fromUserNickname }}</span>
        <span class="time">{{ formatTime(message.sendTime) }}</span>
      </div>

      <div class="message-bubble" :class="{ recalled: message.isRecalled }">
        <!-- 撤回消息 -->
        <RecalledMessage v-if="message.isRecalled" :is-own="isOwn" />

        <!-- 文字消息 -->
        <TextMessage v-else-if="message.messageType === 1" :content="message.content" />

        <!-- 图片消息 -->
        <ImageMessage v-else-if="message.messageType === 2" :src="message.content" />

        <!-- 语音消息 -->
        <VoiceMessage v-else-if="message.messageType === 4" :url="message.content" :duration="message.duration"
          :message-id="message.id" />

        <!-- 其他类型（评价消息等） -->
        <span v-else>{{ message.content }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import defaultAvatar from '@/assets/images/default-avatar.png'
import TextMessage from '../messageBubble/TextMessage.vue'
import ImageMessage from '../messageBubble/ImageMessage.vue'
import VoiceMessage from '../messageBubble/VoiceMessage.vue'
import RecalledMessage from '../messageBubble/RecalledMessage.vue'

const props = defineProps<{
  message: {
    id: number
    fromUserId: number
    fromUserNickname: string
    fromUserAvatar?: string | null
    messageType: number
    content: string
    duration?: number
    sendTime: string
    isRecalled: boolean
  }
  isOwn: boolean
  showInfo?: boolean
}>()

const avatarUrl = computed(() => {
  return props.message.fromUserAvatar || defaultAvatar
})

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.message-bubble-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-bubble-wrapper.own {
  flex-direction: row-reverse;
}

.message-bubble-wrapper.own .message-container {
  align-items: flex-end;
}

.message-container {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-info {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.message-bubble {
  background: #fff;
  padding: 10px 14px;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  word-break: break-word;
}

.message-bubble-wrapper.own .message-bubble {
  background: #95ec69;
  border-bottom-right-radius: 4px;
}

.message-bubble-wrapper:not(.own) .message-bubble {
  background: #fff;
  border-bottom-left-radius: 4px;
}

.message-bubble.recalled {
  background: #f5f5f5;
  color: #909399;
  font-style: italic;
}
</style>