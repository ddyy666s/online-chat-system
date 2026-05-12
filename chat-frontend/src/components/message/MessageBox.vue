<template>
  <el-drawer v-model="visible" title="消息盒子" direction="rtl" size="440px" :before-close="handleClose" class="beautiful-drawer">
    <div class="message-box-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="聊天消息" name="chat">
          <MessageList :messages="unreadMessages" @click="handleMessageClick" />
        </el-tab-pane>
        <el-tab-pane :label="`系统通知${notificationCount > 0 ? ` (${notificationCount})` : ''}`" name="notification">
          <div class="notification-list" v-if="notifications.length > 0">
            <div v-for="n in notifications" :key="n.id" class="notification-item" @click="openNotification(n)">
              <div class="notif-icon-wrap">
                <el-icon class="notif-icon"><Bell /></el-icon>
              </div>
              <div class="notif-content">
                <div class="notif-title">{{ n.title }}</div>
                <div class="notif-meta">{{ n.adminNickname }} · {{ formatTime(n.createdAt) }}</div>
              </div>
              <el-icon class="notif-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
          <Empty v-else title="暂无系统通知" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <NotificationDialog v-model="showNotificationDetail" :notification="selectedNotification"
      @close="handleNotificationRead" />
  </el-drawer>
</template>

<script setup lang="ts">
/** 消息盒子抽屉组件，聚合未读聊天消息和系统通知 @component */
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, ArrowRight } from '@element-plus/icons-vue'
import { useMessageStore } from '@/stores/messageStore'
import { markAsReadApi } from '@/api/message'
import { getUnreadNotificationsApi, markNotificationAsReadApi } from '@/api/notification'
import type { SystemNotification } from '@/api/notification'
import MessageList from '../messageBox/MessageList.vue'
import NotificationDialog from '../messageBox/NotificationDialog.vue'
import Empty from '../common/Empty.vue'

/** 组件属性：显示状态 */
const props = defineProps<{
  modelValue: boolean
}>()

/** 组件事件：更新显示状态 */
const emit = defineEmits(['update:modelValue'])
const router = useRouter()
const messageStore = useMessageStore()

/** 抽屉可见性（双向绑定） */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/** 当前 Tab */
const activeTab = ref('chat')
/** 系统通知列表 */
const notifications = ref<SystemNotification[]>([])
/** 未读通知数量 */
const notificationCount = ref(0)
/** 通知详情对话框显示状态 */
const showNotificationDetail = ref(false)
/** 当前选中的通知 */
const selectedNotification = ref<SystemNotification | null>(null)

/** 未读消息列表（从 store 转换） */
const unreadMessages = computed(() => {
  const messages = messageStore.unreadCount?.messages || []
  return messages.map((msg: any) => ({
    id: msg.id,
    fromUserId: msg.fromUserId,
    fromUserNickname: msg.fromUserNickname,
    fromUserAvatar: msg.fromUserAvatar || undefined,
    content: msg.content,
    messageType: msg.messageType ?? 0,
    sendTime: msg.sendTime,
    unreadCount: msg.unreadCount || 1,
    isOnline: msg.isOnline || false
  }))
})

/** 格式化时间 @param time ISO 时间字符串 @returns 格式化时间文本 */
const formatTime = (time: string) => {
  const d = new Date(time)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  if (isToday) return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  const yesterday = new Date(now.getTime() - 86400000).toDateString()
  if (d.toDateString() === yesterday) return '昨天 ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return `${d.getMonth() + 1}/${d.getDate()}`
}

/** 加载未读通知 @returns Promise<void> */
const loadNotifications = async () => {
  const res = await getUnreadNotificationsApi()
  if (res && res.notifications) {
    notifications.value = res.notifications
    notificationCount.value = res.total || 0
  }
}

/** 打开通知详情 @param n 通知对象 @returns void */
const openNotification = (n: SystemNotification) => {
  selectedNotification.value = n
  showNotificationDetail.value = true
}

/** 处理通知已读 @returns Promise<void> */
const handleNotificationRead = async () => {
  if (selectedNotification.value) {
    try {
      await markNotificationAsReadApi(selectedNotification.value.id)
      notifications.value = notifications.value.filter(n => n.id !== selectedNotification.value!.id)
      notificationCount.value = Math.max(0, notificationCount.value - 1)
    } catch { /* ignore */ }
  }
  selectedNotification.value = null
}

/** 关闭抽屉 @returns void */
const handleClose = () => {
  visible.value = false
}

/** 点击未读消息，跳转至对应聊天 @param friendId 好友 ID @returns Promise<void> */
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

/** 打开抽屉时加载数据 */
watch(() => props.modelValue, (val) => {
  if (val) {
    messageStore.loadUnreadCount()
    loadNotifications()
  }
})
</script>

<style scoped>
.message-box-tabs {
  height: 100%;
}

.message-box-tabs :deep(.el-tabs__content) {
  height: calc(100% - 40px);
  overflow: hidden;
}

.message-box-tabs :deep(.el-tab-pane) {
  height: 100%;
  overflow-y: auto;
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 14px;
  border-radius: 14px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.25s;
  background: var(--bg-color-white);
  border: 1px solid var(--border-color-extra-light);
  position: relative;
  overflow: hidden;
  animation: slideInItem 0.35s ease both;
}

@keyframes slideInItem {
  from { opacity: 0; transform: translateX(-12px); }
  to { opacity: 1; transform: translateX(0); }
}

.notification-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: linear-gradient(180deg, var(--color-warning), #f0a830);
  border-radius: 0 3px 3px 0;
  opacity: 0;
  transition: opacity 0.25s;
}

.notification-item:hover {
  background: linear-gradient(135deg, #fef9e7, #fef3d5);
  border-color: #ffe7ba;
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(230, 162, 60, 0.12);
}

.notification-item:hover::before {
  opacity: 1;
}

.notif-icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fef3d5, #fdecc0);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.25s;
}

.notification-item:hover .notif-icon-wrap {
  background: linear-gradient(135deg, var(--color-warning), #f0a830);
  transform: scale(1.08);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.notif-icon {
  font-size: 20px;
  color: #b8860b;
  transition: color 0.25s;
}

.notification-item:hover .notif-icon {
  color: white;
}

.notif-content {
  flex: 1;
  min-width: 0;
}

.notif-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.notif-meta {
  font-size: 12px;
  color: var(--text-secondary);
}

.notif-arrow {
  color: var(--text-secondary);
  flex-shrink: 0;
  font-size: 16px;
  transition: all 0.25s;
}

.notification-item:hover .notif-arrow {
  color: #b8860b;
  transform: translateX(4px);
}
</style>
