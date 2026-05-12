<template>
  <div class="voice-message" :class="{ playing: isPlaying }" @click="togglePlay">
    <el-icon class="voice-icon">
      <VideoPlay v-if="!isPlaying" />
      <VideoPause v-else />
    </el-icon>
    <div class="voice-wave" :class="{ active: isPlaying }">
      <span v-for="i in 5" :key="i"></span>
    </div>
    <span class="voice-duration">{{ formatDuration }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeUnmount, watch } from 'vue'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  url: string
  duration?: number
  messageId: number
}>()

const isPlaying = ref(false)
let audio: HTMLAudioElement | null = null

<<<<<<< HEAD
const formatDuration = computed(() => {
  const d = props.duration
  if (!d || d <= 0) return '0:05'
  const mins = Math.floor(d / 60)
  const secs = d % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
})

const cleanup = () => {
  if (audio) { audio.pause(); audio.src = ''; audio.load(); audio = null }
  isPlaying.value = false
=======
const formatDuration = (duration?: number) => {
  const secs = duration && duration > 0 ? Math.round(duration) : 0
  const mins = Math.floor(secs / 60)
  const remaining = secs % 60
  return `${mins}:${remaining.toString().padStart(2, '0')}`
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
}

const playUrl = (url: string, useProxy: boolean): Promise<void> => {
  return new Promise((resolve, reject) => {
    const el = new Audio()
    el.preload = 'auto'
    el.oncanplaythrough = () => { el.play().then(resolve).catch(reject) }
    el.onerror = () => reject(new Error('音频加载失败'))
    el.src = useProxy ? `/api/message/proxy-audio?url=${encodeURIComponent(url)}` : url
    el.load()
    audio = el
  })
}

const tryPlay = async (url: string, attempt = 0): Promise<void> => {
  try { await playUrl(url, attempt > 0) }
  catch { if (attempt === 0) return tryPlay(url, 1); throw new Error('播放失败') }
}

const togglePlay = async () => {
  if (!props.url) { ElMessage.error('语音文件不存在'); return }
  if (isPlaying.value) { cleanup(); return }
  cleanup()
  isPlaying.value = true
  try {
    await tryPlay(props.url)
    if (audio) audio.onended = () => { cleanup() }
  } catch { ElMessage.error('语音加载失败，请重试'); cleanup() }
}

watch(() => props.messageId, () => { cleanup() })
onBeforeUnmount(() => { cleanup() })
</script>

<style scoped>
.voice-message { display: inline-flex; align-items: center; gap: 6px; cursor: pointer; min-width: 80px; user-select: none; }
.voice-icon { font-size: 16px; color: #409eff; flex-shrink: 0; }
.voice-duration { font-size: 12px; color: #909399; margin-left: auto; font-variant-numeric: tabular-nums; }
.voice-wave { display: flex; align-items: center; gap: 2px; height: 20px; }
.voice-wave span { width: 3px; height: 4px; background: #409eff; border-radius: 2px; transition: height 0.2s; }
.voice-wave.active span { animation: voiceWave 0.8s ease-in-out infinite; }
:deep(.message-bubble-wrapper.own) .voice-wave span { background: #333; }
.voice-wave span:nth-child(1) { animation-delay: 0s; }
.voice-wave span:nth-child(2) { animation-delay: 0.15s; }
.voice-wave span:nth-child(3) { animation-delay: 0.3s; }
.voice-wave span:nth-child(4) { animation-delay: 0.45s; }
.voice-wave span:nth-child(5) { animation-delay: 0.6s; }
@keyframes voiceWave { 0%, 100% { height: 4px; } 50% { height: 16px; } }
</style>