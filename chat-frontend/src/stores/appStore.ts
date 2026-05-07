import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏是否折叠
  const sidebarCollapsed = ref(false)
  
  // 当前主题（dark/light）
  const theme = ref('light')
  
  // 加载状态
  const globalLoading = ref(false)

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 设置主题
  const setTheme = (newTheme: string) => {
    theme.value = newTheme
    localStorage.setItem('chat_theme', newTheme)
    document.documentElement.setAttribute('data-theme', newTheme)
  }

  // 初始化主题
  const initTheme = () => {
    const savedTheme = localStorage.getItem('chat_theme') || 'light'
    setTheme(savedTheme)
  }

  // 设置全局加载
  const setGlobalLoading = (loading: boolean) => {
    globalLoading.value = loading
  }

  return {
    sidebarCollapsed,
    theme,
    globalLoading,
    toggleSidebar,
    setTheme,
    initTheme,
    setGlobalLoading
  }
})