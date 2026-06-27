/** 耗材流水 */
export interface MaterialLog {
  logId: number
  materialType: string
  color: string
  weightChange: number
  balance: number
  operationType: number
  operationTypeName?: string
  relatedTaskId?: string
  operatorId: string
  operatorName?: string
  remark?: string
  createTime?: string
}

/** 库存视图 */
export interface MaterialStock {
  materialType: string
  color: string
  currentStock: number
  lastUpdateTime?: string
}

/** 入库 DTO */
export interface MaterialInboundDTO {
  materialType: string
  color: string
  weightChange: number
  remark?: string
}

/** 耗材类型 */
export const MaterialTypes = ['PLA', 'PETG', 'TPU', 'ABS'] as const
export type MaterialType = typeof MaterialTypes[number]

export const MaterialTypeText: Record<MaterialType, string> = {
  PLA: 'PLA（聚乳酸）',
  PETG: 'PETG（聚酯）',
  TPU: 'TPU（柔性）',
  ABS: 'ABS（工程）',
}

/** 操作类型 */
export enum OperationType {
  INBOUND = 1,
  CONSUME = 2,
}

export const OperationTypeText: Record<OperationType, string> = {
  [OperationType.INBOUND]: '入库',
  [OperationType.CONSUME]: '消耗',
}

export const OperationTypeTagType: Record<OperationType, 'success' | 'warning'> = {
  [OperationType.INBOUND]: 'success',
  [OperationType.CONSUME]: 'warning',
}
