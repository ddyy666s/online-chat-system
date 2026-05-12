<template>
  <el-dialog v-model="visible" title="系统通知" width="500px" @close="handleClose">
    <div class="notification-content">
      <div class="notification-title">{{ notification?.title }}</div>
      <el-divider />
      <div class="notification-body">{{ notification?.content }}</div>
      <div class="notification-meta">
        <span>发布者: {{ notification?.adminNickname }}</span>
        <span>{{ notification?.createdAt }}</span>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { SystemNotification } from '@/api/systemNotification'

const props = defineProps<{
  modelValue: boolean
  notification: SystemNotification | null
}>()

const emit = defineEmits(['update:modelValue', 'close'])

const visible = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
  emit('close')
}
</script>

<style scoped>
.notification-content {
  padding: 8px 0;
}

.notification-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.notification-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

.notification-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
