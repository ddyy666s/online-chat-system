<template>
  <div class="call-actions">
    <el-button v-if="showAccept" type="success" @click="onAccept" :icon="Phone">
      接听
    </el-button>
<<<<<<< HEAD
    <el-button type="danger" @click="onHangup" :icon="Close">
      挂断
=======
    <el-button v-if="showReject" type="danger" @click="onReject">
      <el-icon>
        <Close />
      </el-icon>
      拒绝
    </el-button>
    <el-button v-else type="danger" @click="onHangup">
      <el-icon>
        <Phone />
      </el-icon>
      {{ hangupText }}
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
    </el-button>
  </div>
</template>

<script setup lang="ts">
<<<<<<< HEAD
import { ref, computed } from 'vue'
=======
import { computed } from 'vue'
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
import { Phone, Close } from '@element-plus/icons-vue'

const props = defineProps<{
  isConnected: boolean
  isCaller: boolean
  showReject?: boolean
}>()

const emit = defineEmits<{
  (e: 'accept'): void
  (e: 'reject'): void
  (e: 'hangup'): void
}>()

const accepted = ref(false)

<<<<<<< HEAD
const showAccept = computed(() => !accepted.value && !props.isConnected && !props.isCaller)

const onAccept = () => {
  accepted.value = true
  emit('accept')
}
=======
const hangupText = computed(() => {
  if (props.isCaller && !props.isConnected) return '取消'
  return '挂断'
})

const onAccept = () => emit('accept')
const onReject = () => emit('reject')
>>>>>>> 6c92023f4f78951a0d3c61a8865456843a074ff7
const onHangup = () => emit('hangup')
</script>

<style scoped>
.call-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>
