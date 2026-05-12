<template>
  <div class="chat-header">
    <div class="group-info">
      <el-avatar :size="40" :src="group?.avatar || ''">
        {{ group?.name?.charAt(0) || '群' }}
      </el-avatar>
      <div class="group-detail">
        <div class="name">{{ group?.name }}</div>
        <div class="member-count">{{ group?.memberCount }}人</div>
      </div>
    </div>
    <div class="actions">
      <el-dropdown @command="$emit('command', $event)">
        <el-button :icon="Setting" circle text />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="notice" v-if="canEditNotice">编辑公告</el-dropdown-item>
            <el-dropdown-item command="viewNotice" v-else>群公告</el-dropdown-item>
            <el-dropdown-item command="members">群成员</el-dropdown-item>
            <el-dropdown-item command="invite">邀请好友</el-dropdown-item>
            <el-dropdown-item command="manage" v-if="canEditNotice" divided>群管理</el-dropdown-item>
            <el-dropdown-item command="quit" divided v-if="!isOwner">退出群聊</el-dropdown-item>
            <el-dropdown-item command="disband" divided v-if="isOwner" style="color: #f56c6c">解散群聊</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Setting } from '@element-plus/icons-vue'

defineProps<{
  group: any
  canEditNotice: boolean
  isOwner: boolean
}>()

defineEmits<{
  (e: 'command', command: string): void
}>()
</script>

<style scoped>
.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.group-detail .name {
  font-size: 16px;
  font-weight: 500;
}

.group-detail .member-count {
  font-size: 12px;
  color: #909399;
}
</style>