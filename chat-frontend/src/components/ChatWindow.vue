<template>
  <div class="chat-window">
    <ChatHeader :friend="friend" @download="showDownloadDialog = true" />

    <MessageList ref="messageListRef" :messages="messages" :current-user-id="currentUserId" :loading="loading"
      @load-more="loadMore" />

    <MessageInput :current-chat-user-id="friend?.userId" @send="sendMessage" @send-image="sendImage"
      @send-voice="sendVoice" @send-emoji="sendEmoji" @start-voice-call="startVoiceCall"
      @start-video-call="startVideoCall" />

    <!-- 通话弹窗 -->
    <CallDialog v-model="voiceCallVisible" :target-user="friend" call-type="voice" :is-caller="true"
      @end-call="endVoiceCall" />
    <CallDialog v-model="videoCallVisible" :target-user="friend" call-type="video" :is-caller="true"
      @end-call="endVideoCall" />
    <CallDialog v-model="incomingCallVisible" :target-user="incomingCaller" :call-type="incomingCallType"
      :is-caller="false" @end-call="endIncomingCall" />

    <!-- 下载弹窗 -->
    <DownloadDialog v-model="showDownloadDialog" :friend-id="friend?.userId" :friend-name="friend?.nickname"
      :total-messages="totalMessageCount" :max-limit="maxDownloadLimit" @download="handleDownload" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getChatHistoryApi, downloadChatHistoryApi, markAsReadApi } from '@/api/message'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'
import ChatHeader from './chat/ChatHeader.vue'
import MessageList from './chat/MessageList.vue'
import MessageInput from './chat/MessageInput.vue'
import CallDialog from './CallDialog.vue'
import DownloadDialog from './DownloadDialog.vue'

const props = defineProps<{ friend: any }>()
const userStore = useUserStore()
const messageStore = useMessageStore()
const currentUserId = userStore.userInfo?.id

// 数据
const messages = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const totalMessageCount = ref(0)
const showDownloadDialog = ref(false)

// 通话
const voiceCallVisible = ref(false)
const videoCallVisible = ref(false)
const incomingCallVisible = ref(false)
const incomingCaller = ref<any>(null)
const incomingCallType = ref<'voice' | 'video'>('voice')

const maxDownloadLimit = 500
const messageListRef = ref()

// 加载历史
const loadHistory = async (reset = true) => {
  if (!props.friend?.userId) return
  if (reset) {
    page.value = 1
    hasMore.value = true
    messages.value = []
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getChatHistoryApi(props.friend.userId, page.value, 20)
    if (page.value === 1) totalMessageCount.value = res.total

    // 后端返回按时间倒序（最新在前），反转成正序（最早在前）显示
    const newMessages = res.records.reverse()

    if (reset) {
      messages.value = newMessages
    } else {
      // 加载更早的消息，添加到列表前面
      messages.value = [...newMessages, ...messages.value]
    }

    hasMore.value = newMessages.length > 0
    page.value++

    // 首次加载完成后滚动到底部
    if (reset) {
      await nextTick()
      messageListRef.value?.scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadHistory(false)
  }
}

// 发送消息
const sendMessage = (content: string) => {
  if (!props.friend?.userId) return
  websocketService.sendMessage(props.friend.userId, content, 1)
}

const sendImage = (url: string) => {
  if (props.friend?.userId) websocketService.sendMessage(props.friend.userId, url, 2)
}

const sendVoice = (url: string, duration: number) => {
  if (props.friend?.userId) websocketService.sendMessage(props.friend.userId, url, 4, duration)
}

const sendEmoji = (url: string) => {
  if (props.friend?.userId) websocketService.sendMessage(props.friend.userId, url, 2)
}

// 通话
const startVoiceCall = (toUserId: number) => {
  if (!toUserId) return ElMessage.warning('请先选择聊天对象')
  if (toUserId === currentUserId) return ElMessage.error('不能给自己打电话')
  voiceCallVisible.value = true
}

const startVideoCall = (toUserId: number) => {
  if (!toUserId) return ElMessage.warning('请先选择聊天对象')
  if (toUserId === currentUserId) return ElMessage.error('不能给自己打电话')
  videoCallVisible.value = true
}

const endVoiceCall = () => { voiceCallVisible.value = false }
const endVideoCall = () => { videoCallVisible.value = false }
const endIncomingCall = () => { incomingCallVisible.value = false; incomingCaller.value = null }

const handleDownload = async (limit: number) => {
  try {
    await downloadChatHistoryApi(props.friend.userId, props.friend.nickname, limit)
    ElMessage.success('开始下载')
  } catch { ElMessage.error('下载失败') }
}

const markAsRead = async () => {
  if (!props.friend?.userId) return
  try {
    await markAsReadApi(props.friend.userId)
    messageStore.clearUnreadForFriend(props.friend.userId)
  } catch (error) { console.error(error) }
}

// 接收新消息
const onNewMessage = (data: any) => {
  if (props.friend?.userId === data.fromUserId) {
    messages.value.push({
      id: data.messageId,
      fromUserId: data.fromUserId,
      fromUserNickname: data.fromUserNickname,
      fromUserAvatar: data.fromUserAvatar,
      content: data.content,
      messageType: data.messageType || 1,
      duration: data.duration,
      sendTime: data.sendTime,
      isRecalled: false
    })
    // 新消息到达，滚动到底部
    messageListRef.value?.scrollToBottom()
  }
}

// 接收通话信令
const onCallSignal = (data: any) => {
  console.log('收到通话信令:', data)
  if (data.action === 'offer' && data.fromUserId !== currentUserId) {
    incomingCaller.value = { id: data.fromUserId, nickname: data.fromUserNickname }
    incomingCallType.value = data.callType === 'video' ? 'video' : 'voice'
    incomingCallVisible.value = true
  }
}

watch(() => props.friend, (newFriend) => {
  if (newFriend?.userId) {
    loadHistory(true)
    markAsRead()
  }
}, { immediate: true, deep: true })

onMounted(() => {
  websocketService.onMessage(onNewMessage)
  websocketService.onCallSignal(onCallSignal)
})

onUnmounted(() => {
  if (props.friend) markAsRead()
})
</script>

<style scoped>
.chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}
</style>