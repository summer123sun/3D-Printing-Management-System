/**
 * 耗材 API（M5）
 */
import { get, post } from '@/utils/request'
import type { MaterialInboundDTO, MaterialLog, MaterialStock } from '@/types/material'
import type { PageResult } from '@/types/api'

/** 库存列表 */
export const listStocks = (materialType?: string) =>
  get<MaterialStock[]>('/material', materialType ? { materialType } : undefined as any)

/** 库存预警 */
export const warningStocks = (threshold?: number) =>
  get<MaterialStock[]>('/material/warning', threshold ? { threshold } : undefined as any)

/** 耗材流水 */
export const listMaterialLogs = (params: {
  page?: number
  size?: number
  materialType?: string
  color?: string
  operationType?: number
  operatorId?: string
}) => get<PageResult<MaterialLog>>('/material/log', params as any)

/** 入库 */
export const inboundMaterial = (dto: MaterialInboundDTO) =>
  post<void>('/material/inbound', dto)

/** 总览 */
export const materialSummary = () =>
  get<object>('/material/summary')
