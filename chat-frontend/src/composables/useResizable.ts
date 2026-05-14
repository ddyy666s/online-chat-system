import { ref, onMounted } from 'vue'

export function useResizable(options: {
  minWidth?: number
  maxWidth?: number
  defaultWidth?: number
  storageKey?: string
} = {}) {
  const {
    minWidth = 280,
    maxWidth = 800,
    defaultWidth = 420,
    storageKey = 'sidebar-width'
  } = options

  const sidebarWidth = ref(defaultWidth)
  const isResizing = ref(false)

  const loadWidth = () => {
    const saved = localStorage.getItem(storageKey)
    if (saved) {
      const w = parseInt(saved, 10)
      if (w >= minWidth && w <= maxWidth) {
        sidebarWidth.value = w
      }
    }
  }

  const startResize = (e: MouseEvent) => {
    e.preventDefault()
    isResizing.value = true
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'

    const startX = e.clientX
    const startWidth = sidebarWidth.value

    const onMouseMove = (ev: MouseEvent) => {
      const diff = ev.clientX - startX
      const newWidth = Math.max(minWidth, Math.min(maxWidth, startWidth + diff))
      sidebarWidth.value = newWidth
    }

    const onMouseUp = () => {
      isResizing.value = false
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
      localStorage.setItem(storageKey, String(sidebarWidth.value))
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
    }

    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
  }

  onMounted(() => {
    loadWidth()
  })

  return {
    sidebarWidth,
    isResizing,
    startResize
  }
}
