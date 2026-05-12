<template>
  <div ref="containerRef" class="infinite-scroll-container" @scroll="handleScroll">
    <div class="infinite-scroll-content">
      <slot />
    </div>
    <div v-if="loading && hasMore" class="infinite-scroll-loading">
      <el-icon class="is-loading">
        <Loading />
      </el-icon>
      <span>加载中...</span>
    </div>
    <div v-if="!hasMore && !loading && listLength > 0" class="infinite-scroll-no-more">
      没有更多了
    </div>
    <div v-if="error" class="infinite-scroll-error">
      <span>{{ errorMessage }}</span>
      <el-button type="primary" link @click="retry">重试</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 无限滚动加载组件 @component */
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'

/** 组件属性：加载状态、是否还有更多、错误信息、触发距离、立即检查 */
const props = withDefaults(defineProps<{
  loading?: boolean
  hasMore?: boolean
  error?: boolean
  errorMessage?: string
  distance?: number
  immediateCheck?: boolean
}>(), {
  loading: false,
  hasMore: true,
  error: false,
  errorMessage: '加载失败',
  distance: 100,
  immediateCheck: true
})

/** 组件事件：加载更多、重试 */
const emit = defineEmits<{
  (e: 'load'): void
  (e: 'retry'): void
}>()

/** 滚动容器引用 */
const containerRef = ref<HTMLElement>()
/** 列表项数量 */
const listLength = ref(0)

/** 检查是否需要加载更多 @returns void */
const checkScroll = () => {
  if (!containerRef.value) return
  if (props.loading) return
  if (!props.hasMore) return

  const { scrollTop, scrollHeight, clientHeight } = containerRef.value
  if (scrollHeight - scrollTop - clientHeight < props.distance) {
    emit('load')
  }
}

/** 滚动事件定时器 ID（用于节流） */
let timer: number | null = null
/** 带节流的滚动事件处理 @returns void */
const handleScroll = () => {
  if (timer) return
  timer = window.setTimeout(() => {
    checkScroll()
    timer = null
  }, 100)
}

/** 重试加载 @returns void */
const retry = () => {
  emit('retry')
}

/** 更新列表项数量 @returns void */
const updateListLength = () => {
  if (containerRef.value) {
    const contentEl = containerRef.value.querySelector('.infinite-scroll-content')
    if (contentEl) {
      listLength.value = contentEl.children.length
    }
  }
}

/** 监听加载状态变化，加载完成后重新检查和更新 */
watch(() => props.loading, (newVal, oldVal) => {
  if (!newVal && oldVal) {
    nextTick(() => {
      updateListLength()
      checkScroll()
    })
  }
})

/** 组件挂载后根据配置立即检查 */
onMounted(() => {
  if (props.immediateCheck) {
    nextTick(() => {
      checkScroll()
    })
  }
})

/** 组件卸载时清理定时器 */
onUnmounted(() => {
  if (timer) {
    clearTimeout(timer)
  }
})
</script>

<style scoped>
.infinite-scroll-container {
  height: 100%;
  overflow-y: auto;
}

.infinite-scroll-loading,
.infinite-scroll-no-more,
.infinite-scroll-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  font-size: 13px;
  color: #909399;
}

.infinite-scroll-loading .el-icon {
  font-size: 16px;
}

.infinite-scroll-error {
  color: #f56c6c;
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
