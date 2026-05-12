<template>
  <el-popover placement="right-start" :width="280" trigger="hover" :show-after="500" :hide-after="200">
    <template #reference>
      <slot />
    </template>

    <div class="mini-profile">
      <div class="profile-header">
        <el-avatar :size="60" :src="userInfo?.avatar || defaultAvatar" />
        <div class="info">
          <div class="name">{{ userInfo?.nickname || userInfo?.username }}</div>
          <div class="status" :class="{ online: userInfo?.isOnline }">
            {{ userInfo?.isOnline ? '在线' : '离线' }}
          </div>
        </div>
      </div>

      <div class="profile-detail">
        <div class="row">
          <span class="label">签名：</span>
          <span class="value">{{ userInfo?.signature || '这个人很懒，什么都没写' }}</span>
        </div>
        <div class="row">
          <span class="label">共同好友：</span>
          <span class="value">{{ commonFriends }}人</span>
        </div>
      </div>

      <div class="profile-actions">
        <el-button type="primary" size="small" @click="startChat">发消息</el-button>
        <el-button size="small" @click="showImpression">写印象</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
/** 迷你个人资料卡片组件，通过 Popover 悬浮展示 @component */
import { ref, onMounted } from 'vue'
import { getUserProfileApi } from '@/api/user'
import defaultAvatar from '@/assets/images/default-avatar.png'

/** 组件属性：用户 ID */
const props = defineProps<{
  userId: number
}>()

/** 组件事件：发起聊天、写印象 */
const emit = defineEmits<{
  (e: 'startChat', userId: number): void
  (e: 'writeImpression', userId: number): void
}>()

const router = useRouter()
/** 用户信息 */
const userInfo = ref<any>(null)
/** 共同好友数 */
const commonFriends = ref(0)

/** 获取用户信息 @returns Promise<void> */
const fetchUserInfo = async () => {
  try {
    const res = await getUserProfileApi(props.userId)
    userInfo.value = res
  } catch {
    /* 静默失败 */
  }
}

/** 发起聊天 @returns void */
const startChat = () => {
  emit('startChat', props.userId)
}

/** 写印象 @returns void */
const showImpression = () => {
  emit('writeImpression', props.userId)
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.mini-profile {
  padding: 8px;
}

.profile-header {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.info .name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.info .status {
  font-size: 12px;
  color: #909399;
}

.info .status.online {
  color: #67c23a;
}

.profile-detail {
  margin-bottom: 16px;
}

.profile-detail .row {
  font-size: 13px;
  margin-bottom: 8px;
}

.profile-detail .label {
  color: #909399;
}

.profile-detail .value {
  color: #606266;
}

.profile-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
