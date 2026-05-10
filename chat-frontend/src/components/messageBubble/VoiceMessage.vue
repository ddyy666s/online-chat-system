<template>
  <div class="voice-message" @click="togglePlay">
    <el-icon class="voice-icon">
      <VideoPlay v-if="!isPlaying" />
      <VideoPause v-else />
    </el-icon>
    <span class="voice-text">语音消息</span>
    <span class="voice-duration">{{ formatDuration(duration) }}</span>
    <div class="voice-wave" v-if="isPlaying">
      <span v-for="i in 5" :key="i"></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, watch } from 'vue'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'

const props = defineProps<{
  url: string
  duration?: number
  messageId: number
}>()

let audio: HTMLAudioElement | null = null
const isPlaying = ref(false)

const formatDuration = (duration?: number) => {
  if (!duration) return '0:05'
  const mins = Math.floor(duration / 60)
  const secs = duration % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const togglePlay = () => {
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

  audio = new Audio(props.url)
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

// 消息切换时停止播放
watch(() => props.messageId, () => {
  if (audio) {
    audio.pause()
    audio = null
    isPlaying.value = false
  }
})

// 组件卸载时停止播放
onBeforeUnmount(() => {
  if (audio) {
    audio.pause()
    audio = null
  }
})
</script>

<style scoped>
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

.voice-wave span:nth-child(1) {
  animation-delay: 0s;
}

.voice-wave span:nth-child(2) {
  animation-delay: 0.15s;
}

.voice-wave span:nth-child(3) {
  animation-delay: 0.3s;
}

.voice-wave span:nth-child(4) {
  animation-delay: 0.45s;
}

.voice-wave span:nth-child(5) {
  animation-delay: 0.6s;
}

@keyframes voiceWave {

  0%,
  100% {
    height: 12px;
  }

  50% {
    height: 20px;
  }
}
</style>