<template>
  <div class="friend-group">
    <div class="group-header">
      <span>{{ groupName }}</span>
      <span class="count">{{ friends.length }}</span>
    </div>

    <FriendItem v-for="friend in friends" :key="friend.userId" :friend="friend"
      :is-active="currentChatUserId === friend.userId" @click="$emit('selectChat', friend)"
      @command="(cmd, f) => $emit('command', cmd, f)"
      @write-impression="(uid) => $emit('writeImpression', uid)" />
  </div>
</template>

<script setup lang="ts">
/** 好友分组组件，展示分组标题和好友列表 @component */
import FriendItem from './FriendItem.vue'

/** 组件属性：分组名称、好友列表、当前聊天好友 ID */
defineProps<{
  groupName: string
  friends: any[]
  currentChatUserId: number | null
}>()

/** 组件事件：选择聊天好友、执行操作命令、写印象 */
defineEmits<{
  (e: 'selectChat', friend: any): void
  (e: 'command', command: string, friend: any): void
  (e: 'writeImpression', userId: number): void
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
