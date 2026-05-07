<template>
  <el-drawer v-model="visible" title="消息盒子" direction="rtl" size="400px" :before-close="handleClose">
    <div class="message-box">
      <div v-if="unreadDetails.length > 0">
        <div v-for="detail in unreadDetails" :key="detail.friendId" class="message-item"
          @click="jumpToChat(detail.friendId)">
          <!-- 使用默认头像，因为后端可能没有返回 friendAvatar -->
          <el-avatar :size="44" :src="defaultAvatar" />
          <div class="content">
            <div class="name">{{ detail.friendNickname }}</div>
            <div class="preview">{{ detail.unreadCount }} 条未读消息</div>
          </div>
          <el-badge :value="detail.unreadCount" class="badge" />
        </div>
      </div>
      <el-empty v-else description="暂无未读消息" />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/messageStore'
import defaultAvatar from '@/assets/images/default-avatar.png'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue', 'selectChat'])
const router = useRouter()
const messageStore = useMessageStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const unreadDetails = computed(() => messageStore.unreadCount.details)

const handleClose = () => {
  visible.value = false
}

const jumpToChat = (friendId: number) => {
  visible.value = false
  emit('selectChat', friendId)
  router.push(`/?friendId=${friendId}`)
}
</script>