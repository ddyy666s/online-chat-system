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
  const isConnected = ref(false)
  const callDuration = ref(0)
  let pc: RTCPeerConnection | null = null
  let localStream: MediaStream | null = null
  let timer: number | null = null

  const configuration = { iceServers: getIceServers() }

  const startLocalStream = async () => {
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
    isConnected.value = false
    callDuration.value = 0
  }

  return {
    isConnected, callDuration, localStream,
    createOffer, handleOffer, handleAnswer, addIceCandidate, hangup
  }
}
