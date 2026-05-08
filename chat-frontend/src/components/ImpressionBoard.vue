<template>
  <div class="impression-board">
    <!-- 添加评价按钮（固定在顶部） -->
    <div class="impression-header">
      <el-button type="primary" size="small" @click="openAddDialog">
        <el-icon>
          <Plus />
        </el-icon>
        添加评价
      </el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="对我的评价" name="to-me">
        <div v-if="impressionsToMe.length > 0">
          <div v-for="imp in impressionsToMe" :key="imp.id" class="impression-item">
            <div class="avatar">
              <el-avatar :size="36" :src="imp.fromUserAvatar || ''">
                {{ imp.fromUserNickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <div class="content">
              <div class="name">{{ imp.fromUserNickname }}</div>
              <div class="text">{{ imp.content }}</div>
              <div class="time">{{ formatDate(imp.createdAt) }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评价" />
      </el-tab-pane>

      <el-tab-pane label="我给出的评价" name="by-me">
        <div v-if="impressionsByMe.length > 0">
          <div v-for="imp in impressionsByMe" :key="imp.id" class="impression-item">
            <div class="avatar">
              <el-avatar :size="36" :src="imp.toUserAvatar || ''">
                {{ imp.toUserNickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <div class="content">
              <div class="name">{{ imp.toUserNickname }}</div>
              <div class="text">{{ imp.content }}</div>
              <div class="time">{{ formatDate(imp.createdAt) }}</div>
            </div>
            <div class="actions">
              <el-button type="danger" size="small" text @click="handleDelete(imp.id)">
                删除
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评价" />
      </el-tab-pane>
    </el-tabs>

    <!-- 添加评价弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加评价" width="400px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="选择好友">
          <el-select v-model="addForm.toUserId" placeholder="请选择要评价的好友" filterable style="width: 100%">
            <el-option v-for="friend in friendList" :key="friend.userId" :label="friend.remark || friend.nickname"
              :value="friend.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="addForm.content" type="textarea" :rows="3" placeholder="请输入评价内容（最多100字）" maxlength="100"
            show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitImpression">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getImpressionsToMeApi,
  getImpressionsByMeApi,
  addImpressionApi,
  deleteImpressionApi,
  type ImpressionVO
} from '@/api/impression'
import { useFriendStore } from '@/stores/friendStore'
import { formatDate } from '@/utils/date'

const friendStore = useFriendStore()

const activeTab = ref('to-me')
const impressionsToMe = ref<ImpressionVO[]>([])
const impressionsByMe = ref<ImpressionVO[]>([])
const friendList = ref<any[]>([])

const showAddDialog = ref(false)
const submitting = ref(false)
const addForm = ref({
  toUserId: null as number | null,
  content: ''
})

// 加载数据
const loadData = async () => {
  try {
    const [toMe, byMe] = await Promise.all([
      getImpressionsToMeApi(),
      getImpressionsByMeApi()
    ])
    impressionsToMe.value = toMe
    impressionsByMe.value = byMe
  } catch (error) {
    console.error('加载评价失败', error)
  }
}

// 加载好友列表（用于选择评价对象）
const loadFriendList = async () => {
  await friendStore.loadFriendList()
  const friends: any[] = []
  for (const group of friendStore.friendList) {
    friends.push(...group.friends)
  }
  friendList.value = friends
}

// 打开添加评价弹窗
const openAddDialog = () => {
  addForm.value = { toUserId: null, content: '' }
  showAddDialog.value = true
}

// 提交评价
const submitImpression = async () => {
  if (!addForm.value.toUserId) {
    ElMessage.warning('请选择要评价的好友')
    return
  }
  if (!addForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }

  submitting.value = true
  try {
    await addImpressionApi(addForm.value.toUserId, addForm.value.content)
    ElMessage.success('评价成功')
    showAddDialog.value = false

    // 刷新列表
    await loadData()
  } catch (error) {
    console.error(error)
    ElMessage.error('评价失败')
  } finally {
    submitting.value = false
  }
}

// 删除评价
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteImpressionApi(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
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

.impression-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #e4e7ed;
  position: relative;
}

.impression-item .content {
  flex: 1;
}

.impression-item .name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.impression-item .text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.impression-item .time {
  font-size: 12px;
  color: #909399;
}

.impression-item .actions {
  display: flex;
  align-items: center;
}
</style>