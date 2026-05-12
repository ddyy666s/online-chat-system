<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="800px" :close-on-click-modal="false" :show-close="true"
    @close="handleClose">
    <div class="video-call-container">
      <div class="remote-video-area">
        <RemoteVideo :stream="remoteStream" :target-user="targetUser" :is-connected="isConnected"
          :status-text="statusText" />
      </div>
      <div class="local-video-area">
        <LocalVideo :stream="localStream" />
      </div>
      <div class="call-duration-overlay" v-if="isConnected">{{ formatDurationLocal(callDuration) }}</div>
    </div>
    <template #footer>
      <div class="call-actions">
        <el-button v-if="showAccept" type="success" size="large" @click="handleAccept">
          <el-icon>
            <VideoCamera />
          </el-icon> 接听
        </el-button>
        <el-button type="danger" size="large" @click="handleHangup">
          <el-icon>
            <VideoCamera />
          </el-icon> 挂断
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { VideoCamera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useAliRTC } from '@/composables/useAliRTC'
import RemoteVideo from './call/RemoteVideo.vue'
import LocalVideo from './call/LocalVideo.vue'

const props = defineProps<{
  modelValue: boolean
  targetUser: any
  isCaller: boolean
  channelId?: string
}>()

const emit = defineEmits(['update:modelValue', 'endCall'])

const userStore = useUserStore()
const visible = ref(false)
let currentChannelId = ''
const { isConnected, localStream, remoteStream, callDuration, startCall, hangup } = useAliRTC()

const showAccept = computed(() => !isConnected.value && !props.isCaller)

const dialogTitle = computed(() => {
  return `视频通话 - ${props.targetUser?.nickname || '未知'}`
})

const statusText = computed(() => {
  if (isConnected.value) return '通话中'
  return props.isCaller ? '正在呼叫...' : '来电'
})

const formatDurationLocal = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const handleAccept = async () => {
  const channelId = props.channelId || currentChannelId || `video_${Date.now()}`
  const userId = String(userStore.userInfo?.id || 0)
  const userName = userStore.userInfo?.nickname || 'User'
  const ok = await startCall(channelId, userId, userName, 'video')
  if (ok) {
    websocketService.sendCallSignal({
      action: 'accept',
      toUserId: props.targetUser?.userId || props.targetUser?.id,
      callType: 'video',
      channelId
    })
  }
}

const handleHangup = async () => {
  await hangup()
  if (props.isCaller) {
    websocketService.sendCallSignal({
      action: 'hangup',
      toUserId: props.targetUser?.userId || props.targetUser?.id,
      callType: 'video'
    })
  }
  visible.value = false
  emit('update:modelValue', false)
  emit('endCall')
}

const handleClose = () => {
  handleHangup()
}

watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val && props.isCaller) {
    currentChannelId = `video_${Date.now()}`
    const userId = String(userStore.userInfo?.id || 0)
    const userName = userStore.userInfo?.nickname || 'User'
    const ok = await startCall(currentChannelId, userId, userName, 'video')
    if (!ok) {
      visible.value = false
      emit('update:modelValue', false)
      return
    }
    websocketService.sendCallSignal({
      action: 'offer',
      toUserId: props.targetUser?.userId || props.targetUser?.id,
      callType: 'video',
      channelId: currentChannelId,
      fromUserNickname: userName
    })
  }
})

onUnmounted(() => {
  hangup()
})
</script>

<style scoped>
.video-call-container {
  position: relative;
  min-height: 460px;
  background: #1a1a2e;
  border-radius: 12px;
  overflow: hidden;
}

.remote-video-area {
  width: 100%;
  height: 100%;
  min-height: 460px;
}

.local-video-area {
  position: absolute;
  bottom: 16px;
  right: 16px;
  z-index: 10;
  width: 160px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.call-duration-overlay {
  position: absolute;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 16px;
  font-weight: 500;
  background: rgba(0, 0, 0, 0.4);
  padding: 4px 16px;
  border-radius: 20px;
  font-variant-numeric: tabular-nums;
}

.call-actions {
  display: flex;
  justify-content: center;
  gap: 24px;
}
</style>
