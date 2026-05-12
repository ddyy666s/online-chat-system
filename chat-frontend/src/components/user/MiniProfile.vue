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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import defaultAvatar from '@/assets/images/default-avatar.png'

const props = defineProps<{
  userId: number
}>()

const router = useRouter()
const userInfo = ref<any>(null)
const commonFriends = ref(0)

const fetchUserInfo = async () => {
  // TODO: 调用获取用户信息接口
  // const res = await getUserProfileApi(props.userId)
  // userInfo.value = res
}

const startChat = () => {
  router.push(`/?friendId=${props.userId}`)
}

const showImpression = () => {
  // TODO: 打开添加印象弹窗
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