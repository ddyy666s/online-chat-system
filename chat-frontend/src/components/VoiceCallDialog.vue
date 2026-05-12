<template>
  <el-dialog v-model="visible" title="语音通话" width="400px" :close-on-click-modal="false" :show-close="false">
    <div style="text-align:center;padding:20px">
      <el-avatar :size="100">{{ (targetNickname || 'U').charAt(0) }}</el-avatar>
      <div style="margin-top:12px;font-size:18px">{{ targetNickname || '对方' }}</div>
      <div style="margin-top:8px;color:#909399">{{ isConnected ? '通话中' : (isCaller ? '正在呼叫...' : '来电') }}</div>
    </div>
    <template #footer>
      <el-button type="danger" @click="hangupCall">挂断</el-button>
      <el-button v-if="!isCaller && !isConnected" type="success" @click="acceptCall">接听</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { websocketService } from '@/utils/websocket'
import { useUserStore } from '@/stores/userStore'
import { useAliRTC } from '@/composables/useAliRTC'

const props = defineProps<{
  modelValue: boolean
  targetUserId: number | undefined
  targetNickname?: string
  isCaller: boolean
}>()

const emit = defineEmits(['update:modelValue', 'endCall'])
const userStore = useUserStore()
const visible = ref(false)
const { isConnected, startCall, acceptCall, hangup } = useAliRTC()
let channelId = ''

const hangupCall = async () => {
  await hangup()
  visible.value = false
  emit('update:modelValue', false)
  emit('endCall')
}

const acceptCallHandler = async () => {
  if (!channelId) { ElMessage.error('房间信息缺失'); return }
  await acceptCall(channelId, String(userStore.userInfo?.id), userStore.userInfo?.nickname || '用户')
}

const handleSignal = async (data: any) => {
  if (data.action === 'call' && !props.isCaller) {
    channelId = data.channelId
  }
}

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

onUnmounted(() => hangup())
</script>