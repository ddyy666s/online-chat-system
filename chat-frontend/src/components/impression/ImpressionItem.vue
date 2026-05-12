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
      <div class="time">{{ formatDate(impression.createdAt) }}</div>
    </div>
    <div v-if="showDelete" class="actions">
      <el-button type="danger" size="small" text @click="$emit('delete', impression.id)">
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 评价项展示组件 @component */
import { computed } from 'vue'
import type { ImpressionVO } from '@/api/impression'
import { formatDate } from '@/utils/date'

/** 组件属性：评价对象、类型（to-me: 我收到的, by-me: 我给出的） */
const props = defineProps<{
  impression: ImpressionVO
  type: 'to-me' | 'by-me'
}>()

/** 组件事件：删除评价 */
defineEmits<{
  delete: [id: number]
}>()

/** 显示的昵称 */
const displayName = computed(() => {
  return props.type === 'to-me'
    ? props.impression.fromUserNickname
    : props.impression.toUserNickname
})

/** 头像 URL */
const avatarUrl = computed(() => {
  return props.type === 'to-me'
    ? props.impression.fromUserAvatar
    : props.impression.toUserAvatar
})

/** 头像占位字符 */
const avatarText = computed(() => {
  return displayName.value?.charAt(0) || 'U'
})

/** 是否显示删除按钮——仅"我给出的"可删除 */
const showDelete = computed(() => props.type === 'by-me')
</script>

<style scoped>
.impression-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #e4e7ed;
}

.impression-item .content {
  flex: 1;
}

.impression-item .name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.impression-item .text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
  word-break: break-word;
}

.impression-item .time {
  font-size: 12px;
  color: #909399;
}

.impression-item .actions {
  display: flex;
  align-items: center;
}
</style>
