<template>
  <el-dialog v-model="visible" title="群成员" width="400px">
    <div v-for="member in members" :key="member.userId" class="member-item">
      <el-avatar :size="32" :src="member.avatar || ''">
        {{ member.nickname?.charAt(0) || 'U' }}
      </el-avatar>
      <div class="member-info">
        <span>{{ member.nickname }}</span>
        <span v-if="member.role === 2" class="owner-tag">群主</span>
        <span v-else-if="member.role === 1" class="admin-tag">管理员</span>
      </div>
    </div>
    <Empty v-if="members.length === 0" description="暂无群成员" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Empty from '@/components/common/Empty.vue'

const props = defineProps<{
  modelValue: boolean
  members: any[]
}>()

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style scoped>
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