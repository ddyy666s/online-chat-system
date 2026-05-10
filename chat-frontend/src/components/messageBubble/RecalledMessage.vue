<template>
  <span class="recalled-text">
    {{ isOwn ? '你撤回了一条消息' : '对方撤回了一条消息' }}
  </span>
</template>

<script setup lang="ts">
defineProps<{
  isOwn: boolean
}>()
</script>

<style scoped>
.recalled-text {
  color: #909399;
  font-style: italic;
  font-size: 13px;
}
</style>