<template>
  <div class="chat-window">
    <div class="chat-header">
      <div class="friend-info">
        <el-avatar :size="40" :src="friend?.avatar">
          {{ friend?.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="friend-detail">
          <div class="name">{{ friend?.remark || friend?.nickname }}</div>
          <div class="status">{{ friend?.isOnline ? '在线' : '离线' }}</div>
        </div>
      </div>
      <div class="actions">
        <el-tooltip content="下载聊天记录" placement="bottom">
          <el-button :icon="Download" circle text @click="downloadHistory" />
        </el-tooltip>
      </div>
    </div>

    <div class="message-list" ref="messageListRef">
      <div v-for="msg in messages" :key="msg.id" class="message-item"
        :class="{ own: msg.fromUserId === currentUserId }">
        <el-avatar :size="32" :src="msg.fromUserAvatar">
          {{ msg.fromUserNickname?.charAt(0) || '?' }}
        </el-avatar>
        <div class="message-content">
          <div class="message-info">
            <span class="name">{{ msg.fromUserNickname || '未知用户' }}</span>
            <span class="time">{{ formatRelativeTime(msg.sendTime) }}</span>
          </div>
          <div class="message-bubble">
            <span v-if="msg.isRecalled" class="recalled">对方撤回了一条消息</span>
            <span v-else>{{ msg.content }}</span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="2" animated />
      </div>

      <div ref="scrollBottomRef"></div>
    </div>

    <div class="message-input">
      <el-input v-model="inputContent" type="textarea" :rows="3" placeholder="请输入消息..."
        @keyup.ctrl.enter="sendMessage" />
      <div class="input-actions">
        <el-button type="primary" @click="sendMessage">
          发送 (Ctrl+Enter)
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getChatHistoryApi, downloadChatHistoryApi, markAsReadApi } from '@/api/message'
import { formatRelativeTime } from '@/utils/date'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useMessageStore } from '@/stores/messageStore'

const props = defineProps<{
  friend: any
}>()

const userStore = useUserStore()
const messageStore = useMessageStore()
const currentUserId = userStore.userInfo?.id

const messages = ref<any[]>([])
const inputContent = ref('')
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const scrollBottomRef = ref<HTMLElement>()

// 加载历史消息
const loadHistory = async (reset = true) => {
  if (!props.friend?.userId) {
    console.warn('loadHistory: friendId 无效', props.friend)
    return
  }

  if (reset) {
    page.value = 1
    hasMore.value = true
    messages.value = []
  }

  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getChatHistoryApi(props.friend.userId, page.value, 20)
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

// 发送消息
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
    sendTime: new Date().toISOString(),
    isRecalled: false
  }

  // 立即显示消息（发送方）
  messages.value.push(tempMsg)
  inputContent.value = ''

  nextTick(() => {
    scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
  })

  // 发送给接收方
  websocketService.sendMessage(props.friend.userId, content)
}

// 下载聊天记录
const downloadHistory = async () => {
  if (!props.friend) return
  try {
    await downloadChatHistoryApi(props.friend.userId, props.friend.nickname || '好友')
    ElMessage.success('开始下载')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 接收新消息
const onNewMessage = (data: any) => {
  console.log('ChatWindow 收到WebSocket消息:', data)

  if (!data.fromUserId) {
    console.warn('消息缺少 fromUserId')
    return
  }

  // 只处理来自当前聊天对象的消息
  if (props.friend && data.fromUserId === props.friend.userId) {
    const newMsg = {
      id: data.messageId,
      fromUserId: data.fromUserId,
      fromUserNickname: data.fromUserNickname,
      fromUserAvatar: data.fromUserAvatar,
      content: data.content,
      sendTime: data.sendTime,
      isRecalled: false
    }
    messages.value.push(newMsg)
    nextTick(() => {
      scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
    })
    markAsRead()
  }
}

// 标记已读
const markAsRead = async () => {
  if (!props.friend) return
  try {
    await markAsReadApi(props.friend.userId)
    messageStore.clearUnreadForFriend(props.friend.userId)
  } catch (error) {
    console.error(error)
  }
}

// 监听好友切换
watch(() => props.friend, (newFriend) => {
  console.log('ChatWindow: friend 变化:', newFriend)
  if (newFriend?.userId) {
    loadHistory(true)
    markAsRead()
  }
}, { immediate: true, deep: true })

// 注册 WebSocket 回调
onMounted(() => {
  websocketService.onMessage(onNewMessage)
  console.log('ChatWindow 已注册 WebSocket 消息回调')
})

onUnmounted(() => {
  if (props.friend) {
    markAsRead()
  }
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

.recalled {
  color: #909399;
  font-style: italic;
}

.message-input {
  padding: 12px;
  border-top: 1px solid #e4e7ed;
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