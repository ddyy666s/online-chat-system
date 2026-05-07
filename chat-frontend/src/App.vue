<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { websocketService } from '@/utils/websocket'

const userStore = useUserStore()

onMounted(() => {
  // 如果已登录，建立WebSocket连接
  if (userStore.isLoggedIn()) {
    websocketService.connect()
  }
})
</script>

<style>
/* 全局样式已在 main.css 中定义 */
</style>