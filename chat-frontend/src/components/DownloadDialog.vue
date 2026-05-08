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
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  friendId: number
  friendName: string
  totalMessages: number
  maxLimit?: number
}>()

const emit = defineEmits(['update:modelValue', 'download'])

const visible = ref(false)
const loading = ref(false)
const formRef = ref()

const maxLimit = props.maxLimit || 500

const form = ref({
  limit: Math.min(100, props.totalMessages || 100)
})

const rules = {
  limit: [
    { required: true, message: '请输入下载条数' },
    { type: 'number', min: 1, max: maxLimit, message: `请输入1-${maxLimit}之间的数字` }
  ]
}

const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

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