// 下载聊天记录
export const downloadChatRecord = async (friendId: number, friendName: string) => {
  const token = localStorage.getItem('chat_token')
  try {
    const response = await fetch(`/api/message/download/${friendId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `chat_${friendName}_${Date.now()}.txt`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载失败', error)
  }
}

// 导出JSON
export const downloadJson = (data: any, filename: string) => {
  const jsonStr = JSON.stringify(data, null, 2)
  const blob = new Blob([jsonStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}