<template>
  <div class="impression-board">
    <div class="impression-header">
      <el-button type="primary" size="small" @click="showAddDialog = true">
        <el-icon>
          <Plus />
        </el-icon>
        添加评价
      </el-button>
    </div>

    <ImpressionList :impressions-to-me="impressionsToMe" :impressions-by-me="impressionsByMe"
      :loading-to-me="loadingToMe" :loading-by-me="loadingByMe" @delete="handleDelete" @add="showAddDialog = true"
      @tab-change="handleTabChange" />

    <AddImpressionDialog v-model="showAddDialog" :friend-list="friendList" :loading="loadingFriends"
      :preselect-user-id="props.targetUserId" @submit="submitImpression" />

    <ConfirmDialog v-model="showDeleteDialog" title="提示" :message="deleteConfirmMessage" type="danger"
      confirm-text="确定删除" cancel-text="取消" @confirm="confirmDelete" />
  </div>
</template>

<script setup lang="ts">
/** 好友印象/评价面板组件 @component */
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getImpressionsToMeApi,
  getImpressionsByMeApi,
  addImpressionApi,
  deleteImpressionApi,
  type ImpressionVO
} from '@/api/impression'
import { useFriendStore } from '@/stores/friendStore'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import ImpressionList from './ImpressionList.vue'
import AddImpressionDialog from './AddImpressionDialog.vue'

/** 外部传入的预选目标用户 ID（来自好友头像卡片"写印象"） */
const props = defineProps<{
  targetUserId?: number | null
}>()

const emit = defineEmits<{
  (e: 'clearTarget'): void
}>()

const friendStore = useFriendStore()

/** 我收到的评价数据 */
const impressionsToMe = ref<ImpressionVO[]>([])
/** 我给出的评价数据 */
const impressionsByMe = ref<ImpressionVO[]>([])
/** 加载状态 */
const loadingToMe = ref(false)
const loadingByMe = ref(false)
/** 好友列表 */
const friendList = ref<any[]>([])
/** 好友列表加载状态 */
const loadingFriends = ref(false)

/** 添加评价对话框显示状态 */
const showAddDialog = ref(false)

/** 监听外部传入的 targetUserId，自动打开添加评价弹窗 */
watch(() => props.targetUserId, (val) => {
  if (val) {
    showAddDialog.value = true
    emit('clearTarget')
  }
})
/** 删除确认对话框显示状态 */
const showDeleteDialog = ref(false)
/** 删除确认消息 */
const deleteConfirmMessage = ref('确定要删除这条评价吗？')
/** 待删除的评价 ID */
const pendingDeleteId = ref<number | null>(null)
/** 当前 Tab */
const currentTab = ref('to-me')

/** 加载「我收到的评价」数据 @returns Promise<void> */
const loadToMeData = async () => {
  loadingToMe.value = true
  try {
    const data = await getImpressionsToMeApi()
    impressionsToMe.value = data || []
  } catch (error) {
    console.error('加载对我的评价失败', error)
    ElMessage.error('加载评价失败')
  } finally {
    loadingToMe.value = false
  }
}

/** 加载「我给出的评价」数据 @returns Promise<void> */
const loadByMeData = async () => {
  loadingByMe.value = true
  try {
    const data = await getImpressionsByMeApi()
    impressionsByMe.value = data || []
  } catch (error) {
    console.error('加载我给出的评价失败', error)
    ElMessage.error('加载评价失败')
  } finally {
    loadingByMe.value = false
  }
}

/** 加载好友列表 @returns Promise<void> */
const loadFriendList = async () => {
  loadingFriends.value = true
  try {
    await friendStore.loadFriendList()
    const friends: any[] = []
    for (const group of friendStore.friendList) {
      friends.push(...group.friends)
    }
    friendList.value = friends
  } catch (error) {
    console.error('加载好友列表失败', error)
    ElMessage.error('加载好友列表失败')
  } finally {
    loadingFriends.value = false
  }
}

/** 提交评价 @param toUserId 目标用户 ID @param content 评价内容 @returns Promise<void> */
const submitImpression = async (toUserId: number, content: string) => {
  try {
    await addImpressionApi(toUserId, content)
    ElMessage.success('评价成功')
    if (currentTab.value === 'to-me') {
      await loadToMeData()
    } else {
      await loadByMeData()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('评价失败')
    throw error
  }
}

/** 删除评价 @param id 评价 ID @returns void */
const handleDelete = (id: number) => {
  pendingDeleteId.value = id
  deleteConfirmMessage.value = '确定要删除这条评价吗？删除后无法恢复。'
  showDeleteDialog.value = true
}

/** 确认删除 @returns Promise<void> */
const confirmDelete = async () => {
  if (!pendingDeleteId.value) return

  try {
    await deleteImpressionApi(pendingDeleteId.value)
    ElMessage.success('删除成功')
    await Promise.all([loadByMeData(), loadToMeData()])
  } catch (error) {
    console.error('删除失败', error)
    ElMessage.error('删除失败')
  } finally {
    pendingDeleteId.value = null
  }
}

/** Tab 切换回调 @param tab Tab 名称 @returns void */
const handleTabChange = (tab: string) => {
  currentTab.value = tab
}

onMounted(() => {
  loadToMeData()
  loadByMeData()
  loadFriendList()
})
</script>

<style scoped>
.impression-board {
  padding: 16px;
  height: 100%;
  overflow-y: auto;
}

.impression-header {
  margin-bottom: 16px;
  text-align: right;
}

.impression-header .el-button {
  border-radius: 14px !important;
  font-weight: 600 !important;
  height: 44px;
  font-size: 15px !important;
  padding: 0 22px !important;
}
</style>
