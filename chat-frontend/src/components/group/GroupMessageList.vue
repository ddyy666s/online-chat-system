<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <div v-if="loading && messages.length === 0" class="loading">
      <el-skeleton :rows="2" animated />
    </div>

    <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ own: msg.fromUserId === currentUserId }">
      <el-avatar :size="32" :src="msg.fromUserAvatar || ''">
        {{ msg.fromUserNickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="message-content">
        <div class="message-info">
          <span class="name">{{ msg.fromUserNickname }}</span>
          <span class="time">{{ formatRelativeTime(msg.sendTime) }}</span>
        </div>
        <div class="message-bubble">
          <span>{{ msg.content }}</span>
        </div>
      </div>
    </div>

    <div v-if="loading && messages.length > 0" class="loading-more">
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
      <span>加载更多...</span>
    </div>

    <div ref="scrollBottomRef"></div>
  </div>
</template>

<script setup lang="ts">
/** 群聊消息列表组件 @component */
import { ref, watch, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/date'

/** 组件属性：消息列表、当前用户 ID、加载状态 */
const props = defineProps<{
  messages: any[]
  currentUserId: number | undefined
  loading: boolean
}>()

/** 组件事件：加载更多 */
const emit = defineEmits(['loadMore'])

/** 列表容器引用 */
const listRef = ref<HTMLElement>()
/** 底部锚点引用 */
const scrollBottomRef = ref<HTMLElement>()

/** 滚动到底部 @returns void */
const scrollToBottom = () => {
  nextTick(() => {
    scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
  })
}

/** 滚动事件处理，触底加载更多 @returns void */
const handleScroll = () => {
  if (!listRef.value) return
  const { scrollTop, scrollHeight, clientHeight } = listRef.value
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    emit('loadMore')
  }
}

/** 监听消息数量变化自动滚动到底部 */
watch(() => props.messages.length, () => {
  scrollToBottom()
}, { immediate: true })

defineExpose({ scrollToBottom })
</script>

<style scoped>
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

.loading {
  padding: 16px;
  text-align: center;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #909399;
  font-size: 12px;
}

.is-loading {
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}
</style>
