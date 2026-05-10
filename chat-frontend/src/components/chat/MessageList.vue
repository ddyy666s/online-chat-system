<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <div v-if="loading && messages.length === 0" class="loading">
      <el-skeleton :rows="2" animated />
    </div>

    <MessageBubble v-for="msg in messages" :key="msg.id" :message="msg" :is-own="msg.fromUserId === currentUserId"
      :show-info="true" />

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
import { ref, watch, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import MessageBubble from './MessageBubble.vue'

const props = defineProps<{
  messages: any[]
  currentUserId: number | undefined
  loading: boolean
}>()

const emit = defineEmits(['loadMore'])

const listRef = ref<HTMLElement>()
const scrollBottomRef = ref<HTMLElement>()

const scrollToBottom = () => {
  nextTick(() => {
    scrollBottomRef.value?.scrollIntoView({ behavior: 'auto' })
  })
}

const handleScroll = () => {
  if (!listRef.value) return
  const { scrollTop, scrollHeight, clientHeight } = listRef.value
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    emit('loadMore')
  }
}

// 监听消息变化，自动滚动到底部
watch(
  () => props.messages.length,
  () => {
    scrollToBottom()
  },
  { immediate: true }
)

defineExpose({ scrollToBottom })
</script>

<style scoped>
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
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