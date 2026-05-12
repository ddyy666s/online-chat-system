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

let audioUnlocked = false
const unlockAudio = () => {
  if (audioUnlocked) return
  audioUnlocked = true
  if (navigator.mediaDevices) {
    navigator.mediaDevices.getUserMedia({ audio: true })
      .then(stream => stream.getTracks().forEach(t => t.stop()))
      .catch(() => {})
  }
  try {
    const ctx = new (window.AudioContext || (window as any).webkitAudioContext)()
    const osc = ctx.createOscillator()
    osc.frequency.value = 1
    osc.connect(ctx.destination)
    osc.start()
    osc.stop(0.001)
  } catch { /* ignore */ }
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