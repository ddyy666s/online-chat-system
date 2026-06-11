<template>
  <div class="impression-item">
    <div class="avatar">
      <el-avatar :size="36" :src="avatarUrl">
        {{ avatarText }}
      </el-avatar>
    </div>
    <div class="content">
      <div class="name">{{ displayName }}</div>
      <div class="text">{{ impression.content }}</div>
      <div class="time">{{ formattedTime }}</div>
    </div>
    <div v-if="showDelete" class="actions">
      <el-button class="delete-btn" size="small" @click="handleDelete">
        <el-icon :size="13"><Delete /></el-icon>
        <span>删除</span>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 评价项展示组件 @component */
import { computed } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import type { ImpressionVO } from '@/api/impression'
import { formatDate } from '@/utils/date'

/** 评价类型 */
type ImpressionType = 'to-me' | 'by-me'

/** 组件属性 */
const props = defineProps<{
  impression: ImpressionVO
  type: ImpressionType
}>()

/** 组件事件 */
const emit = defineEmits<{
  delete: [id: number]
}>()

/** 获取用户信息的映射配置 */
const userFieldMap = {
  'to-me': {
    name: 'fromUserNickname' as const,
    avatar: 'fromUserAvatar' as const
  },
  'by-me': {
    name: 'toUserNickname' as const,
    avatar: 'toUserAvatar' as const
  }
} as const

/** 显示的昵称 */
const displayName = computed(() => {
  const field = userFieldMap[props.type].name
  return props.impression[field] || '未知用户'
})

/** 头像 URL */
const avatarUrl = computed(() => {
  const field = userFieldMap[props.type].avatar
  return props.impression[field] || ''
})

/** 头像占位字符 */
const avatarText = computed(() => {
  return displayName.value.charAt(0).toUpperCase() || 'U'
})

/** 是否显示删除按钮——仅"我给出的"可删除 */
const showDelete = computed(() => props.type === 'by-me')

/** 格式化后的时间（缓存计算结果） */
const formattedTime = computed(() => formatDate(props.impression.createdAt))

/** 删除处理 */
const handleDelete = () => {
  emit('delete', props.impression.id)
}
</script>

<style scoped>
.impression-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-color-lighter);
  transition: all 0.2s ease;
}

.impression-item:hover {
  background: #fafaff;
  margin: 0 -8px;
  padding: 14px 8px;
  border-radius: 12px;
  border-bottom-color: transparent;
}

.content {
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
}

.name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--text-primary);
}

.text {
  font-size: 14px;
  color: var(--text-regular);
  margin-bottom: 4px;
  word-break: break-word;
  line-height: 1.6;
}

.time {
  font-size: 12px;
  color: var(--text-secondary);
}

.actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.delete-btn {
  height: 32px;
  padding: 0 14px !important;
  border-radius: 8px !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  gap: 4px;
  border: 1.5px solid var(--color-danger) !important;
  color: var(--color-danger) !important;
  background: #fff5f5 !important;
  transition: all 0.2s ease !important;
  opacity: 0;
  cursor: pointer;
}

.impression-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: var(--color-danger) !important;
  color: white !important;
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(255, 118, 117, 0.3);
}

.delete-btn:active {
  transform: scale(0.98);
}
</style>