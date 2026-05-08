<template>
  <el-dialog v-model="visible" :title="callTitle" :width="callType === 'video' ? '800px' : '400px'"
    :before-close="handleClose" :close-on-click-modal="false" :show-close="false">
    <div class="call-container" :class="{ 'video-call': callType === 'video' }">
      <div class="remote-video" v-if="callType === 'video'">
        <video ref="remoteVideoRef" autoplay playsinline class="video-element"></video>
        <div class="remote-placeholder" v-if="!isConnected">
          <el-avatar :size="100" :src="targetUser?.avatar || ''">{{ targetUser?.nickname?.charAt(0) || 'U'
            }}</el-avatar>
          <div class="status-text">{{ callStatusText }}</div>
        </div>
      </div>

      <div class="voice-call" v-if="callType === 'voice'">
        <el-avatar :size="120" :src="targetUser?.avatar || ''">{{ targetUser?.nickname?.charAt(0) || 'U' }}</el-avatar>
        <div class="target-name">{{ targetUser?.nickname }}</div>
        <div class="call-status">{{ callStatusText }}</div>
        <div class="call-duration" v-if="isConnected">{{ formatDuration(callDuration) }}</div>
      </div>

      <div class="local-video" v-if="callType === 'video'">
        <video ref="localVideoRef" autoplay muted playsinline class="video-element"></video>
        <div class="local-label">我</div>
      </div>
    </div>
    <template #footer>
      <div class="call-actions">
        <el-button v-if="!isConnected && !isCaller" type="success" @click="acceptCall">接听</el-button>
        <el-button type="danger" @click="hangupCall"><el-icon>
            <Phone />
          </el-icon>挂断</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Phone } from '@element-plus/icons-vue'
import { websocketService } from '@/utils/websocket'

const props = defineProps<{
  modelValue: boolean
  targetUser: any
  callType: 'voice' | 'video'
  isCaller: boolean
}>()

const emit = defineEmits(['update:modelValue', 'endCall'])

const visible = ref(false)
const isConnected = ref(false)
const callDuration = ref(0)
let durationTimer: number | null = null
let localStream: MediaStream | null = null
let peerConnection: RTCPeerConnection | null = null
const localVideoRef = ref<HTMLVideoElement>()
const remoteVideoRef = ref<HTMLVideoElement>()

const configuration = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] }

const callTitle = computed(() => `${props.callType === 'voice' ? '语音' : '视频'}通话 - ${props.targetUser?.nickname}`)
const callStatusText = computed(() => isConnected.value ? '通话中' : (props.isCaller ? '正在呼叫...' : '来电'))

const formatDuration = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 获取目标用户ID（兼容多种格式）
const getTargetUserId = (): number | null => {
  return props.targetUser?.id || props.targetUser?.userId || null
}

const createPeerConnection = async () => {
  peerConnection = new RTCPeerConnection(configuration)
  if (localStream) {
    localStream.getTracks().forEach(track => peerConnection!.addTrack(track, localStream!))
  }
  peerConnection.ontrack = (event) => {
    if (remoteVideoRef.value) remoteVideoRef.value.srcObject = event.streams[0]
  }
  peerConnection.onicecandidate = (event) => {
    if (event.candidate) {
      const targetId = getTargetUserId()
      if (targetId) {
        websocketService.sendCallSignal({
          action: 'ice-candidate',
          toUserId: targetId,
          callType: props.callType,
          candidate: event.candidate.candidate,
          sdpMid: event.candidate.sdpMid,
          sdpMLineIndex: event.candidate.sdpMLineIndex
        })
      }
    }
  }
  peerConnection.onconnectionstatechange = () => {
    if (peerConnection?.connectionState === 'connected') {
      isConnected.value = true
      if (durationTimer) clearInterval(durationTimer)
      durationTimer = setInterval(() => callDuration.value++, 1000) as unknown as number
    } else if (peerConnection?.connectionState === 'disconnected') {
      hangupCall()
    }
  }
}

// 主动发起通话
const startCall = async () => {
  const targetId = getTargetUserId()
  if (!targetId) {
    console.error('目标用户ID不存在', props.targetUser)
    ElMessage.error('通话对象信息错误')
    hangupCall()
    return
  }

  try {
    localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: props.callType === 'video' })
    if (localVideoRef.value) localVideoRef.value.srcObject = localStream
    await createPeerConnection()
    const offer = await peerConnection!.createOffer()
    await peerConnection!.setLocalDescription(offer)
    websocketService.sendCallSignal({
      action: 'offer',
      toUserId: targetId,
      callType: props.callType,
      sdp: offer.sdp
    })
  } catch (err) {
    console.error('获取媒体设备失败', err)
    ElMessage.error('无法获取摄像头/麦克风权限')
    hangupCall()
  }
}

// 接听通话
const acceptCall = async () => {
  const targetId = getTargetUserId()
  if (!targetId) {
    console.error('目标用户ID不存在', props.targetUser)
    ElMessage.error('通话对象信息错误')
    hangupCall()
    return
  }

  try {
    localStream = await navigator.mediaDevices.getUserMedia({ audio: true, video: props.callType === 'video' })
    if (localVideoRef.value) localVideoRef.value.srcObject = localStream
    await createPeerConnection()
    const answer = await peerConnection!.createAnswer()
    await peerConnection!.setLocalDescription(answer)
    websocketService.sendCallSignal({
      action: 'answer',
      toUserId: targetId,
      callType: props.callType,
      sdp: answer.sdp
    })
  } catch (err) {
    console.error('获取媒体设备失败', err)
    ElMessage.error('无法获取摄像头/麦克风权限')
    hangupCall()
  }
}

// 处理接收到的信令
const handleCallSignal = async (data: any) => {
  const targetId = getTargetUserId()
  if (data.fromUserId !== targetId) return

  if (data.action === 'offer') {
    await createPeerConnection()
    await peerConnection!.setRemoteDescription(new RTCSessionDescription({ type: 'offer', sdp: data.sdp }))
    const answer = await peerConnection!.createAnswer()
    await peerConnection!.setLocalDescription(answer)
    websocketService.sendCallSignal({
      action: 'answer',
      toUserId: targetId,
      callType: props.callType,
      sdp: answer.sdp
    })
  } else if (data.action === 'answer') {
    await peerConnection!.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: data.sdp }))
  } else if (data.action === 'ice-candidate' && data.candidate) {
    await peerConnection?.addIceCandidate(new RTCIceCandidate({
      candidate: data.candidate,
      sdpMid: data.sdpMid,
      sdpMLineIndex: data.sdpMLineIndex
    }))
  } else if (data.action === 'hangup') {
    hangupCall()
  }
}

// 挂断通话
const hangupCall = () => {
  if (peerConnection) peerConnection.close()
  if (localStream) localStream.getTracks().forEach(track => track.stop())
  if (durationTimer) clearInterval(durationTimer)
  peerConnection = null
  localStream = null
  isConnected.value = false
  callDuration.value = 0
  if (visible.value) {
    visible.value = false
    emit('update:modelValue', false)
    emit('endCall')
  }
  const targetId = getTargetUserId()
  if (!props.isCaller && targetId) {
    websocketService.sendCallSignal({
      action: 'hangup',
      toUserId: targetId,
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
    hangupCall()
  }
}, { immediate: true })

onUnmounted(() => hangupCall())
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

.voice-call {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 30px;
}

.target-name {
  font-size: 18px;
  font-weight: 500;
}

.call-status {
  font-size: 14px;
  color: #909399;
}

.call-duration {
  font-size: 28px;
  font-weight: 500;
  color: #409eff;
}

.call-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>