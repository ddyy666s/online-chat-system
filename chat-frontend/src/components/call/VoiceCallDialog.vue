<template>
  <el-dialog v-model="visible" title="语音通话" width="400px" :close-on-click-modal="false" :show-close="false">
    <div style="text-align:center;padding:20px">
      <el-avatar :size="100">{{ (targetNickname || 'U').charAt(0) }}</el-avatar>
      <div style="margin-top:12px;font-size:18px">{{ targetNickname || '对方' }}</div>
      <div style="margin-top:8px;color:#909399">{{ isConnected ? '通话中' : (isCaller ? '正在呼叫...' : '来电') }}</div>
    </div>
    <template #footer>
      <el-button type="danger" @click="hangupCall">挂断</el-button>
      <el-button v-if="!isCaller && !isConnected" type="success" @click="acceptCallHandler">接听</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/** 语音通话对话框组件（基于阿里云 RTC 的简化版） @component */
import { ref, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useAliRTC } from '@/composables/useAliRTC'

/** 组件属性：对话框显示状态、目标用户信息、是否为呼叫方 */
const props = defineProps<{
  modelValue: boolean
  targetUserId: number | undefined
  targetNickname?: string
  isCaller: boolean
}>()

/** 组件事件：更新对话框状态、结束通话 */
const emit = defineEmits(['update:modelValue', 'endCall'])
/** 用户状态 store */
const userStore = useUserStore()
/** 对话框可见性 */
const visible = ref(false)
/** 使用阿里云 RTC composable */
const { isConnected, startCall, acceptCall, hangup } = useAliRTC()
/** 当前频道 ID */
let channelId = ''

/** 挂断通话 @returns Promise<void> */
const hangupCall = async () => {
  await hangup()
  visible.value = false
  emit('update:modelValue', false)
  emit('endCall')
}

/** 接听通话处理器 @returns Promise<void> */
const acceptCallHandler = async () => {
  if (!channelId) { ElMessage.error('房间信息缺失'); return }
  await acceptCall(channelId, String(userStore.userInfo?.id), userStore.userInfo?.nickname || '用户')
}

/** 处理通话信令 @param data 信令数据 */
const handleSignal = async (data: any) => {
  if (data.action === 'call' && !props.isCaller) {
    channelId = data.channelId
  }
}

/** 监听对话框显示状态，自动发起或清理通话 */
watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val) {
    websocketService.onCallSignal(handleSignal)
    if (props.isCaller) {
      channelId = `room_${Date.now()}`
      await startCall(channelId, String(userStore.userInfo?.id), userStore.userInfo?.nickname || '用户')
      websocketService.sendCallSignal({
        action: 'call',
        toUserId: props.targetUserId,
        callType: 'voice',
        channelId
      })
    }
  } else {
    hangup()
  }
}, { immediate: true })

/** 组件卸载时挂断 */
onUnmounted(() => hangup())
</script>
