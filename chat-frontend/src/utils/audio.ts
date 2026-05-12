let audioElement: HTMLAudioElement | null = null

export const playVoice = (url: string) => {
  if (audioElement) { audioElement.pause(); audioElement = null }
  audioElement = new Audio(url)
  audioElement.play().catch(() => {})
}

export const stopVoice = () => {
  if (audioElement) { audioElement.pause(); audioElement = null }
}

let soundEnabled = true
export const setSoundEnabled = (v: boolean) => { soundEnabled = v }

let _noticeUrl: string | null = null
let _ringUrl: string | null = null

function getNoticeUrl(): string {
  if (_noticeUrl === null) {
    try {
      const u = new URL('../assets/audio/notice.MP3', import.meta.url)
      _noticeUrl = u.href
    } catch { _noticeUrl = '' }
  }
  return _noticeUrl
}

function getRingUrl(): string {
  if (_ringUrl === null) {
    try {
      const u = new URL('../assets/audio/ring.MP3', import.meta.url)
      _ringUrl = u.href
    } catch { _ringUrl = '' }
  }
  return _ringUrl
}

export const playNotificationSound = () => {
  if (!soundEnabled) return
  const url = getNoticeUrl()
  if (!url) return
  try {
    const a = new Audio(url)
    a.volume = 0.5
    a.play().catch(() => {})
  } catch { /* ignore */ }
}

export const playRingtone = () => {
  if (!soundEnabled) return () => {}
  const url = getRingUrl()
  if (!url) return () => {}
  try {
    const a = new Audio(url)
    a.loop = true
    a.volume = 0.8
    a.play().catch(() => {})
    return () => {
      a.pause()
      a.loop = false
    }
  } catch { return () => {} }
}
