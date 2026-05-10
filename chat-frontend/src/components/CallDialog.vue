<template>
  <el-dialog v-model="visible" :title="callTitle" :width="callType === 'video' ? '800px' : '400px'"
    :before-close="handleClose" :close-on-click-modal="false" :show-close="false">
    <div class="call-container" :class="{ 'video-call': callType === 'video' }">
      <template v-if="callType === 'video'">
        <RemoteVideo :target-user="targetUser" :is-connected="isConnected" :status-text="callStatusText"
          :stream="remoteStream" />
        <LocalVideo :stream="localStream" />
      </template>
      <template v-else>
        <VoiceCallUI :target-user="targetUser" :is-connected="isConnected" :status-text="callStatusText"
          :duration="callDuration" />
      </template>
    </div>
    <template #footer>
      <CallActions :is-connected="isConnected" :is-caller="isCaller" @accept="acceptCall" @hangup="hangupCall" />
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { websocketService } from '@/utils/websocket'
import { useWebRTC } from '@/composables/useWebRTC'
import RemoteVideo from './call/RemoteVideo.vue'
import LocalVideo from './call/LocalVideo.vue'
import VoiceCallUI from './call/VoiceCallUI.vue'
import CallActions from './call/CallActions.vue'

const props = defineProps<{
  modelValue: boolean
  targetUser: any
  callType: 'voice' | 'video'
  isCaller: boolean
}>()

const emit = defineEmits(['update:modelValue', 'endCall'])

const visible = ref(false)
const remoteStream = ref<MediaStream | null>(null)

// 唯一修改：优先使用 userId
const targetUserId = computed(() => props.targetUser?.userId || props.targetUser?.id)

const {
  isConnected,
  callDuration,
  localStream,
  startLocalStream,
  createPeerConnection,
  createOffer,
  createAnswer,
  setRemoteDescription,
  addIceCandidate,
  hangup
} = useWebRTC(props.callType, targetUserId.value)

const callTitle = computed(() => `${props.callType === 'voice' ? '语音' : '视频'}通话 - ${props.targetUser?.nickname}`)
const callStatusText = computed(() => isConnected.value ? '通话中' : (props.isCaller ? '正在呼叫...' : '来电'))

const startCall = async () => {
  if (!targetUserId.value) {
    ElMessage.error('通话对象信息错误')
    hangupCall()
    return
  }

  try {
    await startLocalStream()
    await createPeerConnection((stream) => {
      remoteStream.value = stream
    })
    await createOffer()
  } catch (err) {
    console.error('发起通话失败:', err)
    hangup()
  }
}

const acceptCall = async () => {
  try {
    await startLocalStream()
    await createPeerConnection((stream) => {
      remoteStream.value = stream
    })
    await createAnswer()
  } catch (err) {
    console.error('接听通话失败:', err)
    hangup()
  }
}

const handleCallSignal = async (data: any) => {
  if (data.fromUserId !== targetUserId.value) return

  if (data.action === 'offer') {
    await createPeerConnection((stream) => {
      remoteStream.value = stream
    })
    await setRemoteDescription(data.sdp, 'offer')
    await createAnswer()
  } else if (data.action === 'answer') {
    await setRemoteDescription(data.sdp, 'answer')
  } else if (data.action === 'ice-candidate' && data.candidate) {
    await addIceCandidate(data.candidate, data.sdpMid, data.sdpMLineIndex)
  } else if (data.action === 'hangup') {
    hangupCall()
  }
}

const hangupCall = () => {
  hangup()
  remoteStream.value = null
  if (visible.value) {
    visible.value = false
    emit('update:modelValue', false)
    emit('endCall')
  }
  if (!props.isCaller && targetUserId.value) {
    websocketService.sendCallSignal({
      action: 'hangup',
      toUserId: targetUserId.value,
      callType: props.callType
    })
  }
}

const handleClose = () => hangupCall()

watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val) {
    if (props.isCaller) await startCall()
    websocketService.onCallSignal(handleCallSignal)
  } else {
    hangup()
  }
}, { immediate: true })

onUnmounted(() => hangup())
</script>

<style scoped>
.call-container {
  text-align: center;
  padding: 20px;
  position: relative;
}

.video-call {
  height: 400px;
  position: relative;
}
</style>