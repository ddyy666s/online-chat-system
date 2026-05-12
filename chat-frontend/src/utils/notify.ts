import { ElMessage } from 'element-plus'

export const notify = {
  success(msg: string) { return ElMessage.success(msg) },
  warning(msg: string) { return ElMessage.warning(msg) },
  error(msg: string) { return ElMessage.error(msg) },
  info(msg: string) { return ElMessage.info(msg) }
}
