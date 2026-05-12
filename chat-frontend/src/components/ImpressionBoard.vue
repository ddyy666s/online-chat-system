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
      @submit="submitImpression" />

    <ConfirmDialog v-model="showDeleteDialog" title="提示" :message="deleteConfirmMessage" type="danger"
      confirm-text="确定删除" cancel-text="取消" @confirm="confirmDelete" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
import ImpressionList from './impression/ImpressionList.vue'
import AddImpressionDialog from './impression/AddImpressionDialog.vue'

const friendStore = useFriendStore()

// 数据状态
const impressionsToMe = ref<ImpressionVO[]>([])
const impressionsByMe = ref<ImpressionVO[]>([])
const loadingToMe = ref(false)
const loadingByMe = ref(false)
const friendList = ref<any[]>([])
const loadingFriends = ref(false)

// UI 状态
const showAddDialog = ref(false)
const showDeleteDialog = ref(false)
const deleteConfirmMessage = ref('确定要删除这条评价吗？')
const pendingDeleteId = ref<number | null>(null)
const currentTab = ref('to-me')

// 加载数据
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

// 加载好友列表
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

// 提交评价
const submitImpression = async (toUserId: number, content: string) => {
  try {
    await addImpressionApi(toUserId, content)
    ElMessage.success('评价成功')
    // 刷新当前 Tab 的数据
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

// 删除评价
const handleDelete = (id: number) => {
  pendingDeleteId.value = id
  deleteConfirmMessage.value = '确定要删除这条评价吗？删除后无法恢复。'
  showDeleteDialog.value = true
}

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
</style>