/**
 * 打印任务 API（E - 前端 B 也是你）
 *
 * 调用示例：
 *   import { applyTask, myTasks, approveTask, finishPrint } from '@/api/task'
 */
import { get, post, put } from '@/utils/request'
import type {
  PrintTask,
  TaskApplyDTO,
  ApproveDTO,
  RejectDTO,
  AssignPrinterDTO,
  FinishPrintDTO,
  PickupDTO,
  TaskQuery,
} from '@/types/task'
import type { PageResult } from '@/types/api'

/** 提交打印任务 */
export const applyTask = (dto: TaskApplyDTO) => {
  return post<string>('/task', dto)
}

/** 我的任务列表（分页） */
export const myTasks = (query: TaskQuery) => {
  return get<PageResult<PrintTask>>('/task/my', query as Record<string, unknown>)
}

/** 待审批任务（技术骨干） */
export const pendingTasks = (query: TaskQuery) => {
  return get<PageResult<PrintTask>>('/task/pending', query as Record<string, unknown>)
}

/** 打印队列 */
export const taskQueue = (query: TaskQuery) => {
  return get<PageResult<PrintTask>>('/task/queue', query as Record<string, unknown>)
}

/** 任务详情 */
export const taskDetail = (id: string) => {
  return get<PrintTask>(`/task/${id}`)
}

/** 审批通过 */
export const approveTask = (id: string, dto: ApproveDTO = {}) => {
  return put<void>(`/task/${id}/approve`, dto)
}

/** 审批驳回 */
export const rejectTask = (id: string, dto: RejectDTO) => {
  return put<void>(`/task/${id}/reject`, dto)
}

/** 分配打印机 */
export const assignPrinter = (id: string, dto: AssignPrinterDTO) => {
  return put<void>(`/task/${id}/assign`, dto)
}

/** 开始打印 */
export const startPrint = (id: string) => {
  return put<void>(`/task/${id}/start`)
}

/** 完成打印 */
export const finishPrint = (id: string, dto: FinishPrintDTO) => {
  return put<void>(`/task/${id}/finish`, dto)
}

/** 取件签到 */
export const pickup = (id: string, dto: PickupDTO = {}) => {
  return put<void>(`/task/${id}/pickup`, dto)
}

/** 取消任务 */
export const cancelTask = (id: string) => {
  return put<void>(`/task/${id}/cancel`)
}

/** 任务统计 */
export const taskStats = () => {
  return get<Record<string, number>>('/task/stats')
}