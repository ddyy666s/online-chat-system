<template>
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
</template>

<script setup lang="ts">
import type { StatisticsVO } from '@/api/admin'

defineProps<{
  stats: StatisticsVO
}>()
</script>

<style scoped>
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
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
</style>