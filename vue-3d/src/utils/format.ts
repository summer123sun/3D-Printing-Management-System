/**
 * 格式化工具函数
 */

/** 格式化日期时间（默认 YYYY-MM-DD HH:mm） */
export const formatDate = (date: string | Date | null | undefined, pattern = 'YYYY-MM-DD HH:mm'): string => {
  if (!date) return '-'
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return '-'

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')

  return pattern
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/** 相对时间（如 "3 分钟前"） */
export const formatRelativeTime = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date
  const now = Date.now()
  const diff = Math.floor((now - d.getTime()) / 1000)

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 86400 * 30) return `${Math.floor(diff / 86400)} 天前`
  return formatDate(d, 'YYYY-MM-DD')
}

/** 格式化金额（元，保留 2 位） */
export const formatMoney = (amount: number | string | null | undefined): string => {
  if (amount === null || amount === undefined || amount === '') return '-'
  const n = Number(amount)
  if (isNaN(n)) return '-'
  return `¥${n.toFixed(2)}`
}

/** 格式化重量（g） */
export const formatWeight = (g: number | null | undefined): string => {
  if (g === null || g === undefined) return '-'
  if (g >= 1000) return `${(g / 1000).toFixed(2)} kg`
  return `${g.toFixed(2)} g`
}

/** 格式化文件大小 */
export const formatFileSize = (bytes: number | null | undefined): string => {
  if (!bytes && bytes !== 0) return '-'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`
}

/** 格式化耗时（分钟 → "X小时Y分钟"） */
export const formatDuration = (minutes: number | null | undefined): string => {
  if (!minutes && minutes !== 0) return '-'
  if (minutes < 60) return `${minutes} 分钟`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m === 0 ? `${h} 小时` : `${h} 小时 ${m} 分钟`
}