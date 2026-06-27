/**
 * 系统日志 API（M6）
 */
import { del, get } from '@/utils/request'
import type { LogQuery, SystemLog } from '@/types/log'
import type { PageResult } from '@/types/api'

/** 日志列表 */
export const listLogs = (query: LogQuery) =>
  get<PageResult<SystemLog>>('/log', query as any)

/** 清理过期日志 */
export const cleanLogs = (keepDays = 90) =>
  del<number>(`/log/clean?keepDays=${keepDays}`)
