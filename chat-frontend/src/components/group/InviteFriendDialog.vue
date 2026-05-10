<template>
  <el-dialog v-model="visible" title="邀请好友" width="400px">
    <el-select v-model="selectedId" placeholder="选择好友" filterable style="width: 100%">
      <el-option v-for="friend in friends" :key="friend.userId" :label="friend.remark || friend.nickname"
        :value="friend.userId" />
    </el-select>
    <Empty v-if="friends.length === 0" description="暂无好友可邀请" />
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleInvite">邀请</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import Empty from '@/components/common/Empty.vue'

const props = defineProps<{
  modelValue: boolean
  friends: any[]
}>()

const emit = defineEmits(['update:modelValue', 'invite'])

const visible = ref(false)
const selectedId = ref<number | null>(null)
const loading = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (!val) {
    selectedId.value = null
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleInvite = async () => {
  if (!selectedId.value) {
    ElMessage.warning('请选择好友')
    return
  }
  loading.value = true
  try {
    await emit('invite', selectedId.value)
    visible.value = false
  } finally {
    loading.value = false
  }
}
</script>