<template>
  <div class="message-item" :class="{ own: isOwn }">
    <el-avatar :size="32" :src="message.fromUserAvatar || ''">
      {{ message.fromUserNickname?.charAt(0) || 'U' }}
    </el-avatar>
    <div class="message-content">
      <div class="message-info" v-if="showInfo">
        <span class="name">{{ message.fromUserNickname }}</span>
        <span class="time">{{ formatRelativeTime(message.sendTime) }}</span>
      </div>
      <div class="message-bubble">
        <!-- 文字消息 -->
        <span v-if="message.messageType === 1 && !message.isRecalled">{{ message.content }}</span>

        <!-- 图片消息 -->
        <div v-else-if="message.messageType === 2 && !message.isRecalled" class="image-message">
          <el-image :src="message.content" :preview-src-list="[message.content]" fit="cover" class="message-image" />
        </div>

        <!-- 语音消息 -->
        <VoiceMessage v-else-if="message.messageType === 4 && !message.isRecalled" :url="message.content"
          :duration="message.duration" />

        <!-- 撤回消息 -->
        <span v-else-if="message.isRecalled" class="recalled">
          {{ isOwn ? '你撤回了一条消息' : '对方撤回了一条消息' }}
        </span>

        <!-- 其他 -->
        <span v-else>{{ message.content }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatRelativeTime } from '@/utils/date'
import VoiceMessage from './VoiceMessage.vue'

defineProps<{
  message: any
  isOwn: boolean
  showInfo?: boolean
}>()
</script>

<style scoped>
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-item.own {
  flex-direction: row-reverse;
}

.message-item.own .message-content {
  align-items: flex-end;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.message-info {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.message-bubble {
  background: white;
  padding: 8px 12px;
  border-radius: 12px;
  word-wrap: break-word;
}

.message-item.own .message-bubble {
  background: #95ec69;
}

.image-message {
  cursor: pointer;
}

.message-image {
  max-width: 200px;
  border-radius: 8px;
}

.recalled {
  color: #909399;
  font-style: italic;
}
</style>