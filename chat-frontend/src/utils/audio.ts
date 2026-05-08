let audioElement: HTMLAudioElement | null = null

export const playVoice = (url: string) => {
  if (audioElement) {
    audioElement.pause()
    audioElement = null
  }
  
  audioElement = new Audio(url)
  audioElement.play().catch(err => {
    console.error('播放失败', err)
  })
}

export const stopVoice = () => {
  if (audioElement) {
    audioElement.pause()
    audioElement = null
  }
}