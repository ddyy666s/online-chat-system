<template>
  <div class="sidebar-tabs">
    <div v-for="tab in visibleTabs" :key="tab.key" class="tab-item" :class="{ active: activeTab === tab.key }"
      @click="handleTabClick(tab)">
      <el-icon>
        <component :is="tab.icon" />
      </el-icon>
      <span>{{ tab.label }}</span>
      <span v-if="tab.badge && badgeCount > 0" class="badge">
        {{ badgeCount }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 侧边栏选项卡导航组件 @component */
import { computed } from 'vue'
import { User, ChatDotRound, Message, Star, Setting } from '@element-plus/icons-vue'

/** 组件属性：当前激活 Tab、角标数量、是否为管理员 */
const props = defineProps<{
  activeTab: string
  badgeCount: number
  isAdmin: boolean
}>()

/** 组件事件：切换 Tab、跳转管理后台 */
const emit = defineEmits<{
  'update:activeTab': [tab: string]
  'goToAdmin': []
}>()

/** 所有 Tab 定义 */
const allTabs = [
  { key: 'friends', label: '好友', icon: User, badge: false, requireAdmin: false },
  { key: 'groups', label: '群聊', icon: ChatDotRound, badge: false, requireAdmin: false },
  { key: 'requests', label: '申请', icon: Message, badge: true, requireAdmin: false },
  { key: 'impressions', label: '印象', icon: Star, badge: false, requireAdmin: false },
  { key: 'admin', label: '管理', icon: Setting, badge: false, requireAdmin: true }
]

/** 根据管理员权限过滤可见 Tab @returns 可见 Tab 列表 */
const visibleTabs = computed(() => {
  return allTabs.filter(tab => {
    if (tab.requireAdmin && !props.isAdmin) return false
    return true
  })
})

/** Tab 点击处理 @param tab 点击的 Tab 对象 @returns void */
const handleTabClick = (tab: any) => {
  if (tab.key === 'admin') {
    emit('goToAdmin')
  } else {
    emit('update:activeTab', tab.key)
  }
}
</script>

<style scoped>
.sidebar-tabs {
  display: flex;
  border-bottom: 1px solid #e8ecf0;
  background: #fafafa;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 14px 0;
  cursor: pointer;
  position: relative;
  font-size: 14px;
  color: #606266;
  transition: all 0.2s;
}

.tab-item:hover {
  color: #409eff;
  background: #f0f2f5;
}

.tab-item.active {
  color: #409eff;
  background: #fff;
  border-bottom: 2px solid #409eff;
}

.badge {
  position: absolute;
  top: 8px;
  right: 20px;
  background: #f56c6c;
  color: white;
  font-size: 10px;
  padding: 0 5px;
  border-radius: 10px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
}
</style>
