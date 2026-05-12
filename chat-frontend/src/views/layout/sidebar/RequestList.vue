<template>
  <div class="request-list">
    <div v-for="req in requests" :key="req.id" class="request-item">
      <el-avatar :size="40" :src="req.fromUserAvatar || ''">
        {{ req.fromUserNickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="info">
        <div class="name">{{ req.fromUserNickname }}</div>
        <div class="message">{{ req.message || '申请添加好友' }}</div>
      </div>
      <div class="actions">
        <el-button size="small" type="success" @click="$emit('agree', req.id)">同意</el-button>
        <el-button size="small" type="danger" @click="$emit('reject', req.id)">拒绝</el-button>
      </div>
    </div>

    <el-empty v-if="requests.length === 0" description="暂无申请" />
  </div>
</template>

<script setup lang="ts">
/** 好友申请列表组件 @component */
import type { FriendRequestVO } from '@/api/friend'

/** 组件属性：申请列表 */
defineProps<{
  requests: FriendRequestVO[]
}>()

/** 组件事件：同意/拒绝申请 */
defineEmits<{
  agree: [id: number]
  reject: [id: number]
}>()
</script>

<style scoped>
.request-list {
  padding: 12px;
}

.request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  transition: background 0.2s;
  margin-bottom: 4px;
}

.request-item:hover {
  background: #f5f7fa;
}

.request-item .info {
  flex: 1;
}

.request-item .name {
  font-weight: 500;
  margin-bottom: 4px;
  font-size: 14px;
}

.request-item .message {
  font-size: 12px;
  color: #909399;
}

.request-item .actions {
  display: flex;
  gap: 8px;
}
</style>
