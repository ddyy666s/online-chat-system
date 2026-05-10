// composables/useCallSignal.ts
import { websocketService } from '@/utils/websocket'

export const useCallSignal = () => {
  const sendOffer = (toUserId: number, callType: 'voice' | 'video', sdp: string) => {
    websocketService.sendCallSignal({
      action: 'offer',
      toUserId,
      callType,
      sdp
    })
  }

  const sendAnswer = (toUserId: number, callType: 'voice' | 'video', sdp: string) => {
    websocketService.sendCallSignal({
      action: 'answer',
      toUserId,
      callType,
      sdp
    })
  }

  const sendIceCandidate = (toUserId: number, callType: 'voice' | 'video', candidate: any) => {
    websocketService.sendCallSignal({
      action: 'ice-candidate',
      toUserId,
      callType,
      candidate: candidate.candidate,
      sdpMid: candidate.sdpMid,
      sdpMLineIndex: candidate.sdpMLineIndex
    })
  }

  const sendHangup = (toUserId: number, callType: 'voice' | 'video') => {
    websocketService.sendCallSignal({
      action: 'hangup',
      toUserId,
      callType
    })
  }

  return { sendOffer, sendAnswer, sendIceCandidate, sendHangup }
}