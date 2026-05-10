<template>
  <el-dialog v-model="visible" :title="title" :width="width" :before-close="handleCancel">
    <div class="confirm-content">
      <el-icon :size="24" :color="type === 'danger' ? '#f56c6c' : '#409eff'">
        <WarningFilled v-if="type === 'warning'" />
        <CircleCloseFilled v-else-if="type === 'danger'" />
        <InfoFilled v-else />
      </el-icon>
      <span class="message">{{ message }}</span>
    </div>
    <template #footer>
      <el-button @click="handleCancel">{{ cancelText }}</el-button>
      <el-button :type="type === 'danger' ? 'danger' : 'primary'" @click="handleConfirm">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { WarningFilled, CircleCloseFilled, InfoFilled } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: boolean
  title?: string
  message: string
  type?: 'info' | 'warning' | 'danger'
  confirmText?: string
  cancelText?: string
  width?: string
}>()

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleConfirm = () => {
  emit('confirm')
  visible.value = false
}

const handleCancel = () => {
  emit('cancel')
  visible.value = false
}
</script>

<style scoped>
.confirm-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
}

.message {
  font-size: 14px;
  color: #606266;
}
</style>