<template>
  <!-- 查看公告 -->
  <el-dialog v-model="showView" title="群公告" width="400px">
    <p>{{ notice || '暂无群公告' }}</p>
  </el-dialog>

  <!-- 编辑公告 -->
  <el-dialog v-model="showEdit" title="编辑群公告" width="400px">
    <el-input v-model="editContent" type="textarea" :rows="4" placeholder="请输入群公告" maxlength="200" show-word-limit />
    <template #footer>
      <el-button @click="showEdit = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

// 修复：notice 只接受 string | null
const props = defineProps<{
  modelValue: boolean
  notice: string | null
  canEdit: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', content: string): Promise<void>
}>()

const showView = ref(false)
const showEdit = ref(false)
const editContent = ref('')
const loading = ref(false)

watch(() => props.modelValue, (val) => {
  if (props.canEdit) {
    showEdit.value = val
    editContent.value = props.notice || ''
  } else {
    showView.value = val
  }
})

watch(showView, (val) => {
  if (!val) emit('update:modelValue', false)
})
watch(showEdit, (val) => {
  if (!val) emit('update:modelValue', false)
})

const handleSave = async () => {
  loading.value = true
  try {
    await emit('save', editContent.value)
    showEdit.value = false
  } finally {
    loading.value = false
  }
}
</script>