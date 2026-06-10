<template>
  <BaseDialog v-model="visible" title="添加评价" width="440px" @close="handleClose">
    <el-form :model="form" label-width="80px">
      <el-form-item label="选择好友">
        <el-select 
          v-model="form.toUserId" 
          placeholder="请选择要评价的好友" 
          filterable 
          style="width: 100%" 
          :loading="loading"
        >
          <el-option 
            v-for="friend in friendList" 
            :key="friend.userId" 
            :label="friend.remark || friend.nickname"
            :value="friend.userId" 
          />
        </el-select>
      </el-form-item>
      <el-form-item label="评价内容">
        <el-input 
          v-model="form.content" 
          type="textarea" 
          :rows="3" 
          placeholder="请输入评价内容（最多100字）" 
          maxlength="100"
          show-word-limit 
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        提交
      </el-button>
    </template>
  </BaseDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import BaseDialog from '@/components/common/BaseDialog.vue'
import { notify } from '@/utils/notify'

/** 好友数据结构 */
interface Friend {
  userId: number
  nickname: string
  remark?: string | null
}

/** 组件属性 */
const props = defineProps<{
  modelValue: boolean
  friendList: Friend[]
  loading?: boolean
  preselectUserId?: number | null
}>()

/** 组件事件 */
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [toUserId: number, content: string]
}>()

/** 对话框可见性双向绑定 */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/** 表单数据 */
const form = ref({
  toUserId: null as number | null,
  content: ''
})

/** 提交按钮加载状态 */
const submitting = ref(false)

/** 重置表单 */
const resetForm = () => {
  form.value = { toUserId: null, content: '' }
}

/** 关闭对话框 */
const handleClose = () => {
  resetForm()
  visible.value = false
}

/** 监听对话框打开，自动预选好友 */
watch(
  () => props.modelValue,
  async (isOpen) => {
    if (isOpen && props.preselectUserId) {
      // 等待 DOM 更新，确保 friendList 已是最新
      await nextTick()
      const exists = props.friendList.some(f => f.userId === props.preselectUserId)
      if (exists) {
        form.value.toUserId = props.preselectUserId
      }
    } else if (!isOpen) {
      resetForm()
    }
  },
  { immediate: false }
)

/** 提交评价 */
const handleSubmit = async () => {
  if (!form.value.toUserId) {
    notify.warning('请选择要评价的好友')
    return
  }
  const trimmedContent = form.value.content.trim()
  if (!trimmedContent) {
    notify.warning('请输入评价内容')
    return
  }

  submitting.value = true
  try {
    emit('submit', form.value.toUserId, trimmedContent)
    // 关闭对话框（父组件处理完数据后关闭，但这里立即关闭以提升体验）
    handleClose()
  } finally {
    // 延迟重置提交状态，避免在关闭动画期间用户快速点击
    setTimeout(() => {
      submitting.value = false
    }, 200)
  }
}
</script>