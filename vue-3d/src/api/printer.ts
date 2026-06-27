/**
 * 打印机 + 维护记录 API（M5）
 */
import { get, post, put, del } from '@/utils/request'
import type { Maintenance, Printer } from '@/types/printer'
import type { PageResult } from '@/types/api'

/** 打印机列表 */
export const listPrinters = (params: { page?: number; size?: number; status?: number; keyword?: string }) =>
  get<PageResult<Printer>>('/printer', params as any)

/** 可用打印机列表 */
export const availablePrinters = () =>
  get<Printer[]>('/printer/available')

/** 打印机详情 */
export const printerDetail = (id: string) =>
  get<Printer>(`/printer/${id}`)

/** 新增打印机（ADMIN） */
export const createPrinter = (printer: Partial<Printer>) =>
  post<void>('/printer', printer)

/** 修改打印机（ADMIN） */
export const updatePrinter = (id: string, printer: Partial<Printer>) =>
  put<void>(`/printer/${id}`, printer)

/** 删除打印机（ADMIN） */
export const deletePrinter = (id: string) =>
  del<void>(`/printer/${id}`)

/** 设置状态（STAFF+） */
export const setPrinterStatus = (id: string, status: number, remark?: string) =>
  put<void>(`/printer/${id}/status`, undefined, { params: { status, remark } } as any)

/** 维护记录列表 */
export const listMaintenance = (params: { page?: number; size?: number; printerId?: string }) =>
  get<PageResult<Maintenance>>('/printer/maintenance', params as any)

/** 新增维护记录 */
export const addMaintenance = (dto: Partial<Maintenance>) =>
  post<void>('/printer/maintenance', dto)

/** 删除维护记录 */
export const deleteMaintenance = (id: number) =>
  del<void>(`/printer/maintenance/${id}`)
