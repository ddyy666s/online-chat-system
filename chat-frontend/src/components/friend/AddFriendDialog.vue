<template>
  <el-dialog v-model="visible" title="添加好友" width="500px" @close="handleClose">
    <el-input v-model="keyword" placeholder="请输入用户名或昵称搜索" clearable @keyup.enter="handleSearch">
      <template #append>
        <el-button @click="handleSearch" :loading="loading">搜索</el-button>
      </template>
    </el-input>

    <div v-if="results.length > 0" class="search-results">
      <SearchResultItem v-for="user in results" :key="user.userId" :user="user" @add="handleAdd" />
    </div>

    <Empty v-else-if="searched && results.length === 0" description="未找到相关用户" />
  </el-dialog>
</template>

<script setup lang="ts">
/** 添加好友对话框组件，支持搜索用户和发送好友申请 @component */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { searchUsersApi, sendFriendRequestApi } from '@/api/friend'
import Empty from '@/components/common/Empty.vue'
import SearchResultItem from './SearchResultItem.vue'

/** 组件属性：对话框显示状态 */
const props = defineProps<{
  modelValue: boolean
}>()

/** 组件事件：更新显示状态、添加成功 */
const emit = defineEmits(['update:modelValue', 'success'])

/** 对话框可见性 */
const visible = ref(false)
/** 搜索关键词 */
const keyword = ref('')
/** 搜索结果列表 */
const results = ref<any[]>([])
/** 搜索加载状态 */
const loading = ref(false)
/** 是否已执行过搜索 */
const searched = ref(false)

/** 监听外部对话框显示状态 */
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (!val) {
    keyword.value = ''
    results.value = []
    searched.value = false
  }
})

/** 内部可见性同步到外部 */
watch(visible, (val) => {
  emit('update:modelValue', val)
})

/** 执行搜索 @returns Promise<void> */
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }
  searched.value = true
  loading.value = true
  try {
    results.value = await searchUsersApi(keyword.value)
  } catch (error) {
    console.error(error)
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

/** 发送好友申请 @param userId 目标用户 ID @returns Promise<void> */
const handleAdd = async (userId: number) => {
  try {
    await sendFriendRequestApi(userId)
    ElMessage.success('好友申请已发送')
    visible.value = false
    emit('success')
  } catch (error: any) {
    ElMessage.error(error?.message || '发送失败')
  }
}

/** 关闭对话框 @returns void */
const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.search-results {
  margin-top: 16px;
  max-height: 300px;
  overflow-y: auto;
}
</style>
