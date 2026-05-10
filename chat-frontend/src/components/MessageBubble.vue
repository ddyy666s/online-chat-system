<template>
  <div class="message-bubble-wrapper" :class="{ own: isOwn }">
    <div class="avatar">
      <el-avatar :size="36" :src="message.fromUserAvatar || defaultAvatar">
        {{ message.fromUserNickname?.charAt(0) || 'U' }}
      </el-avatar>
    </div>
    <div class="message-container">
      <div class="message-info" v-if="showInfo">
        <span class="nickname">{{ message.fromUserNickname }}</span>
        <span class="time">{{ formatTime(message.sendTime) }}</span>
      </div>
      <div class="message-bubble" :class="{ recalled: message.isRecalled }">
        <!-- 文字消息 -->
        <span v-if="message.messageType === 1 && !message.isRecalled">
          {{ message.content }}
        </span>
        
        <!-- 图片消息 -->
        <div v-else-if="message.messageType === 2 && !message.isRecalled" class="image-message">
          <el-image 
            :src="message.content" 
            :preview-src-list="[message.content]" 
            fit="cover"
            class="message-image"
            :lazy="true"
          />
        </div>
        
        <!-- 语音消息 -->
        <div v-else-if="message.messageType === 4 && !message.isRecalled" class="voice-message" @click="togglePlay">
          <el-icon class="voice-icon">
            <VideoPlay v-if="!isPlaying" />
            <VideoPause v-else />
          </el-icon>
          <span class="voice-text">语音消息</span>
          <span class="voice-duration">{{ formatDuration(message.duration) }}</span>
          <div class="voice-wave" v-if="isPlaying">
            <span v-for="i in 5" :key="i"></span>
          </div>
        </div>
        
        <!-- 撤回消息 -->
        <span v-else-if="message.isRecalled" class="recalled-text">
          {{ isOwn ? '你撤回了一条消息' : '对方撤回了一条消息' }}
        </span>
        
        <!-- 其他类型 -->
        <span v-else>{{ message.content }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/images/default-avatar.png'

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

let audio: HTMLAudioElement | null = null
const isPlaying = ref(false)

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const formatDuration = (duration?: number) => {
  if (!duration) return '0:05'
  const mins = Math.floor(duration / 60)
  const secs = duration % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const togglePlay = () => {
  if (props.message.isRecalled) return
  
  if (audio && isPlaying.value) {
    audio.pause()
    audio.currentTime = 0
    isPlaying.value = false
    return
  }
  
  if (audio) {
    audio.pause()
    audio = null
  }
  
  audio = new Audio(props.message.content)
  audio.play()
  isPlaying.value = true
  
  audio.onended = () => {
    isPlaying.value = false
    audio = null
  }
  
  audio.onerror = () => {
    console.error('播放失败')
    isPlaying.value = false
    audio = null
  }
}

watch(() => props.message.id, () => {
  if (audio) {
    audio.pause()
    audio = null
    isPlaying.value = false
  }
})
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
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
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

/* 图片消息 */
.image-message {
  cursor: pointer;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
}

/* 语音消息 */
.voice-message {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  min-width: 120px;
}

.voice-icon {
  font-size: 18px;
  color: #409eff;
}

.voice-text {
  font-size: 14px;
}

.voice-duration {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.voice-wave {
  display: flex;
  align-items: center;
  gap: 3px;
  margin-left: 8px;
}

.voice-wave span {
  width: 3px;
  height: 12px;
  background: #409eff;
  border-radius: 1px;
  animation: voiceWave 0.8s ease-in-out infinite;
}

.message-bubble-wrapper.own .voice-wave span {
  background: #333;
}

.voice-wave span:nth-child(1) { animation-delay: 0s; }
.voice-wave span:nth-child(2) { animation-delay: 0.15s; }
.voice-wave span:nth-child(3) { animation-delay: 0.3s; }
.voice-wave span:nth-child(4) { animation-delay: 0.45s; }
.voice-wave span:nth-child(5) { animation-delay: 0.6s; }

@keyframes voiceWave {
  0%, 100% { height: 12px; }
  50% { height: 20px; }
}

.recalled-text {
  color: #909399;
  font-style: italic;
}
</style>