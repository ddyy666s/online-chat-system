<template>
  <div class="loading-container" v-if="visible">
    <div class="loading-mask">
      <div class="loading-spinner">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <span class="loading-text">{{ text || '加载中...' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 全屏加载遮罩组件 @component */
import { Loading } from '@element-plus/icons-vue'

/** 组件属性：是否显示、加载文本 */
defineProps<{
  visible?: boolean
  text?: string
}>()
</script>

<style scoped>
.loading-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-spinner {
  background: white;
  padding: 20px 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #409eff;
}

.is-loading {
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}
</style>
