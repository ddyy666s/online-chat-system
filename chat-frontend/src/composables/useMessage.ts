import { ElMessage, ElMessageBox } from 'element-plus'

export const useMessage = () => {
  const success = (msg: string) => {
    ElMessage.success(msg)
  }

  const error = (msg: string) => {
    ElMessage.error(msg)
  }

  const warning = (msg: string) => {
    ElMessage.warning(msg)
  }

  const info = (msg: string) => {
    ElMessage.info(msg)
  }

  const confirm = (message: string, title?: string): Promise<void> => {
    return ElMessageBox.confirm(message, title || '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {})
  }

  const alert = (message: string, title?: string): Promise<void> => {
    return ElMessageBox.alert(message, title || '提示', {
      confirmButtonText: '确定'
    }).then(() => {})
  }

  const prompt = (message: string, defaultValue?: string): Promise<string> => {
    return ElMessageBox.prompt(message, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: defaultValue || ''
    }).then(({ value }) => value)
  }

  return {
    success,
    error,
    warning,
    info,
    confirm,
    alert,
    prompt
  }
}