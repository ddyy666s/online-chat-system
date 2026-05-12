<template>
  <div class="chat-header">
    <div class="friend-info">
      <el-avatar :size="40" :src="friend?.avatar || ''">
        {{ friend?.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="friend-detail">
        <div class="name">{{ friend?.remark || friend?.nickname }}</div>
        <div class="status">{{ friend?.isOnline ? '在线' : '离线' }}</div>
      </div>
    </div>
    <div class="actions">
      <el-tooltip content="下载聊天记录" placement="bottom">
        <el-button :icon="Download" circle text @click="$emit('download')" />
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 聊天窗口头部组件，展示好友信息和操作按钮 @component */
import { Download } from '@element-plus/icons-vue'

/** 组件属性：好友信息 */
defineProps<{ friend: any }>()
/** 组件事件：下载聊天记录 */
defineEmits<{ (e: 'download'): void }>()
</script>

<style scoped>
.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.friend-detail .name {
  font-size: 16px;
  font-weight: 500;
}

.friend-detail .status {
  font-size: 12px;
  color: #909399;
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
