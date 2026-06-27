/** 打印机 */
export interface Printer {
  printerId: string
  model: string
  brand?: string
  purchaseDate?: string
  status: number
  statusName?: string
  totalPrintHours: number
  location?: string
  nozzleSize?: number
  buildVolume?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 维护记录 */
export interface Maintenance {
  maintId: number
  printerId: string
  maintType: number
  maintTypeName?: string
  content: string
  maintainerId: string
  maintainerName?: string
  maintTime: string
  nextMaintDate?: string
  createTime?: string
}

/** 打印机状态枚举 */
export enum PrinterStatus {
  NORMAL = 1,
  MAINTENANCE = 2,
  SCRAPPED = 3,
}

export const PrinterStatusText: Record<PrinterStatus, string> = {
  [PrinterStatus.NORMAL]: '正常',
  [PrinterStatus.MAINTENANCE]: '维修中',
  [PrinterStatus.SCRAPPED]: '报废',
}

export const PrinterStatusTagType: Record<PrinterStatus, 'success' | 'warning' | 'info'> = {
  [PrinterStatus.NORMAL]: 'success',
  [PrinterStatus.MAINTENANCE]: 'warning',
  [PrinterStatus.SCRAPPED]: 'info',
}

/** 维护类型 */
export enum MaintType {
  ROUTINE = 1,
  REPAIR = 2,
  REPLACE = 3,
  CALIBRATE = 4,
}

export const MaintTypeText: Record<MaintType, string> = {
  [MaintType.ROUTINE]: '保养',
  [MaintType.REPAIR]: '维修',
  [MaintType.REPLACE]: '换件',
  [MaintType.CALIBRATE]: '校准',
}

export const MaintTypeTagType: Record<MaintType, 'primary' | 'danger' | 'warning' | 'info'> = {
  [MaintType.ROUTINE]: 'primary',
  [MaintType.REPAIR]: 'danger',
  [MaintType.REPLACE]: 'warning',
  [MaintType.CALIBRATE]: 'info',
}
