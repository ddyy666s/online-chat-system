import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const APP_ID = '023b95b1-701e-45bf-802d-0c3cbf5d3874'

export function useAliRTC() {
  const isConnected = ref(false)
  const localStream = ref<MediaStream | null>(null)
  const remoteStream = ref<MediaStream | null>(null)
  const callDuration = ref(0)

  let client: any = null
  let durationTimer: ReturnType<typeof setInterval> | null = null

  const getToken = async (channelId: string) => {
    const res = await request.post('/rtc/token', { channelId })
    return res.data as { token: string; appId: string; channelId: string; userId: string }
  }

  const joinChannel = async (channelId: string, userId: string, userName: string, callType: 'voice' | 'video', token: string) => {
    try {
      const AliRTC = (await import('aliyun-rtc-sdk')).default

      client = new AliRTC()

      client.on('stream-added', (remoteStreamInfo: any) => {
        client.subscribe(remoteStreamInfo)
      })

      client.on('stream-subscribed', (remoteStreamInfo: any) => {
        remoteStream.value = remoteStreamInfo.getMediaStream()
      })

      client.on('stream-removed', () => {
        remoteStream.value = null
      })

      const authInfo = { appId: APP_ID, channelId, userId, token }
      await client.joinChannel(authInfo, userName)

      await client.publishLocalAudioStream(true)

      const tracks: MediaStreamTrack[] = []
      const audioTrack = await client.getAudioTrack()
      if (audioTrack) tracks.push(audioTrack)

      if (callType === 'video') {
        await client.publishLocalVideoStream(true)
        const videoTrack = await client.getVideoTrack({ track: AliRTC.AliRtcVideoTrack.AliRtcVideoTrackCamera })
        if (videoTrack) tracks.push(videoTrack)
      }

      localStream.value = new MediaStream(tracks)
      isConnected.value = true
      startTimer()

      return true
    } catch (err) {
      console.error('加入频道失败:', err)
      return false
    }
  }

  const startCall = async (channelId: string, userId: string, userName: string, callType: 'voice' | 'video' = 'voice') => {
    try {
      const tokenData = await getToken(channelId)
      return await joinChannel(channelId, userId, userName, callType, tokenData.token)
    } catch (err) {
      console.error('发起通话失败:', err)
      ElMessage.error('通话失败')
      return false
    }
  }

  const acceptCall = startCall

  const hangup = async () => {
    try {
      if (client) {
        client.off('stream-added')
        client.off('stream-subscribed')
        client.off('stream-removed')
        await client.leave()
        client = null
      }
    } catch (err) {
      console.error('离开频道失败:', err)
    }

    if (localStream.value) {
      localStream.value.getTracks().forEach(t => t.stop())
      localStream.value = null
    }
    if (remoteStream.value) {
      remoteStream.value.getTracks().forEach(t => t.stop())
      remoteStream.value = null
    }
    if (durationTimer) {
      clearInterval(durationTimer)
      durationTimer = null
    }
    isConnected.value = false
    callDuration.value = 0
  }

  const startTimer = () => {
    callDuration.value = 0
    durationTimer = setInterval(() => { callDuration.value++ }, 1000)
  }

  return { isConnected, localStream, remoteStream, callDuration, startCall, acceptCall, hangup, getToken, joinChannel }
}
