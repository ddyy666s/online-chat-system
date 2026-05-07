<template>
  <div class="impression-board">
    <el-tabs v-model="activeTab">
      <!-- 对我的评价 -->
      <el-tab-pane label="对我的评价" name="to-me">
        <div v-for="imp in impressionsToMe" :key="imp.id" class="impression-item">
          <div class="avatar">
            <el-avatar :size="36" :src="imp.fromUserAvatar || defaultAvatar" />
          </div>
          <div class="content">
            <div class="name">{{ imp.fromUserNickname }}</div>
            <div class="text">{{ imp.content }}</div>
            <div class="time">{{ formatDate(imp.createdAt) }}</div>
          </div>
        </div>
        <el-empty v-if="impressionsToMe.length === 0" description="暂无评价" />
      </el-tab-pane>

      <!-- 我给出的评价 -->
      <el-tab-pane label="我给出的评价" name="by-me">
        <div v-for="imp in impressionsByMe" :key="imp.id" class="impression-item">
          <div class="avatar">
            <!-- 使用默认头像，因为后端可能没有返回 toUserAvatar -->
            <el-avatar :size="36" :src="defaultAvatar" />
          </div>
          <div class="content">
            <div class="name">{{ imp.toUserNickname }}</div>
            <div class="text">{{ imp.content }}</div>
            <div class="time">{{ formatDate(imp.createdAt) }}</div>
          </div>
          <el-button text type="danger" @click="handleDelete(imp.id)">删除</el-button>
        </div>
        <el-empty v-if="impressionsByMe.length === 0" description="暂无评价" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ImpressionVO } from '@/api/impression'
import {
  getImpressionsToMeApi,
  getImpressionsByMeApi,
  addImpressionApi,
  deleteImpressionApi
} from '@/api/impression'
import { formatDate } from '@/utils/date'
import defaultAvatar from '@/assets/images/default-avatar.png'

const activeTab = ref('to-me')
const impressionsToMe = ref<ImpressionVO[]>([])
const impressionsByMe = ref<ImpressionVO[]>([])

const loadData = async () => {
  const [toMe, byMe] = await Promise.all([
    getImpressionsToMeApi(),
    getImpressionsByMeApi()
  ])
  impressionsToMe.value = toMe
  impressionsByMe.value = byMe
}

const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', { type: 'warning' })
  await deleteImpressionApi(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>