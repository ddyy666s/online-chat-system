<template>
  <div class="local-video">
    <video ref="videoRef" autoplay muted playsinline class="video-element"></video>
    <div class="local-label">我</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
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
.local-video {
  position: absolute;
  bottom: 16px;
  right: 16px;
  width: 120px;
  height: 90px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #fff;
  z-index: 10;
}

.local-video .video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.local-label {
  position: absolute;
  bottom: 4px;
  left: 8px;
  color: white;
  font-size: 10px;
  background: rgba(0, 0, 0, 0.5);
  padding: 2px 6px;
  border-radius: 4px;
}
</style>