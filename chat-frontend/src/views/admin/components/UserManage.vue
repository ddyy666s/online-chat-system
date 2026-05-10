<template>
  <el-card class="section-card">
    <template #header>
      <span>用户管理</span>
    </template>

    <el-input v-model="keyword" placeholder="搜索用户名/昵称" style="width: 300px; margin-bottom: 16px" @clear="loadUsers"
      clearable />

    <el-table :data="users" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="nickname" label="昵称" width="150" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.role !== 'admin'" :type="row.status === 1 ? 'danger' : 'success'" size="small"
            @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <span v-else class="admin-badge">管理员</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
      layout="total, sizes, prev, pager, next" @current-change="loadUsers" @size-change="loadUsers" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsersApi, updateUserStatusApi, type UserManageVO } from '@/api/admin'

const users = ref<UserManageVO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

const loadUsers = async () => {
  const res = await getAdminUsersApi(currentPage.value, pageSize.value, keyword.value || undefined)
  users.value = res.records
  total.value = res.total
}

const toggleStatus = async (row: UserManageVO) => {
  const newStatus = row.status === 1 ? 0 : 1
  const confirmMsg = newStatus === 0 ? '确定要禁用该用户吗？' : '确定要启用该用户吗？'
  await ElMessageBox.confirm(confirmMsg, '提示', { type: 'warning' })
  await updateUserStatusApi(row.id, newStatus)
  ElMessage.success('操作成功')
  loadUsers()
}

// 搜索防抖
let debounceTimer: ReturnType<typeof setTimeout> | null = null
watch(keyword, () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    currentPage.value = 1
    loadUsers()
  }, 300)
})

loadUsers()
</script>

<style scoped>
.section-card {
  margin-bottom: 24px;
}

.admin-badge {
  color: #909399;
  font-size: 12px;
}
</style>