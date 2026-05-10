<template>
  <div class="emoji-grid">
    <div v-for="emoji in emojis" :key="emoji.id" class="emoji-item" @click="$emit('select', emoji)">
      <img :src="emoji.url" />
      <span>{{ emoji.name }}</span>
      <el-button v-if="showDelete" class="delete-btn" size="small" text @click.stop="$emit('delete', emoji.id)">
        删除
      </el-button>
    </div>
    <Empty v-if="emojis.length === 0" :description="emptyText" />
  </div>
</template>

<script setup lang="ts">
import Empty from '@/components/common/Empty.vue'

defineProps<{
  emojis: any[]
  showDelete?: boolean
  emptyText?: string
}>()

defineEmits<{
  (e: 'select', emoji: any): void
  (e: 'delete', id: number): void
}>()
</script>

<style scoped>
.emoji-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.emoji-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 10px;
  border-radius: 12px;
  position: relative;
}

.emoji-item:hover {
  background: #f5f5f5;
}

.emoji-item img {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.delete-btn {
  position: absolute;
  top: 0;
  right: 0;
  opacity: 0;
}

.emoji-item:hover .delete-btn {
  opacity: 1;
}
</style>