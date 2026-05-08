<template>
  <div class="chat-window">
    <div class="chat-header">
      <div class="friend-info">
        <el-avatar :size="40" :src="friend?.avatar || ''">
          {{ friend?.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="friend-detail">
          <div class="name">{{ friend?.remark || friend?.nickname }}</div>
          <div class="status">{{ friend?.isOnline ? '在线' : '离线' }}</div>
        </div>
      </div>
      <div class="actions">
        <el-tooltip content="下载聊天记录" placement="bottom">
          <el-button :icon="Download" circle text @click="showDownloadDialog = true" />
        </el-tooltip>
      </div>
    </div>

    <div class="message-list" ref="messageListRef">
      <div v-for="msg in messages" :key="msg.id" class="message-item"
        :class="{ own: msg.fromUserId === currentUserId }">
        <el-avatar :size="32" :src="msg.fromUserAvatar || ''">
          {{ msg.fromUserNickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="message-content">
          <div class="message-info">
            <span class="name">{{ msg.fromUserNickname }}</span>
            <span class="time">{{ formatRelativeTime(msg.sendTime) }}</span>
          </div>
          <div class="message-bubble">
            <span v-if="msg.messageType === 1 && !msg.isRecalled">{{ msg.content }}</span>
            <div v-else-if="msg.messageType === 2 && !msg.isRecalled" class="image-message">
              <el-image :src="msg.content" :preview-src-list="[msg.content]" fit="cover" class="message-image" />
            </div>
            <div v-else-if="msg.messageType === 4 && !msg.isRecalled" class="voice-message"
              @click="togglePlayVoice(msg)">
              <el-icon>
                <VideoPlay v-if="currentPlayingId !== msg.id" />
                <VideoPause v-else />
              </el-icon>
              <span>语音消息</span>
              <span class="voice-duration">{{ formatVoiceDuration(msg.duration) }}</span>
              <div class="voice-wave" v-if="currentPlayingId === msg.id"><span v-for="i in 5" :key="i"></span></div>
            </div>
            <span v-else-if="msg.isRecalled" class="recalled">对方撤回了一条消息</span>
            <span v-else>{{ msg.content }}</span>
          </div>
        </div>
      </div>
      <div v-if="loading" class="loading"><el-skeleton :rows="2" animated /></div>
      <div ref="scrollBottomRef"></div>
    </div>

    <div class="message-area">
      <div class="message-input">
        <el-input v-model="inputContent" type="textarea" :rows="2" placeholder="请输入消息..."
          @keyup.ctrl.enter="sendMessage" />
        <div class="input-actions"><el-button type="primary" @click="sendMessage">发送</el-button></div>
      </div>
      <CommunicationBar :current-chat-user-id="friend?.userId" @send-image="sendImage" @send-voice="sendVoice"
        @send-emoji="sendEmoji" @start-voice-call="startVoiceCall" @start-video-call="startVideoCall" />
    </div>

    <!-- 主动发起的通话 -->
    <CallDialog v-model="voiceCallVisible" :target-user="friend" call-type="voice" :is-caller="true"
      @end-call="endVoiceCall" />
    <CallDialog v-model="videoCallVisible" :target-user="friend" call-type="video" :is-caller="true"
      @end-call="endVideoCall" />

    <!-- 来电弹窗 -->
    <CallDialog v-model="incomingCallVisible" :target-user="incomingCaller" :call-type="incomingCallType"
      :is-caller="false" @end-call="endIncomingCall" />

    <!-- 下载聊天记录弹窗 -->
    <DownloadDialog v-model="showDownloadDialog" :friend-id="friend?.userId" :friend-name="friend?.nickname"
      :total-messages="totalMessageCount" :max-limit="maxDownloadLimit" @download="handleDownload" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, VideoPlay, VideoPause } from '@element-plus/icons-vue'
import { getChatHistoryApi, downloadChatHistoryApi, markAsReadApi } from '@/api/message'
import { formatRelativeTime } from '@/utils/date'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'
import DownloadDialog from './DownloadDialog.vue'
import CommunicationBar from './CommunicationBar.vue'
import CallDialog from './CallDialog.vue'

const props = defineProps<{ friend: any }>()
const userStore = useUserStore()
const messageStore = useMessageStore()
const currentUserId = userStore.userInfo?.id

// 聊天相关
const maxDownloadLimit = 500
const messages = ref<any[]>([])
const inputContent = ref('')
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const totalMessageCount = ref(0)
const showDownloadDialog = ref(false)
const scrollBottomRef = ref<HTMLElement>()

// 语音播放
let currentAudio: HTMLAudioElement | null = null
const currentPlayingId = ref<number | null>(null)

// 通话相关
const voiceCallVisible = ref(false)
const videoCallVisible = ref(false)
const incomingCallVisible = ref(false)
const incomingCaller = ref<any>(null)
const incomingCallType = ref<'voice' | 'video'>('voice')

const formatVoiceDuration = (duration?: number): string => {
  if (!duration || duration <= 0) return '0:05'
  const mins = Math.floor(duration / 60)
  const secs = duration % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const togglePlayVoice = async (msg: any) => {
  if (!msg.content) {
    ElMessage.error('语音文件不存在')
    return
  }
  if (currentPlayingId.value === msg.id && currentAudio) {
    currentAudio.pause()
    currentAudio.currentTime = 0
    currentAudio = null
    currentPlayingId.value = null
    return
  }
  if (currentAudio) {
    currentAudio.pause()
    currentAudio = null
  }
  const audio = new Audio(msg.content)
  currentPlayingId.value = msg.id
  currentAudio = audio
  try {
    await audio.play()
  } catch (err) {
    console.error('播放失败', err)
    ElMessage.error('播放失败')
    currentPlayingId.value = null
    currentAudio = null
  }
  audio.onended = () => {
    currentPlayingId.value = null
    currentAudio = null
  }
}

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
    const newMessages = res.records.reverse()
    messages.value = reset ? newMessages : [...messages.value, ...newMessages]
    hasMore.value = newMessages.length > 0
    page.value++
    if (reset) await nextTick()
    scrollBottomRef.value?.scrollIntoView({ behavior: 'auto' })
  } catch (error) {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const scrollToBottom = () => nextTick(() => scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' }))

const sendMessage = () => {
  if (!inputContent.value.trim()) return ElMessage.warning('请输入内容')
  if (!props.friend?.userId) return
  websocketService.sendMessage(props.friend.userId, inputContent.value, 1)
  inputContent.value = ''
  scrollToBottom()
}

const sendImage = (url: string) => {
  if (props.friend?.userId) {
    websocketService.sendMessage(props.friend.userId, url, 2)
    scrollToBottom()
  }
}

const sendVoice = (url: string, duration: number) => {
  if (props.friend?.userId) {
    websocketService.sendMessage(props.friend.userId, url, 4, duration)
    scrollToBottom()
  }
}

const sendEmoji = (url: string) => {
  if (props.friend?.userId) {
    websocketService.sendMessage(props.friend.userId, url, 2)
    scrollToBottom()
  }
}

// 主动发起通话
const startVoiceCall = (toUserId: number) => {
  if (!toUserId) {
    ElMessage.warning('请先选择聊天对象')
    return
  }
  if (toUserId === currentUserId) {
    ElMessage.error('不能给自己打电话')
    return
  }
  if (!props.friend) {
    ElMessage.error('请先选择聊天对象')
    return
  }
  voiceCallVisible.value = true
}

const startVideoCall = (toUserId: number) => {
  if (!toUserId) {
    ElMessage.warning('请先选择聊天对象')
    return
  }
  if (toUserId === currentUserId) {
    ElMessage.error('不能给自己打电话')
    return
  }
  if (!props.friend) {
    ElMessage.error('请先选择聊天对象')
    return
  }
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
    scrollToBottom()
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
  if (currentAudio) {
    currentAudio.pause()
    currentAudio = null
  }
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

.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.friend-detail .name {
  font-size: 16px;
  font-weight: 500;
}

.friend-detail .status {
  font-size: 12px;
  color: #909399;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-item.own {
  flex-direction: row-reverse;
}

.message-item.own .message-content {
  align-items: flex-end;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.message-info {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.message-bubble {
  background: white;
  padding: 8px 12px;
  border-radius: 12px;
  word-wrap: break-word;
}

.message-item.own .message-bubble {
  background: #95ec69;
}

.message-image {
  max-width: 200px;
  border-radius: 8px;
}

.voice-message {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 20px;
  min-width: 120px;
  transition: all 0.2s;
}

.voice-message:hover {
  background: #e9e9e9;
  transform: scale(1.02);
}

.message-item.own .voice-message {
  background: #95ec69;
}

.voice-duration {
  font-size: 12px;
  color: #666;
  margin-left: auto;
}

.voice-wave {
  display: flex;
  gap: 3px;
  margin-left: 8px;
}

.voice-wave span {
  width: 3px;
  height: 12px;
  background: #409eff;
  border-radius: 1px;
  animation: wave 0.8s ease-in-out infinite;
}

.message-item.own .voice-wave span {
  background: #333;
}

.voice-wave span:nth-child(1) {
  animation-delay: 0s;
}

.voice-wave span:nth-child(2) {
  animation-delay: 0.15s;
}

.voice-wave span:nth-child(3) {
  animation-delay: 0.3s;
}

.voice-wave span:nth-child(4) {
  animation-delay: 0.45s;
}

.voice-wave span:nth-child(5) {
  animation-delay: 0.6s;
}

@keyframes wave {

  0%,
  100% {
    height: 12px;
  }

  50% {
    height: 20px;
  }
}

.recalled {
  color: #909399;
  font-style: italic;
}

.message-area {
  border-top: 1px solid #e4e7ed;
  background: #fff;
}

.message-input {
  padding: 12px;
}

.input-actions {
  margin-top: 8px;
  text-align: right;
}

.loading {
  padding: 16px;
  text-align: center;
}
</style>