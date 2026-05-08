<template>
  <div class="communication-bar" :class="{ expanded: isExpanded }">
    <!-- 收起时的按钮 -->
    <div class="toggle-btn" @click="toggleExpand">
      <el-icon :size="24">
        <component :is="isExpanded ? 'ArrowDown' : 'ArrowUp'" />
      </el-icon>
    </div>

    <!-- 展开后的工具栏 -->
    <div class="toolbar" v-show="isExpanded">
      <div class="tool-item" @click="openImageUpload">
        <el-icon :size="24">
          <Picture />
        </el-icon>
        <span>图片</span>
      </div>

      <div class="tool-item" @mousedown="startRecord" @mouseup="stopRecord" @mouseleave="cancelRecord">
        <el-icon :size="24">
          <Microphone />
        </el-icon>
        <span>语音</span>
      </div>

      <div class="tool-item" @click="startVoiceCall">
        <el-icon :size="24">
          <Phone />
        </el-icon>
        <span>语音通话</span>
      </div>

      <div class="tool-item" @click="startVideoCall">
        <el-icon :size="24">
          <VideoCamera />
        </el-icon>
        <span>视频通话</span>
      </div>

      <div class="tool-item" @click="openEmojiPicker">
        <div class="icon-text">😊</div>
        <span>表情</span>
      </div>
    </div>

    <!-- 录音中提示 -->
    <div v-if="isRecording" class="recording-tip">
      <el-icon>
        <Microphone />
      </el-icon>
      <span>正在录音... 松手发送</span>
      <div class="recording-wave">
        <span v-for="i in 5" :key="i" :style="{ height: Math.random() * 20 + 10 + 'px' }"></span>
      </div>
    </div>

    <!-- 表情选择器 -->
    <el-drawer v-model="showEmojiPicker" title="表情包" direction="btt" size="400px">
      <div class="emoji-container">
        <div class="emoji-tabs">
          <el-button :type="emojiTab === 'system' ? 'primary' : 'default'" size="small"
            @click="emojiTab = 'system'">系统表情</el-button>
          <el-button :type="emojiTab === 'user' ? 'primary' : 'default'" size="small"
            @click="emojiTab = 'user'">我的表情</el-button>
          <el-button size="small" @click="uploadCustomEmoji">上传表情</el-button>
        </div>

        <div class="emoji-grid" v-if="emojiTab === 'system'">
          <div v-for="emoji in systemEmojis" :key="emoji.id" class="emoji-item" @click="sendEmoji(emoji)">
            <img :src="emoji.url" :alt="emoji.name" />
            <span>{{ emoji.name }}</span>
          </div>
          <div v-if="systemEmojis.length === 0" class="empty-emoji">暂无系统表情</div>
        </div>

        <div class="emoji-grid" v-if="emojiTab === 'user'">
          <div v-for="emoji in userEmojis" :key="emoji.id" class="emoji-item" @click="sendEmoji(emoji)">
            <img :src="emoji.url" :alt="emoji.name" />
            <span>{{ emoji.name }}</span>
            <el-button class="delete-btn" size="small" text @click.stop="deleteEmoji(emoji.id)">删除</el-button>
          </div>
          <div v-if="userEmojis.length === 0" class="empty-emoji">暂无自定义表情，点击上传添加</div>
        </div>
      </div>
    </el-drawer>

    <!-- 隐藏的文件上传 -->
    <input type="file" ref="imageInput" accept="image/*" style="display: none" @change="handleImageUpload" />
    <input type="file" ref="emojiInput" accept="image/*" style="display: none" @change="handleEmojiUpload" />
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowUp, ArrowDown, Picture, Microphone, Phone, VideoCamera } from '@element-plus/icons-vue'
import { uploadImageApi, uploadVoiceApi } from '@/api/message'
import { getSystemEmojisApi, getUserEmojisApi, uploadEmojiApi, deleteEmojiApi, type EmojiVO } from '@/api/emoji'

const props = defineProps<{
  currentChatUserId?: number
  currentGroupId?: number
}>()

const emit = defineEmits(['send', 'sendImage', 'sendVoice', 'sendEmoji', 'startVoiceCall', 'startVideoCall'])

// UI 状态
const isExpanded = ref(false)
const isRecording = ref(false)
const showEmojiPicker = ref(false)
const emojiTab = ref('system')
const systemEmojis = ref<EmojiVO[]>([])
const userEmojis = ref<EmojiVO[]>([])

// 上传相关
const imageInput = ref<HTMLInputElement>()
const emojiInput = ref<HTMLInputElement>()

// 录音相关
let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []
let recordingTimer: number | null = null

// 切换工具栏展开/收起
const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

// 打开图片选择器
const openImageUpload = () => {
  imageInput.value?.click()
}

// 处理图片上传
const handleImageUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  try {
    const url = await uploadImageApi(file)
    emit('sendImage', url)
    ElMessage.success('图片已发送')
  } catch (error) {
    console.error(error)
    ElMessage.error('发送失败')
  }

  input.value = ''
}

// 开始录音
const startRecord = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data)
    }

    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' })
      const file = new File([audioBlob], `voice_${Date.now()}.webm`, { type: 'audio/webm' })

      try {
        const url = await uploadVoiceApi(file)
        emit('sendVoice', url)
        ElMessage.success('语音已发送')
      } catch (error) {
        console.error(error)
        ElMessage.error('发送失败')
      }

      stream.getTracks().forEach(track => track.stop())
    }

    mediaRecorder.start()
    isRecording.value = true

    // 60秒自动停止
    recordingTimer = setTimeout(() => {
      if (isRecording.value) {
        stopRecord()
      }
    }, 60000) as unknown as number
  } catch (error) {
    console.error(error)
    ElMessage.error('无法获取麦克风权限，请检查浏览器设置')
  }
}

// 停止录音并发送
const stopRecord = () => {
  if (mediaRecorder && isRecording.value && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    isRecording.value = false
    if (recordingTimer) {
      clearTimeout(recordingTimer)
      recordingTimer = null
    }
  }
}

// 取消录音
const cancelRecord = () => {
  if (mediaRecorder && isRecording.value && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    isRecording.value = false
    if (recordingTimer) {
      clearTimeout(recordingTimer)
      recordingTimer = null
    }
    ElMessage.info('已取消录音')
  }
}

// 开始语音通话
const startVoiceCall = () => {
  emit('startVoiceCall')
}

// 开始视频通话
const startVideoCall = () => {
  emit('startVideoCall')
}

// 打开表情选择器
const openEmojiPicker = () => {
  loadEmojis()
  showEmojiPicker.value = true
}

// 加载表情包
const loadEmojis = async () => {
  try {
    const [system, user] = await Promise.all([
      getSystemEmojisApi(),
      getUserEmojisApi()
    ])
    systemEmojis.value = system
    userEmojis.value = user
  } catch (error) {
    console.error('加载表情失败', error)
  }
}

// 发送表情
const sendEmoji = (emoji: EmojiVO) => {
  emit('sendEmoji', emoji.url)
  showEmojiPicker.value = false
}

// 上传自定义表情
const uploadCustomEmoji = () => {
  emojiInput.value?.click()
}

// 处理表情上传
const handleEmojiUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }

  const { value: name } = await ElMessageBox.prompt('请输入表情名称', '上传表情', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^[\u4e00-\u9fa5a-zA-Z0-9]{1,20}$/,
    inputErrorMessage: '请输入1-20个字符'
  })

  if (!name) return

  try {
    const newEmoji = await uploadEmojiApi(file, name)
    userEmojis.value.unshift(newEmoji)
    ElMessage.success('表情上传成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('上传失败')
  }

  input.value = ''
}

// 删除自定义表情
const deleteEmoji = async (emojiId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个表情吗？', '提示', { type: 'warning' })
    await deleteEmojiApi(emojiId)
    userEmojis.value = userEmojis.value.filter(e => e.id !== emojiId)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onUnmounted(() => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
  }
})
</script>

<style scoped>
.communication-bar {
  position: relative;
  border-top: 1px solid #e4e7ed;
  background: #fff;
  transition: all 0.3s;
}

.toggle-btn {
  display: flex;
  justify-content: center;
  padding: 8px 0;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
}

.toggle-btn:hover {
  color: #409eff;
  background: #f5f5f5;
}

.toolbar {
  display: flex;
  justify-content: space-around;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
  transition: all 0.2s;
  padding: 6px 12px;
  border-radius: 8px;
}

.tool-item:hover {
  color: #409eff;
  background: #f0f7ff;
  transform: translateY(-2px);
}

.tool-item span {
  font-size: 12px;
}

.icon-text {
  font-size: 20px;
  line-height: 1;
}

/* 录音提示 */
.recording-tip {
  position: fixed;
  bottom: 150px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.85);
  color: white;
  padding: 16px 24px;
  border-radius: 40px;
  display: flex;
  align-items: center;
  gap: 16px;
  z-index: 1000;
  font-size: 14px;
}

.recording-wave {
  display: flex;
  align-items: center;
  gap: 4px;
}

.recording-wave span {
  width: 4px;
  background: #ff4444;
  border-radius: 2px;
  animation: wave 0.5s ease-in-out infinite;
}

@keyframes wave {

  0%,
  100% {
    height: 10px;
  }

  50% {
    height: 30px;
  }
}

/* 表情选择器 */
.emoji-container {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.emoji-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

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
  transition: all 0.2s;
  position: relative;
  border: 1px solid transparent;
}

.emoji-item:hover {
  background: #f5f5f5;
  border-color: #e4e7ed;
}

.emoji-item img {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.emoji-item span {
  font-size: 11px;
  color: #606266;
}

.delete-btn {
  position: absolute;
  top: 0;
  right: 0;
  padding: 2px 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.emoji-item:hover .delete-btn {
  opacity: 1;
}

.empty-emoji {
  text-align: center;
  padding: 40px;
  color: #909399;
  grid-column: 1 / -1;
}
</style>