<template>
  <div class="profile-container">
    <div class="profile-card">
      <div class="profile-header">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2>个人资料</h2>
        <div></div>
      </div>

      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-wrapper" @click="triggerFileInput">
          <el-avatar :size="100" :src="form.avatar || ''" class="profile-avatar">
            {{ form.nickname?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="avatar-overlay">
            <el-icon>
              <Camera />
            </el-icon>
            <span>更换头像</span>
          </div>
        </div>
        <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/gif" style="display: none"
          @change="handleAvatarChange" />
      </div>

      <!-- 表单 -->
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="个性签名" prop="signature">
          <el-input v-model="form.signature" type="textarea" :rows="3" placeholder="请输入个性签名" maxlength="100"
            show-word-limit />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Camera } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { updateProfileApi, updateAvatarApi, type UserInfo } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)
const fileInput = ref<HTMLInputElement>()

const form = reactive<UserInfo>({
  id: 0,
  username: '',
  nickname: '',
  avatar: null,
  signature: null,
  role: 'user'
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

const goBack = () => {
  router.push('/')
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（限制 2MB）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  // 显示上传中状态
  const loadingMsg = ElMessage.info('正在上传...')

  try {
    const avatarUrl = await updateAvatarApi(file)
    console.log('上传成功，新头像URL:', avatarUrl)

    // 关闭 loading 提示
    loadingMsg.close()

    // 1. 更新当前页面的头像显示
    form.avatar = avatarUrl

    // 2. 更新 store 中的用户信息（关键！这会同步更新 Header 中的头像）
    if (userStore.userInfo) {
      const updatedUserInfo = {
        ...userStore.userInfo,
        avatar: avatarUrl
      }
      userStore.setUserInfo(updatedUserInfo)
    }

    ElMessage.success('头像更新成功')

    // 3. 可选：重新加载好友列表（刷新好友头像）
    // const friendStore = (await import('@/stores/friendStore')).useFriendStore()
    // friendStore.loadFriendList()

  } catch (error) {
    console.error(error)
    ElMessage.error('头像上传失败')
  }
}

const handleSave = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  saving.value = true
  try {
    const res = await updateProfileApi({
      nickname: form.nickname,
      signature: form.signature
    })
    // 更新 store 中的用户信息
    userStore.setUserInfo(res)
    ElMessage.success('保存成功')
    router.push('/')
  } catch (error) {
    console.error(error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  const userInfo = userStore.userInfo
  if (userInfo) {
    form.id = userInfo.id
    form.username = userInfo.username
    form.nickname = userInfo.nickname
    form.avatar = userInfo.avatar
    form.signature = userInfo.signature
    form.role = userInfo.role
  }
})
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.profile-card {
  width: 500px;
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.profile-header h2 {
  margin: 0;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  font-size: 40px;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.avatar-overlay .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
</style>