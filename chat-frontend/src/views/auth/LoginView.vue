<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">在线聊天系统</h1>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" size="large" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" size="large"
            show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
            登录
          </el-button>
        </el-form-item>

        <div class="register-link">
          <span>还没有账号？</span>
          <el-link type="primary" @click="goToRegister">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
/** 登录页面组件 @component */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { loginApi } from '@/api/user'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()
/** 表单引用 */
const formRef = ref()
/** 登录按钮加载状态 */
const loading = ref(false)

/** 登录表单数据 */
const form = reactive({
  username: '',
  password: ''
})

/** 表单校验规则 */
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/** 执行登录 @returns Promise<void> */
const handleLogin = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    const res = await loginApi(form)
    console.log('登录响应:', res)

    userStore.setToken(res.token)
    userStore.setUserInfo(res.user)
    ElMessage.success('登录成功')

    await router.push('/')
  } catch (error: any) {
    console.error('登录失败:', error)
    ElMessage.error(error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

/** 跳转到注册页 @returns void */
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.register-link {
  text-align: center;
  margin-top: 16px;
}
</style>
