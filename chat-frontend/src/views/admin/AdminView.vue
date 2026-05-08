<template>
  <div class="admin-view">
    <div class="admin-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回聊天</el-button>
      <h2>管理后台</h2>
      <div></div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-value">{{ stats.totalUsers }}</div>
        <div class="stat-label">总用户数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ stats.todayActiveUsers }}</div>
        <div class="stat-label">今日活跃</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ stats.todayMessages }}</div>
        <div class="stat-label">今日消息</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ stats.onlineUsers }}</div>
        <div class="stat-label">在线人数</div>
      </el-card>
    </div>

    <!-- 用户管理 -->
    <el-card class="section-card">
      <template #header>
        <span>用户管理</span>
      </template>

      <el-input v-model="userKeyword" placeholder="搜索用户名/昵称" style="width: 300px; margin-bottom: 16px"
        @clear="loadUsers" clearable />

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

      <el-pagination v-model:current-page="userPage" v-model:page-size="userSize" :total="userTotal"
        layout="total, sizes, prev, pager, next" @current-change="loadUsers" @size-change="loadUsers" />
    </el-card>

    <!-- 聊天记录审计 -->
    <el-card class="section-card">
      <template #header>
        <span>聊天记录审计</span>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="发送者ID">
          <el-input v-model="msgFilter.fromUserId" placeholder="发送者ID" clearable />
        </el-form-item>
        <el-form-item label="接收者ID">
          <el-input v-model="msgFilter.toUserId" placeholder="接收者ID" clearable />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="msgFilter.startTime" type="date" placeholder="开始日期" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="msgFilter.endTime" type="date" placeholder="结束日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadMessages">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="messages" border stripe>
        <el-table-column prop="id" label="消息ID" width="100" />
        <el-table-column prop="fromUserNickname" label="发送者" width="120" />
        <el-table-column prop="fromUserId" label="发送者ID" width="100" />
        <el-table-column prop="toUserNickname" label="接收者" width="120" />
        <el-table-column prop="toUserId" label="接收者ID" width="100" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sendTime" label="发送时间" width="180" />
      </el-table>

      <el-pagination v-model:current-page="msgPage" v-model:page-size="msgSize" :total="msgTotal"
        layout="total, sizes, prev, pager, next" @current-change="loadMessages" @size-change="loadMessages" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  getAdminUsersApi,
  updateUserStatusApi,
  getAdminMessagesApi,
  getAdminStatsApi,
  UserManageVO,
  StatisticsVO
} from '@/api/admin'
import { formatDate } from '@/utils/date'

const router = useRouter()

const goBack = () => {
  router.push('/')
}

const stats = ref<StatisticsVO>({
  totalUsers: 0,
  todayActiveUsers: 0,
  todayMessages: 0,
  onlineUsers: 0
})

// 用户管理
const users = ref<UserManageVO[]>([])
const userPage = ref(1)
const userSize = ref(10)
const userTotal = ref(0)
const userKeyword = ref('')

// 消息审计
const messages = ref<any[]>([])
const msgPage = ref(1)
const msgSize = ref(20)
const msgTotal = ref(0)
const msgFilter = ref({
  fromUserId: '',
  toUserId: '',
  startTime: '',
  endTime: ''
})

const loadStats = async () => {
  stats.value = await getAdminStatsApi()
}

const loadUsers = async () => {
  const res = await getAdminUsersApi(userPage.value, userSize.value, userKeyword.value || undefined)
  users.value = res.records
  userTotal.value = res.total
}

const toggleStatus = async (row: UserManageVO) => {
  const newStatus = row.status === 1 ? 0 : 1
  const confirmMsg = newStatus === 0 ? '确定要禁用该用户吗？' : '确定要启用该用户吗？'
  await ElMessageBox.confirm(confirmMsg, '提示', { type: 'warning' })
  await updateUserStatusApi(row.id, newStatus)
  ElMessage.success('操作成功')
  loadUsers()
}

const loadMessages = async () => {
  const params: any = {
    page: msgPage.value,
    size: msgSize.value
  }
  if (msgFilter.value.fromUserId) params.fromUserId = msgFilter.value.fromUserId
  if (msgFilter.value.toUserId) params.toUserId = msgFilter.value.toUserId
  if (msgFilter.value.startTime) params.startTime = formatDate(msgFilter.value.startTime, 'YYYY-MM-DD')
  if (msgFilter.value.endTime) params.endTime = formatDate(msgFilter.value.endTime, 'YYYY-MM-DD')

  const res = await getAdminMessagesApi(params)
  messages.value = res.records
  msgTotal.value = res.total
}

const resetFilter = () => {
  msgFilter.value = { fromUserId: '', toUserId: '', startTime: '', endTime: '' }
  msgPage.value = 1
  loadMessages()
}

onMounted(() => {
  loadStats()
  loadUsers()
  loadMessages()
})
</script>

<style scoped>
.admin-view {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100%;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.admin-header h2 {
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.section-card {
  margin-bottom: 24px;
}

.filter-form {
  margin-bottom: 16px;
}

.admin-badge {
  color: #909399;
  font-size: 12px;
}
</style>
