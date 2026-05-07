import { ref, onUnmounted } from 'vue'
import { websocketService } from '@/utils/websocket'

export const useWebSocket = () => {
  const isConnected = ref(false)
  const messageCallbacks: Map<string, (data: any) => void> = new Map()

  // 连接WebSocket
  const connect = () => {
    websocketService.connect()
    isConnected.value = true
  }

  // 发送消息
  const sendMessage = (toUserId: number, content: string, messageType: number = 1) => {
    websocketService.sendMessage(toUserId, content, messageType)
  }

  // 监听消息
  const onMessage = (type: string, callback: (data: any) => void) => {
    messageCallbacks.set(type, callback)
    
    if (type === 'message') {
      websocketService.onMessage(callback)
    } else if (type === 'status') {
      websocketService.onStatus(callback)
    }
  }

  // 断开连接
  const disconnect = () => {
    websocketService.disconnect()
    isConnected.value = false
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    isConnected,
    connect,
    sendMessage,
    onMessage,
    disconnect
  }
}