<template>
  <el-dialog v-model="visible" :title="title" :width="width" :before-close="handleCancel" top="30vh" center>
    <div class="prompt-body">
      <div class="prompt-icon" :class="type">
        <el-icon :size="48">
          <WarningFilled v-if="type === 'warning'" />
          <CircleCloseFilled v-else-if="type === 'danger'" />
          <SuccessFilled v-else />
        </el-icon>
      </div>
      <div class="prompt-message">{{ message }}</div>
      <el-input v-model="inputVal" :placeholder="placeholder" class="prompt-input"
        @keyup.enter="handleConfirm" />
      <div v-if="inputError" class="prompt-error">{{ inputError }}</div>
    </div>
    <template #footer>
      <div class="prompt-footer">
        <el-button class="btn-cancel" @click="handleCancel">{{ cancelText }}</el-button>
        <el-button class="btn-confirm" :type="btnType" @click="handleConfirm">{{ confirmText }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/** 输入提示对话框组件，支持表单验证 @component */
import { computed, ref, watch } from 'vue'
import { WarningFilled, CircleCloseFilled, SuccessFilled } from '@element-plus/icons-vue'

/** 组件属性：显示状态、标题、消息、类型、按钮文本、输入框配置 */
const props = defineProps<{
  modelValue: boolean
  title?: string
  message: string
  type?: 'info' | 'warning' | 'danger'
  confirmText?: string
  cancelText?: string
  width?: string
  placeholder?: string
  inputValue?: string
  inputPattern?: RegExp
  inputErrorMessage?: string
}>()

/** 组件事件：更新显示状态、确认（带输入值）、取消 */
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

/** 对话框可见性计算属性（双向绑定） */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})
/** 输入框值 */
const inputVal = ref(props.inputValue || '')
/** 输入错误提示 */
const inputError = ref('')

/** 打开对话框时重置输入值 */
watch(() => props.modelValue, (open) => {
  if (open) inputVal.value = props.inputValue || ''
})

/** 确认按钮类型 @returns 'danger' | 'primary' */
const btnType = computed(() => props.type === 'danger' ? 'danger' : 'primary')

/** 确认操作，带输入验证 @returns void */
const handleConfirm = () => {
  if (props.inputPattern && !props.inputPattern.test(inputVal.value)) {
    inputError.value = props.inputErrorMessage || '输入格式不正确'
    return
  }
  inputError.value = ''
  emit('confirm', inputVal.value)
  visible.value = false
}

/** 取消操作 @returns void */
const handleCancel = () => {
  emit('cancel')
  visible.value = false
}
</script>

<style scoped>
.prompt-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 32px 16px 8px;
}
.prompt-icon :deep(.el-icon) { display: flex; }
.prompt-icon.warning svg { color: var(--color-warning); }
.prompt-icon.danger svg { color: var(--color-danger); }
.prompt-icon.info svg { color: var(--color-primary); }
.prompt-message { font-size: 16px; color: var(--text-primary); text-align: center; line-height: 1.7; font-weight: 500; }
.prompt-input { width: 85%; }
.prompt-input :deep(.el-input__wrapper) { border-radius: 12px !important; }
.prompt-error { color: var(--color-danger); font-size: 13px; margin-top: -8px; }
.prompt-footer { display: flex; justify-content: center; gap: 12px; padding: 0 0 8px; }
.btn-cancel { min-width: 110px; border-radius: 12px !important; font-weight: 600 !important; }
.btn-confirm { min-width: 110px; border-radius: 12px !important; font-weight: 600 !important; }
</style>
