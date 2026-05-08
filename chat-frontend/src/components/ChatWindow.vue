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

          <!-- 整合后的多类型消息气泡 -->
          <div class="message-bubble">
            <!-- 文字消息 -->
            <span v-if="msg.messageType === 1 && !msg.isRecalled">{{ msg.content }}</span>

            <!-- 图片消息 -->
            <div v-else-if="msg.messageType === 2 && !msg.isRecalled" class="image-message">
              <el-image :src="msg.content" :preview-src-list="[msg.content]" fit="cover" class="message-image" />
            </div>

            <!-- 语音消息 -->
            <div v-else-if="msg.messageType === 4 && !msg.isRecalled" class="voice-message"
              @click="playVoice(msg.content)">
              <el-icon>
                <Microphone />
              </el-icon>
              <span>语音消息</span>
              <span class="voice-duration">0:05</span>
            </div>

            <!-- 撤回消息 -->
            <span v-else-if="msg.isRecalled" class="recalled">对方撤回了一条消息</span>

            <!-- 其他类型 -->
            <span v-else>{{ msg.content }}</span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="2" animated />
      </div>
      <div ref="scrollBottomRef"></div>
    </div>

    <!-- 消息输入 + 通信栏 -->
    <div class="message-area">
      <div class="message-input">
        <el-input v-model="inputContent" type="textarea" :rows="2" placeholder="请输入消息..."
          @keyup.ctrl.enter="sendMessage" />
        <div class="input-actions">
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>

      <CommunicationBar :current-chat-user-id="friend?.userId" @send-image="sendImage" @send-voice="sendVoice"
        @send-emoji="sendEmoji" @start-voice-call="startVoiceCall" @start-video-call="startVideoCall" />
    </div>

    <!-- 语音通话弹窗 -->
    <VoiceCallDialog v-if="voiceCallVisible" v-model="voiceCallVisible" :target-user="friend" :is-caller="true"
      @end-call="endVoiceCall" />

    <!-- 视频通话弹窗 -->
    <VideoCallDialog v-if="videoCallVisible" v-model="videoCallVisible" :target-user="friend" :is-caller="true"
      @end-call="endVideoCall" />

    <!-- 下载聊天记录弹窗 -->
    <DownloadDialog v-model="showDownloadDialog" :friend-id="friend?.userId" :friend-name="friend?.nickname"
      :total-messages="totalMessageCount" :max-limit="maxDownloadLimit" @download="handleDownload" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Microphone } from '@element-plus/icons-vue'
import { getChatHistoryApi, downloadChatHistoryApi, markAsReadApi } from '@/api/message'
import { formatRelativeTime } from '@/utils/date'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'
import DownloadDialog from './DownloadDialog.vue'
import CommunicationBar from './CommunicationBar.vue'
import VoiceCallDialog from './VoiceCallDialog.vue'
import VideoCallDialog from './VideoCallDialog.vue'

const props = defineProps<{
  friend: any
}>()

const userStore = useUserStore()
const messageStore = useMessageStore()
const currentUserId = userStore.userInfo?.id

// 配置
const maxDownloadLimit = 500

// 数据
const messages = ref<any[]>([])
const inputContent = ref('')
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const totalMessageCount = ref(0)
const showDownloadDialog = ref(false)
const scrollBottomRef = ref<HTMLElement>()
const messageListRef = ref<HTMLElement>()

// 通话弹窗
const voiceCallVisible = ref(false)
const videoCallVisible = ref(false)

// ------------------------------
// 加载历史消息
// ------------------------------
const loadHistory = async (reset = true) => {
  if (!props.friend || !props.friend.userId) return
  if (reset) { page.value = 1; hasMore.value = true; messages.value = [] }
  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getChatHistoryApi(props.friend.userId, page.value, 20)
    if (page.value === 1) totalMessageCount.value = res.total

    const newMessages = res.records.reverse()
    messages.value = [...newMessages, ...messages.value]
    hasMore.value = res.records.length > 0
    page.value++

    if (reset) {
      await nextTick()
      scrollBottomRef.value?.scrollIntoView({ behavior: 'auto' })
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载聊天记录失败')
  } finally {
    loading.value = false
  }
}

// 自动滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

// 播放语音
const playVoice = (url: string) => {
  const audio = new Audio(url)
  audio.play().catch(err => console.error('播放失败', err))
}

// ------------------------------
// 发送文本消息
// ------------------------------
const sendMessage = () => {
  if (!inputContent.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  if (!props.friend?.userId) {
    ElMessage.warning('请先选择聊天对象')
    return
  }

  const content = inputContent.value
  const tempMsg = {
    id: Date.now(),
    fromUserId: currentUserId,
    fromUserNickname: userStore.userInfo?.nickname || '我',
    fromUserAvatar: userStore.userInfo?.avatar,
    content: content,
    messageType: 1,
    sendTime: new Date().toISOString(),
    isRecalled: false
  }

  messages.value.push(tempMsg)
  inputContent.value = ''
  totalMessageCount.value++
  scrollToBottom()
  websocketService.sendMessage(props.friend.userId, content)
}

// ------------------------------
// 发送图片
// ------------------------------
const sendImage = (url: string) => {
  const tempMsg = {
    id: Date.now(),
    fromUserId: currentUserId,
    fromUserNickname: userStore.userInfo?.nickname || '我',
    fromUserAvatar: userStore.userInfo?.avatar,
    content: url,
    messageType: 2,
    sendTime: new Date().toISOString(),
    isRecalled: false
  }
  messages.value.push(tempMsg)
  websocketService.sendMessage(props.friend.userId, url, 2)
  scrollToBottom()
}

// ------------------------------
// 发送语音
// ------------------------------
const sendVoice = (url: string) => {
  const tempMsg = {
    id: Date.now(),
    fromUserId: currentUserId,
    fromUserNickname: userStore.userInfo?.nickname || '我',
    fromUserAvatar: userStore.userInfo?.avatar,
    content: url,
    messageType: 4,
    sendTime: new Date().toISOString(),
    isRecalled: false
  }
  messages.value.push(tempMsg)
  websocketService.sendMessage(props.friend.userId, url, 4)
  scrollToBottom()
}

// ------------------------------
// 发送表情
// ------------------------------
const sendEmoji = (url: string) => {
  const tempMsg = {
    id: Date.now(),
    fromUserId: currentUserId,
    fromUserNickname: userStore.userInfo?.nickname || '我',
    fromUserAvatar: userStore.userInfo?.avatar,
    content: url,
    messageType: 2,
    sendTime: new Date().toISOString(),
    isRecalled: false
  }
  messages.value.push(tempMsg)
  websocketService.sendMessage(props.friend.userId, url, 2)
  scrollToBottom()
}

// ------------------------------
// 语音通话
// ------------------------------
const startVoiceCall = () => {
  voiceCallVisible.value = true
}
const endVoiceCall = () => {
  voiceCallVisible.value = false
}

// ------------------------------
// 视频通话
// ------------------------------
const startVideoCall = () => {
  videoCallVisible.value = true
}
const endVideoCall = () => {
  videoCallVisible.value = false
}

// ------------------------------
// 接收新消息
// ------------------------------
const onNewMessage = (data: any) => {
  if (props.friend && data.fromUserId === props.friend.userId) {
    totalMessageCount.value++
    messages.value.push({
      id: data.messageId,
      fromUserId: data.fromUserId,
      fromUserNickname: data.fromUserNickname,
      fromUserAvatar: data.fromUserAvatar,
      content: data.content,
      messageType: data.messageType || 1,
      sendTime: data.sendTime,
      isRecalled: false
    })
    scrollToBottom()
    markAsRead()
  }
}

// ------------------------------
// 下载聊天记录
// ------------------------------
const handleDownload = async (limit: number) => {
  try {
    await downloadChatHistoryApi(props.friend.userId, props.friend.nickname, limit)
    ElMessage.success(`开始下载最近 ${limit} 条聊天记录`)
  } catch (error) {
    console.error(error)
    ElMessage.error('下载失败')
  }
}

// ------------------------------
// 标记已读
// ------------------------------
const markAsRead = async () => {
  if (!props.friend) return
  try {
    await markAsReadApi(props.friend.userId)
    messageStore.clearUnreadForFriend(props.friend.userId)
  } catch (error) {
    console.error(error)
  }
}

// ------------------------------
// 监听切换好友
// ------------------------------
watch(() => props.friend, (newFriend) => {
  if (newFriend?.userId) {
    loadHistory(true)
    markAsRead()
  }
}, { immediate: true, deep: true })

// 注册 WebSocket 回调
onMounted(() => {
  websocketService.onMessage(onNewMessage)
})

// 清理
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

/* 图片消息 */
.image-message {
  cursor: pointer;
}

.message-image {
  max-width: 200px;
  border-radius: 8px;
}

/* 语音消息 */
.voice-message {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 8px;
}

.voice-duration {
  font-size: 12px;
  color: #909399;
}

.recalled {
  color: #909399;
  font-style: italic;
}

/* 输入框区域 */
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