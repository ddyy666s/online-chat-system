<template>
  <div class="admin-layout">
    <AdminSidebar :active-menu="activeMenu" @select="handleMenuSelect" />

    <div class="admin-main">
      <AdminHeader :title="pageTitle" />

      <div class="admin-content">
        <!-- 数据统计页面 -->
        <template v-if="activeMenu === 'stats'">
          <StatsCards :stats="stats" />
          <StatsChart :stats="stats" />
        </template>

        <UserManage v-if="activeMenu === 'users'" />
        <MessageAudit v-if="activeMenu === 'messages'" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getAdminStatsApi, type StatisticsVO } from '@/api/admin'
import AdminSidebar from './components/AdminSidebar.vue'
import AdminHeader from './components/AdminHeader.vue'
import StatsCards from './components/StatsCards.vue'
import StatsChart from './components/StatsChart.vue'
import UserManage from './components/UserManage.vue'
import MessageAudit from './components/MessageAudit.vue'

const activeMenu = ref('stats')
const stats = ref<StatisticsVO>({
  totalUsers: 0,
  todayActiveUsers: 0,
  todayMessages: 0,
  onlineUsers: 0
})

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    stats: '数据统计',
    users: '用户管理',
    messages: '消息审计'
  }
  return titles[activeMenu.value] || '管理后台'
})

const loadStats = async () => {
  stats.value = await getAdminStatsApi()
}

const handleMenuSelect = (menu: string) => {
  activeMenu.value = menu
  if (menu === 'stats') {
    loadStats()
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f5f5;
}

.admin-main {
  flex: 1;
  margin-left: 240px;
  display: flex;
  flex-direction: column;
}

.admin-content {
  padding: 20px;
}
</style>