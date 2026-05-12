<template>
  <el-dialog v-model="visible" :title="title" :width="width" :before-close="handleCancel"
    class="beautiful-confirm" top="30vh" center>
    <div class="confirm-body">
      <div class="confirm-icon" :class="type">
        <el-icon :size="48">
          <WarningFilled v-if="type === 'warning'" />
          <CircleCloseFilled v-else-if="type === 'danger'" />
          <SuccessFilled v-else />
        </el-icon>
      </div>
      <div class="confirm-message">{{ message }}</div>
    </div>
    <template #footer>
      <div class="confirm-footer">
        <el-button class="btn-cancel" @click="handleCancel">{{ cancelText }}</el-button>
        <el-button class="btn-confirm" :type="btnType" @click="handleConfirm" :icon="confirmIcon">
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { WarningFilled, CircleCloseFilled, SuccessFilled } from '@element-plus/icons-vue'

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

const btnType = computed(() => props.type === 'danger' ? 'danger' : 'primary')
const confirmIcon = computed(() => {
  if (props.type === 'danger') return 'Delete'
  if (props.type === 'warning') return 'WarningFilled'
  return 'Check'
})

const handleConfirm = () => { emit('confirm'); visible.value = false }
const handleCancel = () => { emit('cancel'); visible.value = false }
</script>

<style scoped>
.confirm-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 24px 16px 8px;
}

.confirm-icon :deep(.el-icon) { display: flex; }
.confirm-icon.warning svg { color: #e6a23c; }
.confirm-icon.danger svg { color: #f56c6c; }
.confirm-icon.info svg { color: #409eff; }

.confirm-message {
  font-size: 15px;
  color: #303133;
  text-align: center;
  line-height: 1.6;
  max-width: 320px;
}

.confirm-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 0 0 8px;
}

.btn-cancel {
  min-width: 100px;
  border-radius: 20px;
}

.btn-confirm {
  min-width: 100px;
  border-radius: 20px;
}

.beautiful-confirm :deep(.el-dialog__header) {
  display: none;
}

.beautiful-confirm :deep(.el-dialog__body) {
  padding: 8px 24px 16px;
}
</style>