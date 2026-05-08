<template>
  <el-dialog v-model="visible" :title="`与 ${targetUser?.nickname} 语音通话`" width="400px" :before-close="handleClose">
    <div class="call-container">
      <div class="avatar">
        <el-avatar :size="80" :src="targetUser?.avatar || ''">
          {{ targetUser?.nickname?.charAt(0) || 'U' }}
        </el-avatar>
      </div>
      <div class="status">{{ isCalling ? '正在连接...' : '通话中' }}</div>
      <div class="duration" v-if="!isCalling">{{ formatDuration(duration) }}</div>
    </div>
    <template #footer>
      <el-button type="danger" @click="endCall">挂断</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  targetUser: any
  isCaller?: boolean
}>()

const emit = defineEmits(['update:modelValue', 'endCall'])

const visible = ref(false)
const isCalling = ref(true)
const duration = ref(0)
let timer: number | null = null

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    isCalling.value = true
    duration.value = 0
    timer = setInterval(() => {
      duration.value++
    }, 1000) as unknown as number
    setTimeout(() => {
      isCalling.value = false
    }, 2000)
  } else {
    if (timer) clearInterval(timer)
  }
}, { immediate: true })

const formatDuration = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const handleClose = () => {
  endCall()
}

const endCall = () => {
  if (timer) clearInterval(timer)
  visible.value = false
  emit('update:modelValue', false)
  emit('endCall')
  ElMessage.info('通话已结束')
}
</script>

<style scoped>
.call-container {
  text-align: center;
  padding: 20px;
}

.avatar {
  margin-bottom: 16px;
}

.status {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.duration {
  font-size: 24px;
  font-weight: 500;
  color: #409eff;
}
</style>