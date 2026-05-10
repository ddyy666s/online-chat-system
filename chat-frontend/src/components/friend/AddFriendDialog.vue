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
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { searchUsersApi, sendFriendRequestApi } from '@/api/friend'
import Empty from '@/components/common/Empty.vue'
import SearchResultItem from './SearchResultItem.vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const keyword = ref('')
const results = ref<any[]>([])
const loading = ref(false)
const searched = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (!val) {
    // 关闭时重置状态
    keyword.value = ''
    results.value = []
    searched.value = false
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

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