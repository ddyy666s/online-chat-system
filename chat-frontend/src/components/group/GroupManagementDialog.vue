<template>
  <el-dialog v-model="visible" title="群管理" width="620px" @close="handleClose">
    <div style="margin-bottom:12px;display:flex;align-items:center;gap:8px">
      <el-button v-if="canManage" size="small" @click="toggleSelectAll">全选/取消</el-button>
      <el-button v-if="selectedIds.length > 0" size="small" type="warning"
        @click="batchMute">批量禁言({{ selectedIds.length }})</el-button>
    </div>
    <el-table :data="members" style="width: 100%" max-height="400" @selection-change="onSelectionChange">
      <el-table-column v-if="canManage" type="selection" width="40" />
      <el-table-column label="成员" min-width="140">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:8px">
            <el-avatar :size="32" :src="row.avatar || ''">{{ row.nickname?.charAt(0) }}</el-avatar>
            <span>{{ row.nickname }}</span>
            <el-tag v-if="row.muted" size="small" type="danger">禁言中</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="身份" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.role === 2" type="warning" size="small">群主</el-tag>
          <el-tag v-else-if="row.role === 1" type="success" size="small">管理员</el-tag>
          <span v-else>成员</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button v-if="isOwner && row.role !== 2 && row.userId !== currentUserId"
            size="small" type="primary" plain @click="toggleAdmin(row)">
            {{ row.role === 1 ? '取消管理' : '设为管理' }}
          </el-button>
          <el-button v-if="canKick(row)" size="small" type="danger" plain
            @click="handleKick(row)">移除</el-button>
          <el-button v-if="canManageMute(row)" size="small" plain
            @click="toggleMute(row)">{{ row.muted ? '取消禁言' : '禁言' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <ConfirmDialog v-model="showKickDialog" title="移除成员"
      :message="`确定移除成员 ${kickTarget?.nickname || ''}？`"
      type="danger" confirm-text="移除" cancel-text="取消" @confirm="confirmKick" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  setGroupAdminApi, removeGroupAdminApi,
  removeGroupMemberApi, muteGroupMemberApi, unmuteGroupMemberApi, batchMuteGroupApi,
  type GroupMemberVO
} from '@/api/group'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const props = defineProps<{
  modelValue: boolean
  groupId: number
  members: GroupMemberVO[]
  currentUserId: number
  isOwner: boolean
  canManage: boolean
}>()

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = ref(false)
const selectedIds = ref<number[]>([])
const showKickDialog = ref(false)
const kickTarget = ref<GroupMemberVO | null>(null)

watch(() => props.modelValue, (val) => { visible.value = val }, { immediate: true })

function canKick(row: GroupMemberVO) {
  if (!props.canManage || row.userId === props.currentUserId) return false
  if (row.role === 2) return false
  if (row.role === 1 && !props.isOwner) return false
  return true
}

function canManageMute(row: GroupMemberVO) {
  if (!props.canManage || row.userId === props.currentUserId) return false
  if (row.role === 2) return false
  if (row.role === 1 && !props.isOwner) return false
  return true
}

function isSelectable(r: GroupMemberVO) {
  if (r.role === 2 || r.userId === props.currentUserId) return false
  if (r.role === 1 && !props.isOwner) return false
  return true
}

const onSelectionChange = (rows: GroupMemberVO[]) => {
  selectedIds.value = rows.filter(isSelectable).map(r => r.userId)
}

let allSelected = false
const toggleSelectAll = () => {
  allSelected = !allSelected
  selectedIds.value = allSelected
    ? props.members.filter(isSelectable).map(r => r.userId)
    : []
}

const muteMinutes = async (): Promise<number> => {
  try {
    const { value } = await ElMessageBox.prompt('禁言时长(分钟)：', '禁言', {
      inputValue: '60', inputPattern: /^\d+$/, inputErrorMessage: '请输入数字'
    })
    return parseInt(value, 10)
  } catch { return 0 }
}

const toggleAdmin = async (row: GroupMemberVO) => {
  if (!props.groupId) return
  try {
    if (row.role === 1) {
      await removeGroupAdminApi(props.groupId, row.userId)
      ElMessage.success('已取消管理员')
    } else {
      await setGroupAdminApi(props.groupId, row.userId)
      ElMessage.success('已设置管理员')
    }
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

const handleKick = (row: GroupMemberVO) => {
  kickTarget.value = row
  showKickDialog.value = true
}

const confirmKick = async () => {
  if (!props.groupId || !kickTarget.value) return
  try {
    await removeGroupMemberApi(props.groupId, kickTarget.value.userId)
    ElMessage.success('已移除')
    showKickDialog.value = false
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '移除失败')
  }
}

const toggleMute = async (row: GroupMemberVO) => {
  if (!props.groupId) return
  try {
    if (row.muted) {
      await unmuteGroupMemberApi(props.groupId, row.userId)
      ElMessage.success('已取消禁言')
    } else {
      const minutes = await muteMinutes()
      if (!minutes) return
      await muteGroupMemberApi(props.groupId, row.userId, minutes)
      ElMessage.success(`已禁言 ${minutes} 分钟`)
    }
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

const batchMute = async () => {
  if (!props.groupId || selectedIds.value.length === 0) return
  const minutes = await muteMinutes()
  if (!minutes) return
  try {
    await batchMuteGroupApi(props.groupId, selectedIds.value, minutes)
    ElMessage.success(`已批量禁言 ${selectedIds.value.length} 人`)
    selectedIds.value = []
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '批量禁言失败')
  }
}

const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}
</script>