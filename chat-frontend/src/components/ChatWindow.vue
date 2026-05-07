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
          <el-button :icon="Download" circle text @click="downloadHistory" />
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

const loadHistory = async (reset = true) => {
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
  } finally {
    loading.value = false
  }
}

const sendMessage = () => {
  if (!inputContent.value.trim()) return
  websocketService.sendMessage(props.friend.userId, inputContent.value)
  inputContent.value = ''
}

const downloadHistory = async () => {
  await downloadChatHistoryApi(props.friend.userId, props.friend.nickname)
  ElMessage.success('开始下载')
}

const onNewMessage = (data: any) => {
  if (data.fromUserId === props.friend.userId) {
    messages.value.push({
      id: data.messageId,
      fromUserId: data.fromUserId,
      fromUserNickname: data.fromUserNickname,
      content: data.content,
      sendTime: data.sendTime,
      isRecalled: false
    })
    nextTick(() => {
      scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
    })
    markAsRead()
  }
}

const markAsRead = async () => {
  try {
    await markAsReadApi(props.friend.userId)
    messageStore.clearUnreadForFriend(props.friend.userId)
  } catch (error) {
    console.error(error)
  }
}

watch(() => props.friend, () => {
  if (props.friend) {
    loadHistory(true)
    markAsRead()
  }
}, { immediate: true })

onMounted(() => {
  websocketService.onMessage(onNewMessage)
})

onUnmounted(() => {
  markAsRead()
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