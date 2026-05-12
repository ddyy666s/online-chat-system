<template>
  <div class="friend-list">
    <div class="friend-header">
      <h3>好友列表</h3>
      <el-button :icon="Plus" size="small" circle @click="showAddDialog = true" />
    </div>

    <el-scrollbar class="friend-scroll">
      <FriendGroup v-for="group in friendStore.friendList" :key="group.groupName" :group-name="group.groupName"
        :friends="group.friends" :current-chat-user-id="currentChatUserId" @select-chat="handleSelectChat"
        @command="handleCommand" @write-impression="(uid) => emit('writeImpression', uid)" />
    </el-scrollbar>

    <AddFriendDialog v-model="showAddDialog" @success="refresh" />
    <ConfirmDialog v-model="showDeleteDialog" title="删除好友" :message="deleteMessage" type="danger"
      confirm-text="删除" cancel-text="取消" @confirm="confirmDelete" />
    <PromptDialog v-model="showRemarkPrompt" title="修改备注" message="请输入备注"
      confirm-text="确定" cancel-text="取消" placeholder="请输入备注名称"
      @confirm="onRemarkConfirm" />
    <PromptDialog v-model="showMovePrompt" title="移动分组" message="请输入分组名称"
      confirm-text="确定" cancel-text="取消" placeholder="请输入分组名称"
      @confirm="onMoveConfirm" />
  </div>
</template>

<script setup lang="ts">
/** 好友列表组件，管理好友展示/搜索/删除/备注/分组操作 @component */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useFriendStore } from '@/stores/friendStore'
import { deleteFriendApi, updateFriendRemarkApi, moveFriendGroupApi } from '@/api/friend'
import FriendGroup from './FriendGroup.vue'
import AddFriendDialog from './AddFriendDialog.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import PromptDialog from '@/components/common/PromptDialog.vue'

/** 组件属性：当前聊天用户 ID */
const props = defineProps<{
  currentChatUserId: number | null
}>()

/** 组件事件：选择聊天 */
const emit = defineEmits<{
  (e: 'selectChat', friend: any): void
  (e: 'writeImpression', userId: number): void
}>()

/** 好友状态 store */
const friendStore = useFriendStore()
/** 添加好友对话框显示状态 */
const showAddDialog = ref(false)
/** 删除确认对话框显示状态 */
const showDeleteDialog = ref(false)
/** 删除确认消息 */
const deleteMessage = ref('')
/** 待删除的目标用户 ID */
const deleteTargetId = ref<number>(0)
/** 修改备注对话框显示状态 */
const showRemarkPrompt = ref(false)
/** 移动分组对话框显示状态 */
const showMovePrompt = ref(false)
/** 待处理的好友对象（用于备注/分组操作） */
let pendingFriend: any = null

/** 组件挂载时加载好友列表和申请 */
onMounted(() => {
  friendStore.loadFriendList()
  friendStore.loadFriendRequests()
})

/** 选择聊天好友 @param friend 好友对象 @returns void */
const handleSelectChat = (friend: any) => {
  emit('selectChat', friend)
}

/** 处理好友操作命令 @param command 命令标识 @param friend 好友对象 @returns void */
const handleCommand = (command: string, friend: any) => {
  if (command === 'remark') {
    pendingFriend = friend
    showRemarkPrompt.value = true
  } else if (command === 'move') {
    pendingFriend = friend
    showMovePrompt.value = true
  } else if (command === 'delete') {
    deleteMessage.value = `确定要删除好友 <strong>${friend.nickname}</strong> 吗？`
    deleteTargetId.value = friend.userId
    showDeleteDialog.value = true
  }
}

/** 确认修改备注 @param value 新备注名 @returns Promise<void> */
const onRemarkConfirm = async (value: string) => {
  const friend = pendingFriend
  pendingFriend = null
  if (!friend || value === undefined) return
  try {
    await updateFriendRemarkApi(friend.userId, value)
    ElMessage.success('修改成功')
    friendStore.loadFriendList()
  } catch { ElMessage.error('修改失败') }
}

/** 确认移动分组 @param value 新分组名称 @returns Promise<void> */
const onMoveConfirm = async (value: string) => {
  const friend = pendingFriend
  pendingFriend = null
  if (!friend || !value || value === friend.groupName) return
  try {
    await moveFriendGroupApi(friend.userId, value)
    ElMessage.success('移动成功')
    friendStore.loadFriendList()
  } catch { ElMessage.error('移动失败') }
}

/** 确认删除好友 @returns Promise<void> */
const confirmDelete = async () => {
  try {
    await deleteFriendApi(deleteTargetId.value)
    ElMessage.success('删除成功')
    refresh()
  } catch { ElMessage.error('删除失败') }
}

/** 刷新好友列表和申请 @returns void */
const refresh = () => {
  friendStore.loadFriendList()
  friendStore.loadFriendRequests()
}
</script>

<style scoped>
.friend-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.friend-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
}

.friend-header h3 {
  margin: 0;
  font-size: 16px;
}

.friend-scroll {
  flex: 1;
  overflow-y: auto;
}
</style>
