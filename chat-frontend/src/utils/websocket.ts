/// <reference types="vite/client" />

import { useUserStore } from '@/stores/userStore'

type MessageCallback = (data: any) => void

class WebSocketService {
  private ws: WebSocket | null = null
  private reconnectTimer: number | null = null
  private heartbeatTimer: number | null = null
  private messageCallbacks: MessageCallback[] = []
  private statusCallbacks: MessageCallback[] = []
  private groupMessageCallbacks: MessageCallback[] = []
  
  connect() {
    const userStore = useUserStore()
    const token = userStore.token
    
    if (!token) {
      console.warn('未登录，无法连接WebSocket')
      return
    }
    
    const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'}?token=${token}`
    console.log('WebSocket连接中:', wsUrl)
    this.ws = new WebSocket(wsUrl)
    
    this.ws.onopen = () => {
      console.log('WebSocket连接成功')
      this.startHeartbeat()
      if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    }
    
    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data)
      console.log('WebSocket收到消息:', data)
      
      if (data.type === 'message') {
        this.messageCallbacks.forEach(cb => cb(data))
      } else if (data.type === 'status') {
        this.statusCallbacks.forEach(cb => cb(data))
      } else if (data.type === 'group_message') {
        this.groupMessageCallbacks.forEach(cb => cb(data))
      }
  }
    
    this.ws.onclose = () => {
      console.log('WebSocket断开，尝试重连...')
      this.stopHeartbeat()
      this.reconnect()
    }
    
    this.ws.onerror = (error) => {
      console.error('WebSocket错误', error)
    }
  }
  
  private reconnect() {
    if (this.reconnectTimer) return
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect()
    }, 3000)
  }
  
  private startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'ping' }))
      }
    }, 30000)
  }
  
  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }
  
  sendMessage(toUserId: number, content: string, messageType: number = 1) {
    console.log('sendMessage调用:', { toUserId, content, messageType })
    
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message = JSON.stringify({
        type: 'message',
        toUserId: toUserId,
        content: content,
        messageType: messageType
      })
      console.log('发送WebSocket消息:', message)
      this.ws.send(message)
    } else {
      console.warn('WebSocket未连接，状态:', this.ws?.readyState)
    }
  }
  
  onMessage(callback: MessageCallback) {
    this.messageCallbacks.push(callback)
  }
  
  onStatus(callback: MessageCallback) {
    this.statusCallbacks.push(callback)
  }
  
  
  disconnect() {
    this.stopHeartbeat()
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }
  sendGroupMessage(groupId: number, content: string, messageType: number = 1) {
  if (this.ws && this.ws.readyState === WebSocket.OPEN) {
    this.ws.send(JSON.stringify({
      type: 'group_message',
      groupId: groupId,
      content: content,
      messageType: messageType
    }))
  } else {
    console.warn('WebSocket未连接')
  }
}

onGroupMessage(callback: MessageCallback) {
  this.groupMessageCallbacks.push(callback)
}
}

export const websocketService = new WebSocketService()