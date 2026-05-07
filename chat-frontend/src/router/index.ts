import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useUserStore } from '@/stores/userStore'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth !== false
  
  console.log('路由守卫:', { path: to.path, requiresAuth, isLoggedIn: userStore.isLoggedIn() })
  
  if (requiresAuth && !userStore.isLoggedIn()) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }
  
  if (to.meta.requiresAdmin && !userStore.isAdmin()) {
    ElMessage.error('无权限访问')
    next('/')
    return
  }
  
  document.title = `${to.meta.title || '在线聊天系统'}`
  next()
})

export default router