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
  private callSignalCallbacks: MessageCallback[] = []
  
  // 添加：检查连接状态
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN
  }
  
  connect() {
    const userStore = useUserStore()
    const token = userStore.token
    
    if (!token) {
      console.warn('未登录，无法连接WebSocket')
      return
    }
    
    const cleanToken = token ? token.replace(/"/g, '') : ''
    const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080/ws'}?token=${cleanToken}`
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
      } else if (data.type === 'call') {
        this.callSignalCallbacks.forEach(cb => cb(data))
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
  
  // 发送单聊消息
  sendMessage(toUserId: number, content: string, messageType: number = 1, duration?: number) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message: any = {
        type: 'message',
        toUserId: toUserId,
        content: content,
        messageType: messageType
      }
      if (duration !== undefined && messageType === 4) {
        message.duration = duration
      }
      console.log('发送消息:', message)
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket未连接')
    }
  }
  
  // 发送群消息
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
  
  // 发送通话信令
  sendCallSignal(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('发送通话信令:', data)
      this.ws.send(JSON.stringify({ type: 'call', ...data }))
    } else {
      console.warn('WebSocket未连接，无法发送通话信令')
    }
  }
  
  // 注册回调
  onMessage(callback: MessageCallback) {
    this.messageCallbacks.push(callback)
  }
  
  onStatus(callback: MessageCallback) {
    this.statusCallbacks.push(callback)
  }
  
  onGroupMessage(callback: MessageCallback) {
    this.groupMessageCallbacks.push(callback)
  }
  
  onCallSignal(callback: MessageCallback) {
    this.callSignalCallbacks.push(callback)
  }
  
  disconnect() {
    this.stopHeartbeat()
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }
}



export const websocketService = new WebSocketService()