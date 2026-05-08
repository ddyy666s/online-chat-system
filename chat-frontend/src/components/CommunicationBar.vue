<template>
  <div class="communication-bar" :class="{ expanded: isExpanded }">
    <div class="toggle-btn" @click="toggleExpand">
      <el-icon :size="24">
        <component :is="isExpanded ? 'ArrowDown' : 'ArrowUp'" />
      </el-icon>
    </div>

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

    <!-- 录音提示 -->
    <div v-if="isRecording" class="recording-tip">
      <el-icon>
        <Microphone />
      </el-icon>
      <span>正在录音... {{ formatDuration(recordDuration) }} 松手发送</span>
      <div class="recording-wave"><span v-for="i in 5" :key="i"></span></div>
    </div>

    <!-- 表情选择器 -->
    <el-drawer v-model="showEmojiPicker" title="表情包" direction="btt" size="400px">
      <div class="emoji-container">
        <div class="emoji-tabs">
          <el-button size="small" :type="emojiTab === 'system' ? 'primary' : 'default'"
            @click="emojiTab = 'system'">系统表情</el-button>
          <el-button size="small" :type="emojiTab === 'user' ? 'primary' : 'default'"
            @click="emojiTab = 'user'">我的表情</el-button>
          <el-button size="small" @click="uploadCustomEmoji">上传表情</el-button>
        </div>
        <div class="emoji-grid" v-if="emojiTab === 'system'">
          <div v-for="emoji in systemEmojis" :key="emoji.id" class="emoji-item" @click="sendEmoji(emoji)">
            <img :src="emoji.url" /><span>{{ emoji.name }}</span>
          </div>
          <div v-if="systemEmojis.length === 0" class="empty-emoji">暂无系统表情</div>
        </div>
        <div class="emoji-grid" v-if="emojiTab === 'user'">
          <div v-for="emoji in userEmojis" :key="emoji.id" class="emoji-item" @click="sendEmoji(emoji)">
            <img :src="emoji.url" /><span>{{ emoji.name }}</span>
            <el-button class="delete-btn" size="small" text @click.stop="deleteEmoji(emoji.id)">删除</el-button>
          </div>
          <div v-if="userEmojis.length === 0" class="empty-emoji">暂无自定义表情</div>
        </div>
      </div>
    </el-drawer>

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
}>()

const emit = defineEmits(['sendImage', 'sendVoice', 'sendEmoji', 'startVoiceCall', 'startVideoCall'])

const isExpanded = ref(false)
const isRecording = ref(false)
const recordDuration = ref(0)
const showEmojiPicker = ref(false)
const emojiTab = ref('system')
const systemEmojis = ref<EmojiVO[]>([])
const userEmojis = ref<EmojiVO[]>([])
const imageInput = ref<HTMLInputElement>()
const emojiInput = ref<HTMLInputElement>()

let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []
let recordingTimer: number | null = null
let startTime: number = 0
let mediaStream: MediaStream | null = null

const formatDuration = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const toggleExpand = () => { isExpanded.value = !isExpanded.value }

// 图片
const openImageUpload = () => { imageInput.value?.click() }
const handleImageUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) return ElMessage.error('请选择图片文件')
  try {
    const url = await uploadImageApi(file)
    emit('sendImage', url)
    ElMessage.success('图片已发送')
  } catch { ElMessage.error('发送失败') }
  input.value = ''
}

// 录音
const startRecord = async () => {
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(mediaStream)
    audioChunks = []
    startTime = Date.now()
    recordDuration.value = 0

    recordingTimer = setInterval(() => {
      recordDuration.value = Math.floor((Date.now() - startTime) / 1000)
      if (recordDuration.value >= 60) stopRecord()
    }, 200) as unknown as number

    mediaRecorder.ondataavailable = (e) => { audioChunks.push(e.data) }
    mediaRecorder.onstop = async () => {
      const duration = Math.floor((Date.now() - startTime) / 1000)
      if (duration < 1) {
        ElMessage.warning('录音时间太短')
        mediaStream?.getTracks().forEach(t => t.stop())
        mediaStream = null
        return
      }
      const blob = new Blob(audioChunks, { type: 'audio/webm' })
      const file = new File([blob], `voice_${Date.now()}.webm`, { type: 'audio/webm' })
      try {
        const url = await uploadVoiceApi(file)
        emit('sendVoice', url, duration)
        ElMessage.success('语音已发送')
      } catch { ElMessage.error('发送失败') }
      mediaStream?.getTracks().forEach(t => t.stop())
      mediaStream = null
    }
    mediaRecorder.start(100)
    isRecording.value = true
  } catch { ElMessage.error('无法获取麦克风权限') }
}

const stopRecord = () => {
  if (mediaRecorder && isRecording.value && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    isRecording.value = false
    if (recordingTimer) clearInterval(recordingTimer)
  }
}

const cancelRecord = () => {
  if (mediaRecorder && isRecording.value && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
    isRecording.value = false
    if (recordingTimer) clearInterval(recordingTimer)
    ElMessage.info('已取消录音')
    mediaStream?.getTracks().forEach(t => t.stop())
    mediaStream = null
  }
}

// 通话
const startVoiceCall = () => {
  if (props.currentChatUserId) {
    emit('startVoiceCall', props.currentChatUserId)
  } else {
    ElMessage.warning('请先选择聊天对象')
  }
}

const startVideoCall = () => {
  if (props.currentChatUserId) {
    emit('startVideoCall', props.currentChatUserId)
  } else {
    ElMessage.warning('请先选择聊天对象')
  }
}

// 表情
const openEmojiPicker = () => { loadEmojis(); showEmojiPicker.value = true }
const loadEmojis = async () => {
  try {
    systemEmojis.value = await getSystemEmojisApi()
    userEmojis.value = await getUserEmojisApi()
  } catch { console.error('加载表情失败') }
}
const sendEmoji = (emoji: EmojiVO) => { emit('sendEmoji', emoji.url); showEmojiPicker.value = false }
const uploadCustomEmoji = () => { emojiInput.value?.click() }
const handleEmojiUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) return ElMessage.error('请选择图片文件')
  const { value: name } = await ElMessageBox.prompt('请输入表情名称', '上传表情', {
    inputPattern: /^[\u4e00-\u9fa5a-zA-Z0-9]{1,20}$/,
    inputErrorMessage: '请输入1-20个字符'
  })
  if (!name) return
  try {
    const newEmoji = await uploadEmojiApi(file, name)
    userEmojis.value.unshift(newEmoji)
    ElMessage.success('表情上传成功')
  } catch { ElMessage.error('上传失败') }
  input.value = ''
}
const deleteEmoji = async (emojiId: number) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示')
    await deleteEmojiApi(emojiId)
    userEmojis.value = userEmojis.value.filter(e => e.id !== emojiId)
    ElMessage.success('删除成功')
  } catch { }
}

onUnmounted(() => {
  if (mediaRecorder && isRecording.value) mediaRecorder.stop()
  if (mediaStream) mediaStream.getTracks().forEach(t => t.stop())
})
</script>

<style scoped>
.communication-bar {
  border-top: 1px solid #e4e7ed;
  background: #fff;
}

.toggle-btn {
  display: flex;
  justify-content: center;
  padding: 8px 0;
  cursor: pointer;
  color: #909399;
}

.toggle-btn:hover {
  color: #409eff;
}

.toolbar {
  display: flex;
  justify-content: space-around;
  padding: 12px 16px;
}

.tool-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
  padding: 6px 12px;
  border-radius: 8px;
}

.tool-item:hover {
  color: #409eff;
  background: #f0f7ff;
}

.icon-text {
  font-size: 20px;
}

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
  gap: 16px;
  z-index: 1000;
}

.recording-wave {
  display: flex;
  gap: 4px;
}

.recording-wave span {
  width: 4px;
  height: 15px;
  background: #ff4444;
  animation: wave 0.5s infinite;
}

@keyframes wave {

  0%,
  100% {
    height: 15px;
  }

  50% {
    height: 35px;
  }
}

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

.empty-emoji {
  text-align: center;
  padding: 40px;
  color: #909399;
  grid-column: 1/-1;
}
</style>