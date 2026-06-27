/**
 * 统计 API（M6）
 */
import { get } from '@/utils/request'
import type { DashboardData, MaterialStats, MemberRank, PrinterStats, TaskStats } from '@/types/stats'

/** Dashboard 总览 */
export const dashboard = () => get<DashboardData>('/stats/dashboard')

/** 任务统计 */
export const taskStats = (params?: { startDate?: string; endDate?: string }) =>
  get<TaskStats>('/stats/task', params as any)

/** 材料消耗 */
export const materialStats = () => get<MaterialStats>('/stats/material')

/** 打印机使用率 */
export const printerStats = () => get<PrinterStats>('/stats/printer')

/** 活跃社员排行 */
export const memberRanking = (limit = 10) =>
  get<MemberRank[]>('/stats/member', { limit } as any)
