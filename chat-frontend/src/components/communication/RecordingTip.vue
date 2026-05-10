<template>
  <div v-if="isRecording" class="recording-tip">
    <el-icon>
      <Microphone />
    </el-icon>
    <span>正在录音... {{ formatDuration(recordDuration) }} 松手发送</span>
    <div class="recording-wave"><span v-for="i in 5" :key="i"></span></div>
  </div>
</template>

<script setup lang="ts">
import { Microphone } from '@element-plus/icons-vue'
import { formatDuration } from '@/utils/date'

defineProps<{
  isRecording: boolean
  recordDuration: number
}>()
</script>

<style scoped>
.recording-tip {
  position: fixed;
  bottom: 150px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.85);
  color: white;
  padding: 16px 24px;
  border-radius: 40px;
  display: flex;
  gap: 16px;
  z-index: 1000;
}

.recording-wave {
  display: flex;
  gap: 4px;
}

.recording-wave span {
  width: 4px;
  height: 15px;
  background: #ff4444;
  animation: wave 0.5s infinite;
}

@keyframes wave {

  0%,
  100% {
    height: 15px;
  }

  50% {
    height: 35px;
  }
}
</style>