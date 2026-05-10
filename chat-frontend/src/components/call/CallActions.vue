<template>
  <div class="call-actions">
    <el-button v-if="showAccept" type="success" @click="onAccept">
      <el-icon>
        <Phone />
      </el-icon>
      接听
    </el-button>
    <el-button type="danger" @click="onHangup">
      <el-icon>
        <Phone />
      </el-icon>
      挂断
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { Phone } from '@element-plus/icons-vue'

const props = defineProps<{
  isConnected: boolean
  isCaller: boolean
}>()

const emit = defineEmits<{
  (e: 'accept'): void
  (e: 'hangup'): void
}>()

const showAccept = !props.isConnected && !props.isCaller

const onAccept = () => emit('accept')
const onHangup = () => emit('hangup')
</script>

<style scoped>
.call-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>