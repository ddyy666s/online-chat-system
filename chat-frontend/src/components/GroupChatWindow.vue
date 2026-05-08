<template>
  <div class="group-chat-window">
    <!-- 群聊头部 -->
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
        <el-dropdown @command="handleGroupCommand">
          <el-button :icon="Setting" circle text />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="notice">群公告</el-dropdown-item>
              <el-dropdown-item command="members">群成员</el-dropdown-item>
              <el-dropdown-item command="invite">邀请好友</el-dropdown-item>
              <el-dropdown-item command="quit" divided v-if="!isOwner">退出群聊</el-dropdown-item>
              <el-dropdown-item command="disband" divided v-if="isOwner" style="color: #f56c6c">解散群聊</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef">
      <div v-for="msg in messages" :key="msg.id" class="message-item"
        :class="{ own: msg.fromUserId === currentUserId }">
        <el-avatar :size="32" :src="msg.fromUserAvatar || ''">
          {{ msg.fromUserNickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="message-content">
          <div class="message-info">
            <span class="name">{{ msg.fromUserNickname }}</span>
            <span class="time">{{ formatRelativeTime(msg.sendTime) }}</span>
          </div>
          <div class="message-bubble">
            <span>{{ msg.content }}</span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="2" animated />
      </div>

      <div ref="scrollBottomRef"></div>
    </div>

    <!-- 消息输入框 -->
    <div class="message-input">
      <el-input v-model="inputContent" type="textarea" :rows="3" placeholder="请输入群消息..."
        @keyup.ctrl.enter="sendMessage" />
      <div class="input-actions">
        <el-button type="primary" @click="sendMessage">
          发送 (Ctrl+Enter)
        </el-button>
      </div>
    </div>

    <!-- 群公告弹窗 -->
    <el-dialog v-model="showNotice" title="群公告" width="400px">
      <p>{{ group?.notice || '暂无群公告' }}</p>
    </el-dialog>

    <!-- 群成员弹窗 -->
    <el-dialog v-model="showMembers" title="群成员" width="400px">
      <div v-for="member in memberList" :key="member.userId" class="member-item">
        <el-avatar :size="32" :src="member.avatar || ''">
          {{ member.nickname?.charAt(0) || 'U' }}
        </el-avatar>
        <div class="member-info">
          <span>{{ member.nickname }}</span>
          <span v-if="member.role === 2" class="owner-tag">群主</span>
          <span v-else-if="member.role === 1" class="admin-tag">管理员</span>
        </div>
      </div>
    </el-dialog>

    <!-- 邀请好友弹窗 -->
    <el-dialog v-model="showInvite" title="邀请好友" width="400px">
      <el-select v-model="selectedFriendId" placeholder="选择好友" filterable style="width: 100%">
        <el-option v-for="friend in friendList" :key="friend.userId" :label="friend.remark || friend.nickname"
          :value="friend.userId" />
      </el-select>
      <template #footer>
        <el-button @click="showInvite = false">取消</el-button>
        <el-button type="primary" @click="handleInvite">邀请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import {
  getGroupHistoryApi,
  quitGroupApi,
  disbandGroupApi,
  inviteMemberApi,
  getGroupMembersApi,
  type GroupVO,
  type GroupMessageVO,
  type GroupMemberVO
} from '@/api/group'
import { getFriendListApi, type FriendVO } from '@/api/friend'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { formatRelativeTime } from '@/utils/date'

const props = defineProps<{
  group: GroupVO | null
}>()

const emit = defineEmits(['update:list'])

const userStore = useUserStore()
const currentUserId = userStore.userInfo?.id
const isOwner = computed(() => props.group?.ownerId === currentUserId)

const messages = ref<GroupMessageVO[]>([])
const inputContent = ref('')
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)
const scrollBottomRef = ref<HTMLElement>()
const messageListRef = ref<HTMLElement>()

const showNotice = ref(false)
const showMembers = ref(false)
const showInvite = ref(false)
const memberList = ref<GroupMemberVO[]>([])
const friendList = ref<FriendVO[]>([])
const selectedFriendId = ref<number | null>(null)

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
    messages.value = [...newMessages, ...messages.value]
    hasMore.value = res.records.length > 0
    page.value++

    if (reset) {
      await nextTick()
      scrollBottomRef.value?.scrollIntoView({ behavior: 'auto' })
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputContent.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  if (!props.group?.id) {
    ElMessage.warning('群聊信息有误')
    return
  }

  const content = inputContent.value
  const tempMsg: GroupMessageVO = {
    id: Date.now(),
    groupId: props.group.id,
    fromUserId: currentUserId!,
    fromUserNickname: userStore.userInfo?.nickname || '我',
    fromUserAvatar: userStore.userInfo?.avatar ?? null,  // 关键修复
    content: content,
    messageType: 1,
    sendTime: new Date().toISOString()
  }

  messages.value.push(tempMsg)
  inputContent.value = ''

  nextTick(() => {
    scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
  })

  // 发送 WebSocket 消息
  websocketService.sendGroupMessage(props.group.id, content)
}

// 接收群消息
// 接收群消息
const onGroupMessage = (data: any) => {
  if (props.group && data.groupId === props.group.id) {
    const newMsg: GroupMessageVO = {
      id: data.messageId,
      groupId: data.groupId,
      fromUserId: data.fromUserId,
      fromUserNickname: data.fromUserNickname,
      fromUserAvatar: data.fromUserAvatar ?? null,  // 添加 ?? null
      content: data.content,
      messageType: data.messageType,
      sendTime: data.sendTime
    }
    messages.value.push(newMsg)
    nextTick(() => {
      scrollBottomRef.value?.scrollIntoView({ behavior: 'smooth' })
    })
  }
}

// 加载群成员
const loadMembers = async () => {
  if (!props.group?.id) return
  try {
    const res = await getGroupMembersApi(props.group.id)
    memberList.value = res
  } catch (error) {
    console.error('加载群成员失败', error)
    ElMessage.error('加载群成员失败')
  }
}

// 加载好友列表（用于邀请）
const loadFriendList = async () => {
  try {
    const res = await getFriendListApi()
    const friends: FriendVO[] = []
    for (const group of res) {
      friends.push(...group.friends)
    }
    friendList.value = friends
  } catch (error) {
    console.error('加载好友列表失败', error)
  }
}

// 群管理命令
const handleGroupCommand = async (command: string) => {
  if (!props.group) return

  if (command === 'notice') {
    showNotice.value = true
  } else if (command === 'members') {
    await loadMembers()
    showMembers.value = true
  } else if (command === 'invite') {
    await loadFriendList()
    showInvite.value = true
  } else if (command === 'quit') {
    await ElMessageBox.confirm('确定要退出该群聊吗？', '提示', { type: 'warning' })
    await quitGroupApi(props.group.id)
    ElMessage.success('已退出群聊')
    emit('update:list')
  } else if (command === 'disband') {
    await ElMessageBox.confirm('确定要解散该群聊吗？此操作不可恢复', '提示', { type: 'warning' })
    await disbandGroupApi(props.group.id)
    ElMessage.success('群聊已解散')
    emit('update:list')
  }
}

// 邀请好友
const handleInvite = async () => {
  if (!selectedFriendId.value) {
    ElMessage.warning('请选择好友')
    return
  }
  if (!props.group?.id) return

  try {
    await inviteMemberApi({ groupId: props.group.id, userId: selectedFriendId.value })
    ElMessage.success('邀请已发送')
    showInvite.value = false
    selectedFriendId.value = null
  } catch (error) {
    console.error(error)
    ElMessage.error('邀请失败')
  }
}

// 监听群切换
watch(() => props.group, (newGroup) => {
  if (newGroup?.id) {
    loadHistory(true)
  }
}, { immediate: true })

// 注册 WebSocket 回调
onMounted(() => {
  websocketService.onGroupMessage(onGroupMessage)
})

onUnmounted(() => {
  // 清理回调（可选）
})
</script>

<style scoped>
.group-chat-window {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

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

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-item.own {
  flex-direction: row-reverse;
}

.message-item.own .message-content {
  align-items: flex-end;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.message-info {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.message-bubble {
  background: white;
  padding: 8px 12px;
  border-radius: 12px;
  word-wrap: break-word;
}

.message-item.own .message-bubble {
  background: #95ec69;
}

.message-input {
  padding: 12px;
  border-top: 1px solid #e4e7ed;
}

.input-actions {
  margin-top: 8px;
  text-align: right;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.member-info {
  flex: 1;
}

.owner-tag,
.admin-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 8px;
}

.owner-tag {
  background: #e6a23c;
  color: white;
}

.admin-tag {
  background: #409eff;
  color: white;
}
</style>