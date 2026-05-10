<template>
  <el-dialog v-model="visible" title="添加评价" width="400px" @close="handleClose">
    <el-form :model="form" label-width="80px">
      <el-form-item label="选择好友">
        <el-select v-model="form.toUserId" placeholder="请选择要评价的好友" filterable style="width: 100%" :loading="loading">
          <el-option v-for="friend in friendList" :key="friend.userId" :label="friend.remark || friend.nickname"
            :value="friend.userId" />
        </el-select>
      </el-form-item>
      <el-form-item label="评价内容">
        <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入评价内容（最多100字）" maxlength="100"
          show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        提交
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  friendList: any[]
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [toUserId: number, content: string]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const submitting = ref(false)
const form = ref({
  toUserId: null as number | null,
  content: ''
})

const handleClose = () => {
  form.value = { toUserId: null, content: '' }
  visible.value = false
}

const handleSubmit = async () => {
  if (!form.value.toUserId) {
    ElMessage.warning('请选择要评价的好友')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }

  submitting.value = true
  try {
    await emit('submit', form.value.toUserId, form.value.content)
    handleClose()
  } finally {
    submitting.value = false
  }
}
</script>