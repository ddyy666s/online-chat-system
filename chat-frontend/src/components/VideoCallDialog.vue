<template>
  <el-dialog v-model="visible" :title="`与 ${targetUser?.nickname} 视频通话`" width="600px" :before-close="handleClose">
    <div class="video-call-container">
      <div class="remote-video">
        <div class="placeholder-avatar">
          <el-avatar :size="120" :src="targetUser?.avatar || ''">
            {{ targetUser?.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="name">{{ targetUser?.nickname }}</div>
        </div>
      </div>
      <div class="local-video">
        <video ref="localVideoRef" autoplay muted playsinline></video>
        <div class="local-label">我</div>
      </div>
    </div>
    <template #footer>
      <el-button type="danger" @click="endCall">挂断</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  targetUser: any
  isCaller?: boolean
}>()

const emit = defineEmits(['update:modelValue', 'endCall'])

const visible = ref(false)
const localVideoRef = ref<HTMLVideoElement>()

watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val) {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
      if (localVideoRef.value) {
        localVideoRef.value.srcObject = stream
      }
    } catch (error) {
      ElMessage.error('无法获取摄像头权限')
    }
  }
}, { immediate: true })

const handleClose = () => {
  endCall()
}

const endCall = () => {
  if (localVideoRef.value && localVideoRef.value.srcObject) {
    const tracks = (localVideoRef.value.srcObject as MediaStream).getTracks()
    tracks.forEach(track => track.stop())
  }
  visible.value = false
  emit('update:modelValue', false)
  emit('endCall')
  ElMessage.info('通话已结束')
}
</script>

<style scoped>
.video-call-container {
  position: relative;
  min-height: 400px;
  background: #1a1a2e;
  border-radius: 12px;
  overflow: hidden;
}

.remote-video {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 360px;
}

.placeholder-avatar {
  text-align: center;
}

.placeholder-avatar .name {
  margin-top: 12px;
  color: white;
  font-size: 16px;
}

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
}

.local-video video {
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