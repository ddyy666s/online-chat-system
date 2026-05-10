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
import { ref } from 'vue'
import CommunicationBar from '@/components/CommunicationBar.vue'

defineProps<{
  currentChatUserId?: number
}>()

const emit = defineEmits<{
  (e: 'send', content: string): void
  (e: 'sendImage', url: string): void
  (e: 'sendVoice', url: string, duration: number): void
  (e: 'sendEmoji', url: string): void
  (e: 'startVoiceCall', toUserId: number): void
  (e: 'startVideoCall', toUserId: number): void
}>()

const content = ref('')

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