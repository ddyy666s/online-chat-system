<template>
  <div class="remote-video">
    <video ref="videoRef" autoplay playsinline class="video-element"></video>
    <div class="remote-placeholder" v-if="!isConnected">
      <el-avatar :size="100" :src="targetUser?.avatar || ''">
        {{ targetUser?.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="status-text">{{ statusText }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  targetUser: any
  isConnected: boolean
  statusText: string
  stream: MediaStream | null
}>()

const videoRef = ref<HTMLVideoElement>()

watch(() => props.stream, (newStream) => {
  if (videoRef.value && newStream) {
    videoRef.value.srcObject = newStream
  }
}, { immediate: true })
</script>

<style scoped>
.remote-video {
  width: 100%;
  height: 100%;
  background: #1a1a2e;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remote-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.status-text {
  margin-top: 12px;
  color: #fff;
  font-size: 14px;
}
</style>