// composables/useWebRTC.ts
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { websocketService } from '@/utils/websocket'

export const useWebRTC = (callType: 'voice' | 'video', targetUserId: number) => {
  const isConnected = ref(false)
  const callDuration = ref(0)
  let peerConnection: RTCPeerConnection | null = null
  let localStream: MediaStream | null = null
  let durationTimer: number | null = null

  const configuration = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] }

  const requestMediaPermissions = async (): Promise<MediaStream> => {
    const constraints = {
      audio: true,
      video: callType === 'video'
    }
    return await navigator.mediaDevices.getUserMedia(constraints)
  }

  const createPeerConnection = async (onTrack: (stream: MediaStream) => void) => {
    console.log('🔧 createPeerConnection, targetUserId:', targetUserId)
    peerConnection = new RTCPeerConnection(configuration)
    
    if (localStream) {
      localStream.getTracks().forEach(track => peerConnection!.addTrack(track, localStream!))
    }
    
    peerConnection.ontrack = (event) => onTrack(event.streams[0])
    
    peerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        console.log('📨 发送 ICE 候选')
        websocketService.sendCallSignal({
          action: 'ice-candidate',
          toUserId: targetUserId,
          callType,
          candidate: event.candidate.candidate,
          sdpMid: event.candidate.sdpMid,
          sdpMLineIndex: event.candidate.sdpMLineIndex
        })
      }
    }
    
    peerConnection.onconnectionstatechange = () => {
      console.log('🔌 连接状态变化:', peerConnection?.connectionState)
      if (peerConnection?.connectionState === 'connected') {
        isConnected.value = true
        ElMessage.success('通话已建立')
        startTimer()
      } else if (peerConnection?.connectionState === 'disconnected') {
        ElMessage.info('通话已结束')
        hangup()
      }
    }
    
    return peerConnection
  }

  const startLocalStream = async () => {
    console.log('🎤 startLocalStream')
    localStream = await requestMediaPermissions()
    return localStream
  }

  const createOffer = async () => {
    console.log('📞 createOffer, targetUserId:', targetUserId)
    const offer = await peerConnection!.createOffer()
    await peerConnection!.setLocalDescription(offer)
    websocketService.sendCallSignal({
      action: 'offer',
      toUserId: targetUserId,
      callType,
      sdp: offer.sdp
    })
  }

  const createAnswer = async () => {
    console.log('📞 createAnswer, targetUserId:', targetUserId)
    const answer = await peerConnection!.createAnswer()
    await peerConnection!.setLocalDescription(answer)
    websocketService.sendCallSignal({
      action: 'answer',
      toUserId: targetUserId,
      callType,
      sdp: answer.sdp
    })
  }

  const setRemoteDescription = async (sdp: string, type: 'offer' | 'answer') => {
    console.log('📨 setRemoteDescription, type:', type)
    await peerConnection!.setRemoteDescription(new RTCSessionDescription({ type, sdp }))
  }

  const addIceCandidate = async (candidate: string, sdpMid: string, sdpMLineIndex: number) => {
    console.log('📨 addIceCandidate')
    await peerConnection?.addIceCandidate(new RTCIceCandidate({ candidate, sdpMid, sdpMLineIndex }))
  }

  const startTimer = () => {
    if (durationTimer) clearInterval(durationTimer)
    durationTimer = setInterval(() => callDuration.value++, 1000) as unknown as number
  }

  const hangup = () => {
    console.log('📞 hangup')
    if (peerConnection) peerConnection.close()
    if (localStream) localStream.getTracks().forEach(track => track.stop())
    if (durationTimer) clearInterval(durationTimer)
    peerConnection = null
    localStream = null
    isConnected.value = false
    callDuration.value = 0
  }

  return {
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
  }
}