<template>
  <div class="message-area">
    <div class="message-input">
      <el-input v-model="content" type="textarea" :rows="2" placeholder="请输入消息..." @keyup.ctrl.enter="handleSend" />
      <div class="input-actions">
        <el-button type="primary" @click="handleSend">发送</el-button>
      </div>
    </div>
    <CommunicationBar :current-chat-user-id="currentChatUserId" @send-image="(url) => $emit('sendImage', url)"
      @send-voice="(url, duration) => $emit('sendVoice', url, duration)" @send-emoji="(url) => $emit('sendEmoji', url)"
      @start-voice-call="(id) => $emit('startVoiceCall', id)" @start-video-call="(id) => $emit('startVideoCall', id)" />
  </div>
</template>

<script setup lang="ts">
/** 消息输入组件，提供文本输入和通信工具栏 @component */
import { ref } from 'vue'
import CommunicationBar from '@/components/common/CommunicationBar.vue'

/** 组件属性：当前聊天用户 ID */
defineProps<{
  currentChatUserId?: number
}>()

/** 组件事件：发送文本/图片/语音/表情消息、发起通话 */
const emit = defineEmits<{
  (e: 'send', content: string): void
  (e: 'sendImage', url: string): void
  (e: 'sendVoice', url: string, duration: number): void
  (e: 'sendEmoji', url: string): void
  (e: 'startVoiceCall', toUserId: number): void
  (e: 'startVideoCall', toUserId: number): void
}>()

/** 输入框内容 */
const content = ref('')

/** 发送文本消息 @returns void */
const handleSend = () => {
  if (!content.value.trim()) return
  emit('send', content.value)
  content.value = ''
}
</script>

<style scoped>
.message-area {
  border-top: 1px solid #e4e7ed;
  background: #fff;
}

.message-input {
  padding: 12px;
}

.input-actions {
  margin-top: 8px;
  text-align: right;
}
</style>
