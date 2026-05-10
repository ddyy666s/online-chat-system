<template>
  <div class="image-message">
    <el-image :src="src" :preview-src-list="[src]" fit="cover" class="message-image" :lazy="true" />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  src: string
}>()
</script>

<style scoped>
.image-message {
  cursor: pointer;
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
}
</style>