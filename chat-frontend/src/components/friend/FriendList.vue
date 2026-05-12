<template>
  <div class="friend-list">
    <div class="friend-header">
      <h3>好友列表</h3>
      <el-button :icon="Plus" size="small" circle @click="showAddDialog = true" />
    </div>

    <el-scrollbar class="friend-scroll">
      <FriendGroup v-for="group in friendStore.friendList" :key="group.groupName" :group-name="group.groupName"
        :friends="group.friends" :current-chat-user-id="currentChatUserId" @select-chat="handleSelectChat"
        @command="handleCommand" />
    </el-scrollbar>

    <AddFriendDialog v-model="showAddDialog" @success="refresh" />
    <ConfirmDialog v-model="showDeleteDialog" title="删除好友" :message="deleteMessage" type="danger"
      confirm-text="删除" cancel-text="取消" @confirm="confirmDelete" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useFriendStore } from '@/stores/friendStore'
import { deleteFriendApi, updateFriendRemarkApi, moveFriendGroupApi } from '@/api/friend'
import FriendGroup from './FriendGroup.vue'
import AddFriendDialog from './AddFriendDialog.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const props = defineProps<{
  currentChatUserId: number | null
}>()

const emit = defineEmits<{
  (e: 'selectChat', friend: any): void
}>()

const friendStore = useFriendStore()
const showAddDialog = ref(false)
const showDeleteDialog = ref(false)
const deleteMessage = ref('')
const deleteTargetId = ref<number>(0)

onMounted(() => {
  friendStore.loadFriendList()
  friendStore.loadFriendRequests()
})

const handleSelectChat = (friend: any) => {
  emit('selectChat', friend)
}

const handleCommand = async (command: string, friend: any) => {
  if (command === 'remark') {
    const { value } = await ElMessageBox.prompt('请输入备注', '修改备注', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: friend.remark || '',
      inputPlaceholder: '请输入备注名称'
    })
    if (value !== undefined) {
      try {
        await updateFriendRemarkApi(friend.userId, value)
        ElMessage.success('修改成功')
        friendStore.loadFriendList()
      } catch {
        ElMessage.error('修改失败')
      }
    }
  } else if (command === 'move') {
    const { value } = await ElMessageBox.prompt('请输入分组名称', '移动分组', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: friend.groupName,
      inputPlaceholder: '请输入分组名称'
    })
    if (value && value !== friend.groupName) {
      try {
        await moveFriendGroupApi(friend.userId, value)
        ElMessage.success('移动成功')
        friendStore.loadFriendList()
      } catch {
        ElMessage.error('移动失败')
      }
    }
  } else if (command === 'delete') {
    deleteMessage.value = `确定要删除好友 <strong>${friend.nickname}</strong> 吗？`
    deleteTargetId.value = friend.userId
    showDeleteDialog.value = true
  }
}

const confirmDelete = async () => {
  try {
    await deleteFriendApi(deleteTargetId.value)
    ElMessage.success('删除成功')
    refresh()
  } catch { ElMessage.error('删除失败') }
}

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