<template>
  <div class="search-item">
    <div class="user-info">
      <el-avatar :size="36" :src="user.avatar || ''">
        {{ user.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="user-detail">
        <div class="username">{{ user.nickname }}</div>
        <div class="signature">{{ user.signature || '暂无签名' }}</div>
      </div>
    </div>
    <el-button v-if="user.remark" type="info" size="small" disabled>
      已是好友
    </el-button>
    <el-button v-else type="primary" size="small" @click="$emit('add', user.userId)">
      添加好友
    </el-button>
  </div>
</template>

<script setup lang="ts">
/** 用户搜索结果显示组件，支持添加好友操作 @component */
defineProps<{
  user: any
}>()

/** 组件事件：添加好友 @param userId 目标用户 ID */
defineEmits<{
  (e: 'add', userId: number): void
}>()
</script>

<style scoped>
.search-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-detail {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.signature {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
