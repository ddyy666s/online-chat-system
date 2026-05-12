<template>
  <div class="group-chat-window">
    <GroupChatHeader :group="group" :can-edit-notice="canEditNotice" :is-owner="isOwner"
      @command="handleGroupCommand" />

    <!-- 修复：确保传入 string | null 类型，过滤 undefined -->
    <GroupNoticeBar :notice="group?.notice ?? null" @click="openNotice" />

    <GroupMessageList ref="messageListRef" :messages="messages" :current-user-id="currentUserId" :loading="loading"
      @load-more="loadMore" />

    <GroupMessageInput :muted="isMuted" @send="sendMessage" />

    <!-- 修复：确保传入 string | null 类型 -->
    <GroupNoticeDialog v-model="showNoticeDialog" :notice="group?.notice ?? null" :can-edit="canEditNotice"
      @save="handleUpdateNotice" />

    <GroupMembersDialog v-model="showMembers" :members="memberList" />

    <GroupManagementDialog v-model="showManage" :group-id="props.group?.id || 0" :members="memberList"
      :current-user-id="currentUserId!" :is-owner="isOwner" :can-manage="canEditNotice"
      @refresh="loadMembers" />

    <InviteFriendDialog v-model="showInvite" :friends="friendList" @invite="handleInvite" />
    <ConfirmDialog v-model="showQuitDialog" title="退出群聊" message="确定要退出该群聊吗？"
      type="warning" confirm-text="退出" cancel-text="取消" @confirm="confirmQuit" />
    <ConfirmDialog v-model="showDisbandDialog" title="解散群聊" message="确定要解散该群聊吗？此操作不可恢复"
      type="danger" confirm-text="解散" cancel-text="取消" @confirm="confirmDisband" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getGroupHistoryApi,
  quitGroupApi,
  disbandGroupApi,
  inviteMemberApi,
  getGroupMembersApi,
  clearGroupUnreadApi,
  updateGroupNoticeApi,
  type GroupVO,
  type GroupMessageVO,
  type GroupMemberVO
} from '@/api/group'
import { getFriendListApi, type FriendVO } from '@/api/friend'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import GroupChatHeader from './GroupChatHeader.vue'
import GroupNoticeBar from './GroupNoticeBar.vue'
import GroupMessageList from './GroupMessageList.vue'
import GroupMessageInput from './GroupMessageInput.vue'
import GroupNoticeDialog from './GroupNoticeDialog.vue'
import GroupMembersDialog from './GroupMembersDialog.vue'
import GroupManagementDialog from './GroupManagementDialog.vue'
import InviteFriendDialog from './InviteFriendDialog.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const props = defineProps<{
  group: GroupVO | null
}>()

const emit = defineEmits(['update:list', 'updateUnreadCount'])

const userStore = useUserStore()
const currentUserId = userStore.userInfo?.id
const isOwner = computed(() => props.group?.ownerId === currentUserId)

const canEditNotice = computed(() => {
  const member = memberList.value.find(m => m.userId === currentUserId)
  return isOwner.value || member?.role === 1
})

const isMuted = computed(() => {
  const member = memberList.value.find(m => m.userId === currentUserId)
  return member?.muted === true
})

const messages = ref<GroupMessageVO[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const messageListRef = ref()

// 弹窗状态
const showNoticeDialog = ref(false)
const showMembers = ref(false)
const showManage = ref(false)
const showInvite = ref(false)
const showQuitDialog = ref(false)
const showDisbandDialog = ref(false)
const memberList = ref<GroupMemberVO[]>([])
const friendList = ref<FriendVO[]>([])

// 加载历史消息
const loadHistory = async (reset = true) => {
  if (!props.group?.id) return
  if (reset) {
    page.value = 1
    hasMore.value = true
    messages.value = []
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const res = await getGroupHistoryApi(props.group.id, page.value, 20)
    const newMessages = res.records.reverse()
    if (reset) {
      messages.value = newMessages
    } else {
      messages.value = [...newMessages, ...messages.value]
    }
    hasMore.value = newMessages.length > 0
    page.value++
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadHistory(false)
  }
}

// 清除未读计数
const clearUnreadCount = async () => {
  if (!props.group?.id) return
  try {
    await clearGroupUnreadApi(props.group.id)
    emit('updateUnreadCount', props.group.id)
  } catch (error) {
    console.error('清除未读失败', error)
  }
}

// 发送消息
const sendMessage = (content: string) => {
  if (!props.group?.id) return
  if (isMuted.value) { ElMessage.warning('你已被禁言'); return }
  // 添加临时消息到列表
  const tempMsg: GroupMessageVO = {
    id: Date.now(),
    groupId: props.group.id,
    fromUserId: currentUserId!,
    fromUserNickname: userStore.userInfo?.nickname || '我',
    fromUserAvatar: userStore.userInfo?.avatar ?? null,
    content: content,
    messageType: 1,
    sendTime: new Date().toISOString()
  }
  messages.value.push(tempMsg)
  messageListRef.value?.scrollToBottom()
  websocketService.sendGroupMessage(props.group.id, content)
}

// 接收群消息
const onGroupMessage = (data: any) => {
  if (props.group && data.groupId === props.group.id) {
    const newMsg: GroupMessageVO = {
      id: data.messageId,
      groupId: data.groupId,
      fromUserId: data.fromUserId,
      fromUserNickname: data.fromUserNickname,
      fromUserAvatar: data.fromUserAvatar ?? null,
      content: data.content,
      messageType: data.messageType,
      sendTime: data.sendTime
    }
    messages.value.push(newMsg)
    messageListRef.value?.scrollToBottom()
  }
}

// 加载群成员
const loadMembers = async () => {
  if (!props.group?.id) return
  try {
    memberList.value = await getGroupMembersApi(props.group.id)
  } catch (error) {
    console.error('加载群成员失败', error)
  }
}

// 加载好友列表
const loadFriendList = async () => {
  try {
    const res = await getFriendListApi()
    const friends: FriendVO[] = []
    for (const group of res) friends.push(...group.friends)
    friendList.value = friends
  } catch (error) {
    console.error('加载好友列表失败', error)
  }
}

// 更新群公告
const handleUpdateNotice = async (notice: string) => {
  if (!props.group?.id) return
  try {
    await updateGroupNoticeApi(props.group.id, notice)
    if (props.group) props.group.notice = notice
    ElMessage.success('群公告已更新')
    emit('update:list')
  } catch (error) {
    console.error(error)
    ElMessage.error('更新失败')
    throw error
  }
}

// 打开公告弹窗
const openNotice = () => {
  showNoticeDialog.value = true
}

// 群管理命令
const handleGroupCommand = async (command: string) => {
  if (!props.group) return

  if (command === 'notice' || command === 'viewNotice') {
    showNoticeDialog.value = true
  } else if (command === 'members') {
    await loadMembers()
    showMembers.value = true
  } else if (command === 'invite') {
    await loadFriendList()
    showInvite.value = true
  } else if (command === 'manage') {
    await loadMembers()
    showManage.value = true
  } else if (command === 'quit') {
    showQuitDialog.value = true
  } else if (command === 'disband') {
    showDisbandDialog.value = true
  }
}

const confirmQuit = async () => {
  await quitGroupApi(props.group!.id)
  ElMessage.success('已退出群聊')
  emit('update:list')
}

const confirmDisband = async () => {
  await disbandGroupApi(props.group!.id)
  ElMessage.success('群聊已解散')
  emit('update:list')
}

// 邀请好友
const handleInvite = async (userId: number) => {
  if (!props.group?.id) return
  try {
    await inviteMemberApi({ groupId: props.group.id, userId })
    ElMessage.success('邀请已发送')
  } catch (error) {
    console.error(error)
    ElMessage.error('邀请失败')
    throw error
  }
}

// 监听群切换
watch(() => props.group, (newGroup) => {
  if (newGroup?.id) {
    loadHistory(true)
    clearUnreadCount()
    loadMembers()
  }
}, { immediate: true })

// 注册 WebSocket 回调
onMounted(() => {
  websocketService.onGroupMessage(onGroupMessage)
})
</script>

<style scoped>
.group-chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}
</style>