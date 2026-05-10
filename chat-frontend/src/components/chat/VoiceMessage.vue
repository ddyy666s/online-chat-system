<template>
  <div class="voice-message" @click="togglePlay">
    <el-icon class="voice-icon">
      <VideoPlay v-if="!isPlaying" />
      <VideoPause v-else />
    </el-icon>
    <span>语音消息</span>
    <span class="voice-duration">{{ formatDuration }}</span>
    <div class="voice-wave" v-if="isPlaying">
      <span v-for="i in 5" :key="i"></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  url: string
  duration?: number
}>()

const isPlaying = ref(false)
let audio: HTMLAudioElement | null = null

const formatDuration = computed(() => {
  const d = props.duration
  if (!d || d <= 0) return '0:05'
  const mins = Math.floor(d / 60)
  const secs = d % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
})

const togglePlay = () => {
  if (!props.url) {
    ElMessage.error('语音文件不存在')
    return
  }

  if (isPlaying.value && audio) {
    audio.pause()
    audio = null
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
    ElMessage.error('播放失败')
    isPlaying.value = false
    audio = null
  }
}

onUnmounted(() => {
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
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 20px;
  min-width: 120px;
  transition: all 0.2s;
}

.voice-message:hover {
  background: #e9e9e9;
  transform: scale(1.02);
}

.message-item.own .voice-message {
  background: #95ec69;
}

.voice-icon {
  font-size: 18px;
  color: #409eff;
}

.voice-duration {
  font-size: 12px;
  color: #666;
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
  animation: wave 0.8s ease-in-out infinite;
}

.message-item.own .voice-wave span {
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

@keyframes wave {

  0%,
  100% {
    height: 12px;
  }

  50% {
    height: 20px;
  }
}
</style>