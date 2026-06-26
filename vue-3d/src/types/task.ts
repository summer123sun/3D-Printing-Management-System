/**
 * 打印任务类型（**B** 核心模块）
 * @see 需求规格说明书 v2 §3.3
 */

/** 任务状态枚举 */
export enum TaskStatus {
  PENDING = 0,    // 待审批
  APPROVED = 1,   // 已通过
  REJECTED = 2,   // 已驳回
  QUEUED = 3,     // 排队中
  PRINTING = 4,   // 打印中
  DONE = 5,       // 已完成
  CANCELLED = 6,  // 已取消
}

/** 任务状态文本 */
export const TaskStatusText: Record<TaskStatus, string> = {
  [TaskStatus.PENDING]: '待审批',
  [TaskStatus.APPROVED]: '已通过',
  [TaskStatus.REJECTED]: '已驳回',
  [TaskStatus.QUEUED]: '排队中',
  [TaskStatus.PRINTING]: '打印中',
  [TaskStatus.DONE]: '已完成',
  [TaskStatus.CANCELLED]: '已取消',
}

/** 任务状态标签类型（用于 el-tag type） */
export const TaskStatusTagType: Record<TaskStatus, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
  [TaskStatus.PENDING]: 'info',
  [TaskStatus.APPROVED]: 'success',
  [TaskStatus.REJECTED]: 'danger',
  [TaskStatus.QUEUED]: 'warning',
  [TaskStatus.PRINTING]: 'primary',
  [TaskStatus.DONE]: 'success',
  [TaskStatus.CANCELLED]: 'info',
}

/** 任务优先级 */
export enum Priority {
  URGENT = 1,   // 紧急
  NORMAL = 2,   // 普通
  LOW = 3,      // 低优
}

/** 优先级文本 */
export const PriorityText: Record<Priority, string> = {
  [Priority.URGENT]: '紧急',
  [Priority.NORMAL]: '普通',
  [Priority.LOW]: '低优',
}

/** 材料类型 */
export const MaterialType = ['PLA', 'PETG', 'TPU', 'ABS'] as const
export type MaterialType = (typeof MaterialType)[number]

/** 颜色枚举（常用，可扩展） */
export const MaterialColor = [
  '白色', '黑色', '红色', '蓝色', '绿色',
  '黄色', '金色', '银色', '透明', '灰色',
] as const
export type MaterialColor = (typeof MaterialColor)[number]

/** 任务完整信息 */
export interface PrintTask {
  taskId: string
  applicantId: string
  applicantName?: string
  approverId?: string
  approverName?: string
  printerId?: string
  printerModel?: string
  projectId?: number
  title: string
  modelName: string
  stlFilePath: string
  materialType: MaterialType
  color?: MaterialColor
  layerHeight: number
  infillRate: number
  /** 是否需要支撑：0否 1是（与数据库字段对应） */
  needSupport: number
  priority: Priority
  estWeight?: number
  estTime?: number
  applyTime: string
  approveTime?: string
  approveComment?: string
  status: TaskStatus
  actualWeight?: number
  actualTime?: number
  finishTime?: string
  qualityScore?: number
  pickupTime?: string
  createTime?: string
  updateTime?: string
}

/** 提交任务请求 */
export interface TaskApplyDTO {
  title: string
  modelName: string
  stlFilePath: string
  materialType: MaterialType
  color?: MaterialColor
  layerHeight: number
  infillRate: number
  /** 是否需要支撑：0否 1是（与数据库字段对应） */
  needSupport: number
  priority: Priority
  estWeight?: number
  estTime?: number
  projectId?: number
}

/** 审批请求 */
export interface ApproveDTO {
  approveComment?: string
}

/** 驳回请求 */
export interface RejectDTO {
  approveComment: string  // 驳回原因必填
}

/** 分配打印机请求 */
export interface AssignPrinterDTO {
  printerId: string
}

/** 完成打印请求 */
export interface FinishPrintDTO {
  actualWeight: number
  actualTime: number
  qualityScore?: number
}

/** 取件签到请求 */
export interface PickupDTO {
  qualityScore?: number
}

/** 任务查询参数 */
export interface TaskQuery {
  page?: number
  size?: number
  /** 任务状态（可选，按状态筛选） */
  status?: TaskStatus | number
  /** 打印机编号 */
  printerId?: string
  /** 申请人学号 */
  applicantId?: string
  /** 关键字 */
  keyword?: string
}