<template>
  <el-drawer v-model="visible" title="消息盒子" direction="rtl" size="400px" :before-close="handleClose">
    <MessageList :messages="unreadMessages" @click="handleMessageClick" />
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/messageStore'
import { markAsReadApi } from '@/api/message'
import MessageList from './messageBox/MessageList.vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue'])
const router = useRouter()
const messageStore = useMessageStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const unreadMessages = computed(() => {
  const messages = messageStore.unreadCount?.messages || []
  // 转换数据格式，处理 null 和可选字段
  return messages.map((msg: any) => ({
    id: msg.id,
    fromUserId: msg.fromUserId,
    fromUserNickname: msg.fromUserNickname,
    fromUserAvatar: msg.fromUserAvatar || undefined,  // null -> undefined
    content: msg.content,
    messageType: msg.messageType ?? 0,  // 默认值 0
    sendTime: msg.sendTime,
    unreadCount: msg.unreadCount || 1,
    isOnline: msg.isOnline || false
  }))
})

const handleClose = () => {
  visible.value = false
}

const handleMessageClick = async (friendId: number) => {
  visible.value = false
  try {
    await markAsReadApi(friendId)
    messageStore.loadUnreadCount()
  } catch (error) {
    console.error(error)
  }
  router.push(`/?friendId=${friendId}`)
}
</script>