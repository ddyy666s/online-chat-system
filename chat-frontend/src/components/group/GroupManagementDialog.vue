<template>
  <el-dialog v-model="visible" title="群管理" width="620px" @close="handleClose">
    <div style="margin-bottom:12px;display:flex;align-items:center;gap:8px">
      <el-button v-if="canManage" size="small" @click="toggleSelectAll">全选/取消</el-button>
      <el-button v-if="selectedIds.length > 0" size="small" type="warning"
        @click="openBatchMute">批量禁言({{ selectedIds.length }})</el-button>
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
    <PromptDialog v-model="showPromptMute" title="禁言" message="禁言时长(分钟)："
      confirm-text="确定" cancel-text="取消" input-value="60"
      :input-pattern="/^\d+$/" input-error-message="请输入数字"
      @confirm="onMuteConfirm" />
    <PromptDialog v-model="showPromptBatch" title="批量禁言" message="禁言时长(分钟)："
      confirm-text="确定" cancel-text="取消" input-value="60"
      :input-pattern="/^\d+$/" input-error-message="请输入数字"
      @confirm="onBatchMuteConfirm" />
  </el-dialog>
</template>

<script setup lang="ts">
/** 群管理对话框组件，支持查看成员、设置管理员、禁言、移除等操作 @component */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  setGroupAdminApi, removeGroupAdminApi,
  removeGroupMemberApi, muteGroupMemberApi, unmuteGroupMemberApi, batchMuteGroupApi,
  type GroupMemberVO
} from '@/api/group'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import PromptDialog from '@/components/common/PromptDialog.vue'

/** 组件属性：显示状态、群 ID、成员列表、当前用户、是否为群主、是否可管理 */
const props = defineProps<{
  modelValue: boolean
  groupId: number
  members: GroupMemberVO[]
  currentUserId: number
  isOwner: boolean
  canManage: boolean
}>()

/** 组件事件：更新显示状态、刷新数据 */
const emit = defineEmits(['update:modelValue', 'refresh'])

/** 对话框可见性 */
const visible = ref(false)
/** 批量操作选中的用户 ID 列表 */
const selectedIds = ref<number[]>([])
/** 移除确认对话框显示状态 */
const showKickDialog = ref(false)
/** 待移除的成员 */
const kickTarget = ref<GroupMemberVO | null>(null)

/** 同步外部 modelValue 到内部 visible */
watch(() => props.modelValue, (val) => { visible.value = val }, { immediate: true })

/** 判断是否可以移除成员 @param row 成员对象 @returns boolean */
function canKick(row: GroupMemberVO) {
  if (!props.canManage || row.userId === props.currentUserId) return false
  if (row.role === 2) return false
  if (row.role === 1 && !props.isOwner) return false
  return true
}

/** 判断是否可管理成员的禁言状态 @param row 成员对象 @returns boolean */
function canManageMute(row: GroupMemberVO) {
  if (!props.canManage || row.userId === props.currentUserId) return false
  if (row.role === 2) return false
  if (row.role === 1 && !props.isOwner) return false
  return true
}

/** 判断成员是否可选择（用于批量操作） @param r 成员对象 @returns boolean */
function isSelectable(r: GroupMemberVO) {
  if (r.role === 2 || r.userId === props.currentUserId) return false
  if (r.role === 1 && !props.isOwner) return false
  return true
}

/** 表格选择变化回调 @param rows 选中的行 @returns void */
const onSelectionChange = (rows: GroupMemberVO[]) => {
  selectedIds.value = rows.filter(isSelectable).map(r => r.userId)
}

/** 全选/取消全选状态标记 */
let allSelected = false
/** 切换全选/取消 @returns void */
const toggleSelectAll = () => {
  allSelected = !allSelected
  selectedIds.value = allSelected
    ? props.members.filter(isSelectable).map(r => r.userId)
    : []
}

/** 禁言对话框显示状态 */
const showPromptMute = ref(false)
/** 待禁言成员 */
const pendingMuteRow = ref<GroupMemberVO | null>(null)
/** 禁言分钟数结果 */
const muteMinutesResult = ref(0)

/** 打开禁言输入弹窗 @param row 目标成员 @returns void */
const openMutePrompt = (row: GroupMemberVO) => {
  pendingMuteRow.value = row
  muteMinutesResult.value = 0
  showPromptMute.value = true
}

/** 确认禁言回调 @param minutes 禁言分钟数字符串 @returns Promise<void> */
const onMuteConfirm = async (minutes: string) => {
  muteMinutesResult.value = parseInt(minutes, 10)
  showPromptMute.value = false
  if (!props.groupId || !pendingMuteRow.value) return
  try {
    await muteGroupMemberApi(props.groupId, pendingMuteRow.value.userId, muteMinutesResult.value)
    ElMessage.success(`已禁言 ${muteMinutesResult.value} 分钟`)
    pendingMuteRow.value = null
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  }
}

/** 切换管理员状态 @param row 目标成员 @returns Promise<void> */
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

/** 打开移除确认弹窗 @param row 目标成员 @returns void */
const handleKick = (row: GroupMemberVO) => {
  kickTarget.value = row
  showKickDialog.value = true
}

/** 确认移除成员 @returns Promise<void> */
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

/** 切换禁言/取消禁言 @param row 目标成员 @returns Promise<void> */
const toggleMute = async (row: GroupMemberVO) => {
  if (!props.groupId) return
  if (row.muted) {
    try {
      await unmuteGroupMemberApi(props.groupId, row.userId)
      ElMessage.success('已取消禁言')
      emit('refresh')
    } catch (e: any) {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  } else {
    openMutePrompt(row)
  }
}

/** 批量禁言对话框显示状态 */
const showPromptBatch = ref(false)
/** 打开批量禁言弹窗 @returns void */
const openBatchMute = () => {
  muteMinutesResult.value = 0
  showPromptBatch.value = true
}

/** 确认批量禁言 @param minutes 禁言分钟数字符串 @returns Promise<void> */
const onBatchMuteConfirm = async (minutes: string) => {
  showPromptBatch.value = false
  if (!props.groupId || selectedIds.value.length === 0) return
  const m = parseInt(minutes, 10)
  try {
    await batchMuteGroupApi(props.groupId, selectedIds.value, m)
    ElMessage.success(`已批量禁言 ${selectedIds.value.length} 人`)
    selectedIds.value = []
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '批量禁言失败')
  }
}

/** 关闭对话框 @returns void */
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}
</script>
