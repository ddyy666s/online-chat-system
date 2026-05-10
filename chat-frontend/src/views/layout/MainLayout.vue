<template>
  <div class="app-container" @click="unlockAudio">
    <div class="main-layout">
      <Sidebar @select-chat="handleSelectChat" @select-group="handleSelectGroup" />
      <div class="right-panel">
        <Header />
        <Content />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import Content from './Content.vue'
import { useFriendStore } from '@/stores/friendStore'

const router = useRouter()
const friendStore = useFriendStore()

const handleSelectChat = (friend: any) => {
  router.push({ name: 'Main', query: { friendId: friend.userId || friend.id } })
}

const handleSelectGroup = (group: any) => {
  router.push({ name: 'Main', query: { groupId: group.id } })
}

const unlockAudio = () => {
  if (navigator.mediaDevices) {
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(stream => {
        stream.getTracks().forEach(track => track.stop())
        console.log('音频权限已获取')
      })
      .catch(err => console.log('权限获取失败', err))
  }
}

onMounted(() => {
  friendStore.loadFriendList()
})
</script>

<style scoped>
.app-container {
  height: 100%;
  width: 100%;
  background: #f5f7fa;
}

.main-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  /* 防止内容溢出 */
  overflow: hidden;
}
</style>