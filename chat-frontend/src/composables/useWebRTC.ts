<<<<<<< HEAD
import { ref } from 'vue'
import { websocketService } from '@/utils/websocket'

const getIceServers = (): RTCIceServer[] => {
  const servers: RTCIceServer[] = [{ urls: 'stun:stun.l.google.com:19302' }]
  const turnUrl = import.meta.env.VITE_TURN_URL
  const turnUser = import.meta.env.VITE_TURN_USERNAME
  const turnCred = import.meta.env.VITE_TURN_CREDENTIAL
  if (turnUrl && turnUser && turnCred) {
    servers.push({ urls: turnUrl, username: turnUser, credential: turnCred })
  }
  return servers
}

export const useWebRTC = (callType: 'voice' | 'video') => {
=======
import { ref, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { websocketService } from '@/utils/websocket'

export const useWebRTC = (callType: 'voice' | 'video', targetUserIdRef: Ref<number | null>) => {
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
  const isConnected = ref(false)
  const callDuration = ref(0)
  let pc: RTCPeerConnection | null = null
  let localStream: MediaStream | null = null
  let timer: number | null = null

<<<<<<< HEAD
  const configuration = { iceServers: getIceServers() }
=======
  const configuration = { iceServers: [{ urls: 'stun:stun.l.google.com:19302' }] }

  const getUserId = () => targetUserIdRef.value || 0

  const requestMediaPermissions = async (): Promise<MediaStream> => {
    const constraints = {
      audio: true,
      video: callType === 'video'
    }
    return await navigator.mediaDevices.getUserMedia(constraints)
  }

  const createPeerConnection = async (onTrack: (stream: MediaStream) => void) => {
    const userId = getUserId()
    console.log('createPeerConnection, targetUserId:', userId)
    peerConnection = new RTCPeerConnection(configuration)

    if (localStream) {
      localStream.getTracks().forEach(track => peerConnection!.addTrack(track, localStream!))
    }

    peerConnection.ontrack = (event) => onTrack(event.streams[0])

    peerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        console.log('发送 ICE 候选')
        websocketService.sendCallSignal({
          action: 'ice-candidate',
          toUserId: getUserId(),
          callType,
          candidate: event.candidate.candidate,
          sdpMid: event.candidate.sdpMid,
          sdpMLineIndex: event.candidate.sdpMLineIndex
        })
      }
    }

    peerConnection.onconnectionstatechange = () => {
      console.log('连接状态变化:', peerConnection?.connectionState)
      if (peerConnection?.connectionState === 'connected') {
        isConnected.value = true
        ElMessage.success('通话已建立')
        startTimer()
      } else if (peerConnection?.connectionState === 'disconnected' || peerConnection?.connectionState === 'failed') {
        ElMessage.info('通话已结束')
        hangup()
      }
    }

    return peerConnection
  }
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7

  const addLocalStreamToPeer = (stream: MediaStream) => {
    if (peerConnection && stream) {
      stream.getTracks().forEach(track => peerConnection!.addTrack(track, stream))
    }
    localStream = stream
  }

  const startLocalStream = async () => {
<<<<<<< HEAD
    stopLocalStream()
    localStream = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: callType === 'video'
    })
    return localStream
  }

  const stopLocalStream = () => {
    if (localStream) {
      localStream.getTracks().forEach(t => t.stop())
      localStream = null
    }
  }

  const createPC = (onRemoteStream: (s: MediaStream) => void, targetUserId: number) => {
    pc = new RTCPeerConnection(configuration)
    if (localStream) {
      localStream.getTracks().forEach(t => pc!.addTrack(t, localStream!))
    }
    pc.ontrack = (e) => onRemoteStream(e.streams[0])
    pc.onicecandidate = (e) => {
      if (e.candidate) {
        websocketService.sendCallSignal({
          action: 'ice-candidate', toUserId: targetUserId, callType,
          candidate: e.candidate.candidate,
          sdpMid: e.candidate.sdpMid,
          sdpMLineIndex: e.candidate.sdpMLineIndex
        })
      }
    }
    pc.onconnectionstatechange = () => {
      if (pc?.connectionState === 'connected') {
        isConnected.value = true
        timer = setInterval(() => callDuration.value++, 1000) as unknown as number
      }
    }
    return pc
  }

  const createOffer = async (onRemoteStream: (s: MediaStream) => void, targetUserId: number) => {
    await startLocalStream()
    const p = createPC(onRemoteStream, targetUserId)
    const offer = await p.createOffer()
    await p.setLocalDescription(offer)
    websocketService.sendCallSignal({ action: 'offer', toUserId: targetUserId, callType, sdp: offer.sdp })
  }

  const handleOffer = async (sdp: string, onRemoteStream: (s: MediaStream) => void, targetUserId: number) => {
    await startLocalStream()
    const p = createPC(onRemoteStream, targetUserId)
    await p.setRemoteDescription(new RTCSessionDescription({ type: 'offer', sdp }))
    const answer = await p.createAnswer()
    await p.setLocalDescription(answer)
    websocketService.sendCallSignal({ action: 'answer', toUserId: targetUserId, callType, sdp: answer.sdp })
  }

  const handleAnswer = async (sdp: string) => {
    if (!pc) throw new Error('No peer connection')
    await pc.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp }))
  }

  const addIceCandidate = async (candidate: string, sdpMid: string, sdpMLineIndex: number) => {
    if (!pc) return
    try { await pc.addIceCandidate(new RTCIceCandidate({ candidate, sdpMid, sdpMLineIndex })) }
    catch { /* ignore */ }
  }

  const hangup = () => {
    if (pc) { pc.onconnectionstatechange = null; pc.close(); pc = null }
    stopLocalStream()
    if (timer) { clearInterval(timer); timer = null }
=======
    console.log('startLocalStream')
    localStream = await requestMediaPermissions()
    addLocalStreamToPeer(localStream)
    return localStream
  }

  const createOffer = async () => {
    const userId = getUserId()
    console.log('createOffer, targetUserId:', userId)
    const offer = await peerConnection!.createOffer()
    await peerConnection!.setLocalDescription(offer)
    websocketService.sendCallSignal({
      action: 'offer',
      toUserId: userId,
      callType,
      sdp: offer.sdp
    })
  }

  const createAnswer = async () => {
    const userId = getUserId()
    console.log('createAnswer, targetUserId:', userId)
    const answer = await peerConnection!.createAnswer()
    await peerConnection!.setLocalDescription(answer)
    websocketService.sendCallSignal({
      action: 'answer',
      toUserId: userId,
      callType,
      sdp: answer.sdp
    })
  }

  const setRemoteDescription = async (sdp: string, type: 'offer' | 'answer') => {
    console.log('setRemoteDescription, type:', type)
    await peerConnection!.setRemoteDescription(new RTCSessionDescription({ type, sdp }))
  }

  const addIceCandidate = async (candidate: string, sdpMid: string, sdpMLineIndex: number) => {
    console.log('addIceCandidate')
    await peerConnection?.addIceCandidate(new RTCIceCandidate({ candidate, sdpMid, sdpMLineIndex }))
  }

  const startTimer = () => {
    if (durationTimer) clearInterval(durationTimer)
    durationTimer = setInterval(() => callDuration.value++, 1000) as unknown as number
  }

  const hangup = () => {
    console.log('hangup')
    if (peerConnection) peerConnection.close()
    if (localStream) localStream.getTracks().forEach(track => track.stop())
    if (durationTimer) clearInterval(durationTimer)
    peerConnection = null
    localStream = null
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
    isConnected.value = false
    callDuration.value = 0
  }

  return {
<<<<<<< HEAD
    isConnected, callDuration, localStream,
    createOffer, handleOffer, handleAnswer, addIceCandidate, hangup
=======
    isConnected,
    callDuration,
    localStream,
    startLocalStream,
    createPeerConnection,
    addLocalStreamToPeer,
    createOffer,
    createAnswer,
    setRemoteDescription,
    addIceCandidate,
    hangup
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
  }
}
