<template>
  <div class="communication-bar" :class="{ expanded: isExpanded }">
    <div class="toggle-btn" @click="toggleExpand">
      <el-icon :size="24">
        <component :is="isExpanded ? 'ArrowDown' : 'ArrowUp'" />
      </el-icon>
    </div>

    <Toolbar :is-expanded="isExpanded" @open-image-upload="openImageUpload" @start-record="startRecord"
      @stop-record="stopRecord" @cancel-record="cancelRecord" @start-voice-call="startVoiceCall"
      @start-video-call="startVideoCall" @open-emoji-picker="openEmojiPicker" />

    <RecordingTip :is-recording="isRecording" :record-duration="recordDuration" />

    <EmojiPicker v-model="showEmojiPicker" :system-emojis="systemEmojis" :user-emojis="userEmojis" @select="sendEmoji"
      @upload="uploadCustomEmoji" @delete="deleteEmoji" />

    <input type="file" ref="imageInput" accept="image/*" style="display: none" @change="handleImageUpload" />
    <input type="file" ref="emojiInput" accept="image/*" style="display: none" @change="handleEmojiUpload" />
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { uploadImageApi, uploadVoiceApi } from '@/api/message'
import { getSystemEmojisApi, getUserEmojisApi, uploadEmojiApi, deleteEmojiApi, type EmojiVO } from '@/api/emoji'
import Toolbar from '../communication/Toolbar.vue'
import RecordingTip from '../communication/RecordingTip.vue'
import EmojiPicker from '../communication/EmojiPicker.vue'

const props = defineProps<{ currentChatUserId?: number }>()
const emit = defineEmits(['sendImage', 'sendVoice', 'sendEmoji', 'startVoiceCall', 'startVideoCall'])

// UI 状态
const isExpanded = ref(false)
const isRecording = ref(false)
const recordDuration = ref(0)
const showEmojiPicker = ref(false)

// 表情数据
const systemEmojis = ref<EmojiVO[]>([])
const userEmojis = ref<EmojiVO[]>([])

// 上传引用
const imageInput = ref<HTMLInputElement>()
const emojiInput = ref<HTMLInputElement>()

// 录音相关
let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []
let recordingTimer: number | null = null
let startTime: number = 0
let mediaStream: MediaStream | null = null

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
const getSupportedMimeType = () => {
  const types = ['audio/webm;codecs=opus', 'audio/webm', 'audio/ogg;codecs=opus', 'audio/mp4', 'audio/mpeg']
  return types.find(t => MediaRecorder.isTypeSupported(t)) || ''
}

const startRecord = async () => {
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    const mimeType = getSupportedMimeType()
    const options: any = mimeType ? { mimeType } : {}
    mediaRecorder = new MediaRecorder(mediaStream, options)
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
      const mimeType = getSupportedMimeType() || 'audio/webm'
      const ext = mimeType.includes('mp4') ? 'mp4' : mimeType.includes('ogg') ? 'ogg' : 'webm'
      const blob = new Blob(audioChunks, { type: mimeType })
      const file = new File([blob], `voice_${Date.now()}.${ext}`, { type: mimeType })
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
  if (props.currentChatUserId) emit('startVoiceCall', props.currentChatUserId)
  else ElMessage.warning('请先选择聊天对象')
}
const startVideoCall = () => {
  if (props.currentChatUserId) emit('startVideoCall', props.currentChatUserId)
  else ElMessage.warning('请先选择聊天对象')
}

// 表情
const openEmojiPicker = async () => {
  await loadEmojis()
  showEmojiPicker.value = true
}
const loadEmojis = async () => {
  try {
    systemEmojis.value = await getSystemEmojisApi()
    userEmojis.value = await getUserEmojisApi()
  } catch { console.error('加载表情失败') }
}

// 关键修复：发送表情时显示成功提示
const sendEmoji = (emoji: EmojiVO) => {
  console.log('发送表情:', emoji)
  emit('sendEmoji', emoji.url)
  ElMessage.success('表情已发送')
}

const uploadCustomEmoji = () => emojiInput.value?.click()
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
    await deleteEmojiApi(emojiId)
    userEmojis.value = await getUserEmojisApi()
    ElMessage.success('删除成功')
  } catch {
    ElMessage.error('删除失败')
  }
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
</style>