<template>
  <el-dialog v-model="visible" title="下载聊天记录" width="400px" :before-close="handleClose">
    <div class="download-dialog">
      <div class="info">
        <p>当前共有 <strong>{{ totalMessages }}</strong> 条聊天记录</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="下载条数" prop="limit">
          <el-input-number v-model="form.limit" :min="1" :max="maxLimit" :step="10" controls-position="right"
            style="width: 100%" />
          <div class="tip">最多可下载 {{ maxLimit }} 条</div>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleDownload">下载</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/** 聊天记录下载对话框组件 @component */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

/** 组件属性：显示状态、好友信息、总消息数、最大下载限制 */
const props = defineProps<{
  modelValue: boolean
  friendId: number
  friendName: string
  totalMessages: number
  maxLimit?: number
}>()

/** 组件事件：更新显示状态、执行下载 */
const emit = defineEmits(['update:modelValue', 'download'])

/** 对话框可见性 */
const visible = ref(false)
/** 下载按钮加载状态 */
const loading = ref(false)
/** 表单引用 */
const formRef = ref()

/** 最大下载条数 */
const maxLimit = props.maxLimit || 500

/** 表单数据 */
const form = ref({
  limit: Math.min(100, props.totalMessages || 100)
})

/** 表单校验规则 */
const rules = {
  limit: [
    { required: true, message: '请输入下载条数' },
    { type: 'number', min: 1, max: maxLimit, message: `请输入1-${maxLimit}之间的数字` }
  ]
}

/** 关闭对话框 @returns void */
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

/** 执行下载 @returns Promise<void> */
const handleDownload = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    emit('download', form.value.limit)
    handleClose()
  } catch (error) {
    ElMessage.error('下载失败')
  } finally {
    loading.value = false
  }
}

/** 监听外部显示状态变化 */
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    form.value.limit = Math.min(100, props.totalMessages || 100)
    formRef.value?.clearValidate()
  }
})
</script>

<style scoped>
.download-dialog {
  padding: 8px 0;
}

.info {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.info strong {
  color: #409eff;
  font-size: 20px;
}

.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>
