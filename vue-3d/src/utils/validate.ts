/**
 * 表单校验规则
 */

/** 学号：8-20 位数字 */
export const validateStudentId = (rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入学号'))
  } else if (!/^\d{8,20}$/.test(value)) {
    callback(new Error('学号格式不正确（8-20 位数字）'))
  } else {
    callback()
  }
}

/** 密码：6-20 位 */
export const validatePassword = (rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度 6-20 位'))
  } else {
    callback()
  }
}

/** 手机号 */
export const validatePhone = (rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (!value) {
    callback()  // 可选字段
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

/** 邮箱 */
export const validateEmail = (rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (!value) {
    callback()
  } else if (!/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

/** 文件大小校验（STL 默认 50MB，图片 5MB） */
export const validateFileSize = (maxSizeMB: number) => {
  return (file: File): boolean => {
    const isLt = file.size / 1024 / 1024 < maxSizeMB
    return isLt
  }
}

/** STL 文件类型校验 */
export const validateStlType = (file: File): boolean => {
  const name = file.name.toLowerCase()
  return name.endsWith('.stl')
}