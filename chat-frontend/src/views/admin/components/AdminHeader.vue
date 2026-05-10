<template>
  <div class="admin-header">
    <el-button :icon="ArrowLeft" @click="goBack">返回聊天</el-button>
    <h2>{{ title }}</h2>
    <div></div>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

defineProps<{
  title: string
}>()

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.admin-header h2 {
  margin: 0;
  font-size: 18px;
}
</style>