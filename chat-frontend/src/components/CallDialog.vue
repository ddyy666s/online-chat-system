<template>
  <el-dialog v-model="visible" :title="callTitle" :width="callType === 'video' ? '800px' : '400px'"
    :before-close="handleClose" :close-on-click-modal="false" :show-close="false" destroy-on-close>
    <div class="call-container" :class="{ 'video-call': callType === 'video' }">
      <template v-if="callType === 'video'">
        <RemoteVideo :target-user="targetUser" :is-connected="isConnected" :status-text="callStatusText" :stream="remoteStream" />
        <LocalVideo :stream="localStream" />
      </template>
      <template v-else>
        <VoiceCallUI :target-user="targetUser" :is-connected="isConnected" :status-text="callStatusText" :duration="callDuration" />
      </template>
    </div>
    <template #footer>
      <CallActions :is-connected="isConnected" :is-caller="isCaller" :show-reject="callState === 'ringing'"
        @accept="acceptIncomingCall" @reject="rejectIncomingCall" @hangup="hangupCall" />
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
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
  initialOffer?: any
}>()

const emit = defineEmits(['update:modelValue', 'endCall'])

const visible = ref(false)
const remoteStream = ref<MediaStream | null>(null)
<<<<<<< HEAD
const targetUserId = computed(() => props.targetUser?.userId || props.targetUser?.id)
const pendingCandidates: any[] = []
let hangupSent = false

const { isConnected, callDuration, localStream, createOffer, handleOffer, handleAnswer, addIceCandidate, hangup } = useWebRTC(props.callType)
=======
const callState = ref<'idle' | 'calling' | 'ringing' | 'connecting' | 'connected'>('idle')
let callTimeout: number | null = null

const targetUserId = computed(() => props.targetUser?.userId || props.targetUser?.id || null)

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
} = useWebRTC(props.callType, targetUserId)
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7

const callTitle = computed(() => `${props.callType === 'voice' ? '语音' : '视频'}通话 - ${props.targetUser?.nickname}`)

<<<<<<< HEAD
const onRemoteStream = (s: MediaStream) => { remoteStream.value = s }

const flushCandidates = async () => {
  for (const c of pendingCandidates.splice(0)) {
    await addIceCandidate(c.candidate, c.sdpMid, c.sdpMLineIndex)
  }
}

const doHangup = (showMessage: string | null) => {
  if (hangupSent) return
  hangupSent = true
  hangup()
  remoteStream.value = null
  if (showMessage) ElMessage.info(showMessage)
  if (visible.value) {
    visible.value = false
    emit('update:modelValue', false)
    emit('endCall')
  }
}

const handleSignal = async (data: any) => {
  if (data.fromUserId !== targetUserId.value) return
  if (data.action === 'answer') {
    await handleAnswer(data.sdp)
    flushCandidates()
  } else if (data.action === 'ice-candidate' && data.candidate) {
    if (remoteStream.value) {
      await addIceCandidate(data.candidate, data.sdpMid, data.sdpMLineIndex)
    } else {
      pendingCandidates.push(data)
    }
  } else if (data.action === 'hangup') {
    doHangup('对方已挂断')
  }
}

const startCall = async () => {
  if (!targetUserId.value) { doHangup('通话对象信息错误'); return }
  try {
    await createOffer(onRemoteStream, targetUserId.value)
=======
const callStatusText = computed(() => {
  if (isConnected.value) return '通话中'
  if (callState.value === 'calling') return '正在呼叫...'
  if (callState.value === 'ringing') return '来电...'
  if (callState.value === 'connecting') return '连接中...'
  return ''
})

const establishCall = async () => {
  try {
    await startLocalStream()
    await createPeerConnection((stream) => {
      remoteStream.value = stream
    })
    if (props.isCaller) {
      await createOffer()
    }
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
  } catch (err) {
    console.error('建立通话失败:', err)
    ElMessage.error('获取媒体设备失败，请检查摄像头和麦克风权限')
    hangupCall()
  }
}

<<<<<<< HEAD
const acceptCall = async () => {
  if (!targetUserId.value) return
  try {
    if (props.initialOffer?.sdp) {
      await handleOffer(props.initialOffer.sdp, onRemoteStream, targetUserId.value)
      flushCandidates()
    } else {
      doHangup('未收到通话请求')
    }
  } catch (err) {
    console.error('接听通话失败:', err)
    hangup()
  }
}

const hangupCall = () => {
  if (hangupSent) return
  if (targetUserId.value) {
    websocketService.sendCallSignal({ action: 'hangup', toUserId: targetUserId.value, callType: props.callType })
  }
  doHangup(null)
=======
const initCall = async () => {
  callState.value = 'calling'
  callTimeout = window.setTimeout(() => {
    if (callState.value === 'calling') {
      ElMessage.info('对方未响应')
      hangupCall()
    }
  }, 30000)
  websocketService.sendCallSignal({
    action: 'call-request',
    toUserId: targetUserId.value,
    callType: props.callType
  })
}

const acceptIncomingCall = async () => {
  callState.value = 'connecting'
  websocketService.sendCallSignal({
    action: 'call-accept',
    toUserId: targetUserId.value,
    callType: props.callType
  })
  await establishCall()
}

const rejectIncomingCall = () => {
  websocketService.sendCallSignal({
    action: 'call-reject',
    toUserId: targetUserId.value,
    callType: props.callType
  })
  hangupCall()
}

const handleCallSignal = async (data: any) => {
  if (data.fromUserId !== targetUserId.value) return

  if (props.isCaller) {
    if (data.action === 'call-accept') {
      if (callState.value !== 'calling') return
      if (callTimeout) clearTimeout(callTimeout)
      callState.value = 'connecting'
      await establishCall()
    } else if (data.action === 'call-reject') {
      if (callState.value !== 'calling') return
      if (callTimeout) clearTimeout(callTimeout)
      ElMessage.info('对方已拒绝')
      hangupCall()
    } else if (data.action === 'answer') {
      if (callState.value !== 'connecting') return
      await setRemoteDescription(data.sdp, 'answer')
    } else if (data.action === 'ice-candidate' && data.candidate) {
      await addIceCandidate(data.candidate, data.sdpMid, data.sdpMLineIndex)
    } else if (data.action === 'hangup') {
      if (callState.value === 'idle') return
      ElMessage.info('对方已挂断')
      hangupCall()
    }
  } else {
    if (data.action === 'offer') {
      if (callState.value !== 'connecting') return
      await setRemoteDescription(data.sdp, 'offer')
      await createAnswer()
    } else if (data.action === 'ice-candidate' && data.candidate) {
      await addIceCandidate(data.candidate, data.sdpMid, data.sdpMLineIndex)
    } else if (data.action === 'hangup' || data.action === 'call-cancel') {
      if (callState.value === 'idle') return
      ElMessage.info('对方已取消通话')
      hangupCall()
    }
  }
}

const hangupCall = () => {
  if (callTimeout) clearTimeout(callTimeout)
  if (callState.value === 'calling') {
    websocketService.sendCallSignal({
      action: 'call-cancel',
      toUserId: targetUserId.value,
      callType: props.callType
    })
  } else if (callState.value === 'ringing') {
    websocketService.sendCallSignal({
      action: 'call-reject',
      toUserId: targetUserId.value,
      callType: props.callType
    })
  } else if (callState.value === 'connecting' || callState.value === 'connected') {
    websocketService.sendCallSignal({
      action: 'hangup',
      toUserId: targetUserId.value,
      callType: props.callType
    })
  }
  hangup()
  remoteStream.value = null
  callState.value = 'idle'
  visible.value = false
  emit('update:modelValue', false)
  emit('endCall')
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
}

const handleClose = () => hangupCall()

<<<<<<< HEAD
let registered = false
watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val) {
    hangupSent = false
    if (!registered) {
      websocketService.onCallSignal(handleSignal)
      registered = true
    }
    if (props.isCaller) await startCall()
=======
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    if (props.isCaller) {
      initCall()
    } else {
      callState.value = 'ringing'
    }
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
  } else {
    hangup()
    callState.value = 'idle'
  }
}, { immediate: true })

<<<<<<< HEAD
onUnmounted(() => {
  if (!hangupSent) {
    hangup()
  }
})
</script>

<style scoped>
.call-container { text-align: center; padding: 20px; position: relative; }
.video-call { height: 400px; position: relative; }
</style>
=======
onMounted(() => {
  websocketService.onCallSignal(handleCallSignal)
})

onUnmounted(() => {
  if (callTimeout) clearTimeout(callTimeout)
  hangup()
})

defineExpose({ initCall, acceptIncomingCall, rejectIncomingCall })
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
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
