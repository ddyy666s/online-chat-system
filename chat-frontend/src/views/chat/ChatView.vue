<template>
  <div class="chat-view">
    <ChatWindow v-if="currentFriend" :friend="currentFriend" />
    <div v-else class="empty">
      <el-empty description="选择好友开始聊天" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useFriendStore } from '@/stores/friendStore'
import ChatWindow from '@/components/ChatWindow.vue'

const route = useRoute()
const friendStore = useFriendStore()
const currentFriend = ref<any>(null)

onMounted(() => {
  const friendId = route.query.friendId
  if (friendId) {
    currentFriend.value = friendStore.getFriendById(Number(friendId))
  }
})
</script>

<style scoped>
.chat-view {
  height: 100%;
}

.empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>