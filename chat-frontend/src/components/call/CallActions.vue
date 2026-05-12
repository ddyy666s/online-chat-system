<template>
  <div class="call-actions">
    <el-button v-if="showAccept" type="success" @click="onAccept" :icon="Phone">
      接听
    </el-button>
    <el-button type="danger" @click="onHangup" :icon="Close">
      挂断
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Phone, Close } from '@element-plus/icons-vue'

const props = defineProps<{
  isConnected: boolean
  isCaller: boolean
}>()

const emit = defineEmits<{
  (e: 'accept'): void
  (e: 'hangup'): void
}>()

const accepted = ref(false)

const showAccept = computed(() => !accepted.value && !props.isConnected && !props.isCaller)

const onAccept = () => {
  accepted.value = true
  emit('accept')
}
const onHangup = () => emit('hangup')
</script>

<style scoped>
.call-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>