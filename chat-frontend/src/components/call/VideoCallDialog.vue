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
/** 视频通话对话框组件（基于阿里云 RTC） @component */
import { ref, computed, watch, onUnmounted } from 'vue'
import { VideoCamera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useAliRTC } from '@/composables/useAliRTC'
import RemoteVideo from './RemoteVideo.vue'
import LocalVideo from './LocalVideo.vue'

/** 组件属性：对话框显示状态、目标用户、是否为呼叫方、频道 ID */
const props = defineProps<{
  modelValue: boolean
  targetUser: any
  isCaller: boolean
  channelId?: string
}>()

/** 组件事件：更新对话框状态、结束通话 */
const emit = defineEmits(['update:modelValue', 'endCall'])

/** 用户状态 store */
const userStore = useUserStore()
/** 对话框可见性 */
const visible = ref(false)
/** 当前频道 ID */
let currentChannelId = ''
/** 使用阿里云 RTC composable 获取连接状态、流、时长及操作方法 */
const { isConnected, localStream, remoteStream, callDuration, startCall, hangup } = useAliRTC()

/** 是否显示接听按钮：未连接且非呼叫方 */
const showAccept = computed(() => !isConnected.value && !props.isCaller)

/** 对话框标题 */
const dialogTitle = computed(() => {
  return `视频通话 - ${props.targetUser?.nickname || '未知'}`
})

/** 状态文本 */
const statusText = computed(() => {
  if (isConnected.value) return '通话中'
  return props.isCaller ? '正在呼叫...' : '来电'
})

/** 格式化通话时长 @param seconds 秒数 @returns 格式化后的时间字符串 mm:ss */
const formatDurationLocal = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

/** 接听通话 @returns Promise<void> */
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

/** 挂断通话 @returns Promise<void> */
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

/** 关闭对话框 @returns Promise<void> */
const handleClose = () => {
  handleHangup()
}

/** 监听对话框显示，呼叫方自动发起通话 */
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

/** 组件卸载时挂断 */
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
