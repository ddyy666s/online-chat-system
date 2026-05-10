<template>
  <el-drawer v-model="visible" title="表情包" direction="btt" size="400px" @close="handleClose">
    <div class="emoji-container">
      <div class="emoji-tabs">
        <el-button size="small" :type="activeTab === 'system' ? 'primary' : 'default'" @click="activeTab = 'system'">
          系统表情
        </el-button>
        <el-button size="small" :type="activeTab === 'user' ? 'primary' : 'default'" @click="activeTab = 'user'">
          我的表情
        </el-button>
        <el-button size="small" @click="$emit('upload')">上传表情</el-button>
      </div>

      <EmojiGrid v-if="activeTab === 'system'" :emojis="systemEmojis" :show-delete="false" empty-text="暂无系统表情"
        @select="$emit('select', $event)" />

      <EmojiGrid v-if="activeTab === 'user'" :emojis="userEmojis" :show-delete="true" empty-text="暂无自定义表情"
        @select="$emit('select', $event)" @delete="$emit('delete', $event)" />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import EmojiGrid from './EmojiGrid.vue'

const props = defineProps<{
  modelValue: boolean
  systemEmojis: any[]
  userEmojis: any[]
}>()

const emit = defineEmits(['update:modelValue', 'select', 'upload', 'delete'])

const visible = ref(false)
const activeTab = ref('system')

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.emoji-container {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.emoji-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
</style>