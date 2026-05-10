<template>
  <div v-if="notice" class="notice-bar" @click="$emit('click')">
    <el-icon>
      <InfoFilled />
    </el-icon>
    <span class="notice-text">{{ notice }}</span>
    <el-button text size="small">详情</el-button>
  </div>
</template>

<script setup lang="ts">
import { InfoFilled } from '@element-plus/icons-vue'

// 修复：只接受 string | null，过滤掉 undefined
defineProps<{
  notice: string | null
}>()

defineEmits<{
  (e: 'click'): void
}>()
</script>

<style scoped>
.notice-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fef0e6;
  border-bottom: 1px solid #e4e7ed;
  cursor: pointer;
}

.notice-bar .notice-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: #e6a23c;
}
</style>