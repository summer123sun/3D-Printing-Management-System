/**
 * 统一接口返回格式
 * @see 需求规格说明书 v2 §7.1
 */
export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

/**
 * 分页请求参数
 */
export interface PageQuery {
  page?: number
  size?: number
  keyword?: string
  [key: string]: unknown
}

/**
 * 分页返回数据
 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

/**
 * 业务错误码
 */
export const ErrorCode = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  BAD_REQUEST: 400,
  SERVER_ERROR: 500,
} as const

export type ErrorCodeType = (typeof ErrorCode)[keyof typeof ErrorCode]