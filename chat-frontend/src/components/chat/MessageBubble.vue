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
      <div class="message-bubble" :class="{ recalled: message.isRecalled }">
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

        <el-button v-if="isOwn && !message.isRecalled && canRecall" class="recall-btn" text size="small"
          @click.stop="handleRecall">撤回</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatRelativeTime } from '@/utils/date'
import { recallMessageApi } from '@/api/message'
import VoiceMessage from './VoiceMessage.vue'

const props = defineProps<{
  message: any
  isOwn: boolean
  showInfo?: boolean
}>()

const RECALL_LIMIT = 2 * 60 * 1000

const canRecall = computed(() => {
  if (!props.message.sendTime) return false
  const now = Date.now()
  const sendTime = new Date(props.message.sendTime).getTime()
  return now - sendTime <= RECALL_LIMIT
})

const handleRecall = async () => {
  if (!canRecall.value) {
    ElMessage.warning('消息发送超过2分钟，无法撤回')
    return
  }
  try {
    await recallMessageApi(props.message.id)
    ElMessage.success('已撤回')
    props.message.isRecalled = true
  } catch {
    ElMessage.error('撤回失败')
  }
}
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

.message-bubble {
  position: relative;
}

.message-bubble:hover .recall-btn {
  display: inline-flex;
}

.recall-btn {
  position: absolute;
  top: -24px;
  right: 0;
  display: none;
  font-size: 12px;
  color: #409eff;
}

.recalled {
  color: #909399;
  font-style: italic;
}
</style>