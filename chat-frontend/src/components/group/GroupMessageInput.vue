<template>
  <div class="message-input">
    <el-input v-model="content" type="textarea" :rows="3"
      :disabled="muted"
      :placeholder="muted ? '你已被禁言' : '请输入群消息...'"
      @keyup.ctrl.enter="handleSend" />
    <div class="input-actions">
      <el-tag v-if="muted" type="danger" effect="dark" size="small">你已被禁言</el-tag>
      <el-button v-else type="primary" @click="handleSend">发送 (Ctrl+Enter)</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 群聊消息输入组件，支持禁言状态 @component */
import { ref } from 'vue'

/** 组件属性：是否被禁言 */
defineProps<{
  muted?: boolean
}>()

/** 组件事件：发送消息 */
const emit = defineEmits<{
  (e: 'send', content: string): void
}>()

/** 输入框内容 */
const content = ref('')

/** 发送消息 @returns void */
const handleSend = () => {
  if (!content.value.trim()) return
  emit('send', content.value)
  content.value = ''
}
</script>

<style scoped>
.message-input {
  padding: 12px;
  border-top: 1px solid #e4e7ed;
}

.input-actions {
  margin-top: 8px;
  text-align: right;
}
</style>
