<template>
  <div class="friend-group">
    <div class="group-header">
      <span>{{ groupName }}</span>
      <span class="count">{{ friends.length }}</span>
    </div>

    <FriendItem v-for="friend in friends" :key="friend.userId" :friend="friend"
      :is-active="currentChatUserId === friend.userId" @click="$emit('selectChat', friend)"
      @command="(cmd, f) => $emit('command', cmd, f)" />
  </div>
</template>

<script setup lang="ts">
import FriendItem from './FriendItem.vue'

defineProps<{
  groupName: string
  friends: any[]
  currentChatUserId: number | null
}>()

defineEmits<{
  (e: 'selectChat', friend: any): void
  (e: 'command', command: string, friend: any): void
}>()
</script>

<style scoped>
.friend-group {
  margin-bottom: 8px;
}

.group-header {
  padding: 8px 16px;
  font-size: 13px;
  color: #909399;
  background: #f5f5f5;
}

.count {
  margin-left: 8px;
  font-size: 12px;
}
</style>