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
/** 确认对话框组件，支持 info/warning/danger 三种类型 @component */
import { computed } from 'vue'
import { WarningFilled, CircleCloseFilled, SuccessFilled } from '@element-plus/icons-vue'

/** 组件属性：显示状态、标题、消息、类型、按钮文本、宽度 */
const props = defineProps<{
  modelValue: boolean
  title?: string
  message: string
  type?: 'info' | 'warning' | 'danger'
  confirmText?: string
  cancelText?: string
  width?: string
}>()

/** 组件事件：更新显示状态、确认、取消 */
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

/** 对话框可见性计算属性（双向绑定） */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/** 确认按钮类型 @returns 'danger' | 'primary' */
const btnType = computed(() => props.type === 'danger' ? 'danger' : 'primary')
/** 确认按钮图标 @returns 图标名称字符串 */
const confirmIcon = computed(() => {
  if (props.type === 'danger') return 'Delete'
  if (props.type === 'warning') return 'WarningFilled'
  return 'Check'
})

/** 确认操作 @returns void */
const handleConfirm = () => { emit('confirm'); visible.value = false }
/** 取消操作 @returns void */
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
