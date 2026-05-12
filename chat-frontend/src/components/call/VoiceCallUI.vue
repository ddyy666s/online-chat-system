<template>
  <div class="voice-call">
    <el-avatar :size="120" :src="targetUser?.avatar || ''">
      {{ targetUser?.nickname?.charAt(0) || 'U' }}
    </el-avatar>
    <div class="target-name">{{ targetUser?.nickname }}</div>
    <div class="call-status">{{ statusText }}</div>
    <div class="call-duration" v-if="isConnected">{{ formatDuration(duration) }}</div>
    <audio ref="audioRef" autoplay />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, type Ref } from 'vue'
import { formatDuration } from '@/utils/date'

const props = defineProps<{
  targetUser: any
  isConnected: boolean
  statusText: string
  duration: number
  stream?: MediaStream | null
}>()

const audioRef = ref<HTMLAudioElement>()

watch(() => props.stream, (stream) => {
  const el = audioRef.value
  if (!el) return
  if (stream) {
    el.srcObject = stream
  } else {
    el.srcObject = null
  }
}, { immediate: true })
</script>

<style scoped>
.voice-call {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 30px;
}
.target-name { font-size: 18px; font-weight: 500; }
.call-status { font-size: 14px; color: #909399; }
.call-duration { font-size: 28px; font-weight: 500; color: #409eff; }
</style>