/** 统计类型定义（M6） */

/** Dashboard 总览 */
export interface DashboardData {
  totalTasks: number
  doneTasks: number
  pendingTasks: number
  completionRate: number
  totalPrintHours: number
  totalWeight: number
  totalArtworks: number
  recommendedArtworks: number
  totalPrinters: number
  normalPrinters: number
  trend7d: Array<{ date: string; count: number }>
}

/** 任务统计 */
export interface TaskStats {
  byStatus: Array<{ status: number; count: number }>
  byMaterial: Array<{ material_type: string; count: number }>
  monthly: Array<{ month: string; count: number }>
}

/** 材料统计 */
export interface MaterialStats {
  byMaterial: Array<{ material_type: string; total_used: number; log_count: number }>
  currentStock: any[]
}

/** 打印机统计 */
export interface PrinterStats {
  printers: any[]
  totalHours: number
  count: number
  normalCount: number
  maintCount: number
  scrappedCount: number
}

/** 活跃社员 */
export interface MemberRank {
  applicant_id: string
  done_count: number
  total_hours: number
  total_weight: number
}
