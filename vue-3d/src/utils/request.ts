/**
 * Axios 封装 + JWT 拦截器 + 统一错误处理
 * @see 需求规格说明书 v2 §7.1 统一返回格式
 *
 * 错误处理策略：
 * 1. HTTP 层错误（网络/超时/5xx）→ ElNotification 顶部通知
 * 2. 业务层错误（code != 200）→ 默认 ElNotification，可通过 config.silent 静默
 * 3. 401 未登录 → 清 token + 跳登录页（除登录接口外）
 * 4. 离线（navigator.onLine === false）→ 友好提示
 */
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import { ErrorCode, type ApiResponse } from '@/types/api'
import { getToken, clearAuth } from './auth'

// 请求配置扩展（支持 silent 选项）
declare module 'axios' {
  export interface AxiosRequestConfig {
    /** 静默模式：业务错误不弹通知（调用方自己处理） */
    silent?: boolean
    /** 自定义超时（毫秒） */
    timeout?: number
  }
}

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
})

// ============================================
// 请求拦截器：自动带 JWT
// ============================================
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 错误码 → 友好消息映射
const ERROR_MSG_MAP: Record<number, string> = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '无权限访问此资源',
  404: '请求的资源不存在',
  408: '请求超时',
  500: '服务器开了小差，请稍后再试',
  502: '网关错误',
  503: '服务暂不可用',
  504: '网关超时',
}

function showError(title: string, message: string, type: 'error' | 'warning' = 'error') {
  ElNotification({
    title,
    message,
    type,
    duration: 2000,  // 业务错误通知 2 秒自动消失（用户习惯：及时消失，不打扰）
    showClose: true,
  })
}

// ============================================
// 响应拦截器：统一处理 code + 错误提示
// ============================================
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    const silent = (response.config as AxiosRequestConfig).silent === true

    // 后端约定：code === 200 表示成功
    if (res.code === ErrorCode.SUCCESS) {
      return res.data as unknown as AxiosResponse
    }

    // 401 未登录：清 token，跳登录
    if (res.code === ErrorCode.UNAUTHORIZED) {
      clearAuth()
      if (!silent) {
        showError('登录已过期', '请重新登录', 'warning')
      }
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.msg || '未登录'))
    }

    // 403 无权限
    if (res.code === ErrorCode.FORBIDDEN) {
      if (!silent) showError('无权限', res.msg || '您没有权限执行此操作', 'warning')
      return Promise.reject(new Error(res.msg))
    }

    // 其它业务错误（默认弹通知，silent 模式不弹）
    if (!silent) {
      showError('操作失败', res.msg || '请稍后再试')
    }
    return Promise.reject(new Error(res.msg))
  },
  (error) => {
    // HTTP 层面错误（网络/超时/5xx）
    const config = error.config as AxiosRequestConfig | undefined
    const silent = config?.silent === true

    // 离线检测
    if (typeof navigator !== 'undefined' && !navigator.onLine) {
      if (!silent) showError('网络已断开', '请检查网络连接后重试', 'warning')
      return Promise.reject(error)
    }

    let title = '请求失败'
    let msg = '网络错误，请稍后再试'

    if (error.response) {
      const status = error.response.status
      title = ERROR_MSG_MAP[status] || `请求失败 (${status})`
      // 后端业务错误格式 {code, msg, data}，尝试提取 msg
      const backendMsg = error.response.data?.msg || error.response.data?.message
      msg = backendMsg || msg
    } else if (error.code === 'ECONNABORTED') {
      title = '请求超时'
      msg = '网络连接超时，请检查后端服务是否启动'
    } else if (error.message?.includes('Network Error')) {
      title = '网络错误'
      msg = '无法连接到后端服务（http://localhost:8080）'
    }

    if (!silent) showError(title, msg)
    return Promise.reject(error)
  },
)

/**
 * 通用请求函数（返回 data）
 */
export const request = <T = unknown>(config: AxiosRequestConfig): Promise<T> => {
  return service.request(config) as unknown as Promise<T>
}

/**
 * GET 请求
 */
export const get = <T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: AxiosRequestConfig,
): Promise<T> => {
  return request<T>({ url, method: 'GET', params, ...config })
}

/**
 * POST 请求
 */
export const post = <T = unknown>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> => {
  return request<T>({ url, method: 'POST', data, ...config })
}

/**
 * PUT 请求
 */
export const put = <T = unknown>(
  url: string,
  data?: unknown,
  config?: AxiosRequestConfig,
): Promise<T> => {
  return request<T>({ url, method: 'PUT', data, ...config })
}

/**
 * DELETE 请求
 */
export const del = <T = unknown>(
  url: string,
  config?: AxiosRequestConfig,
): Promise<T> => {
  return request<T>({ url, method: 'DELETE', ...config })
}

/**
 * 上传文件（带进度回调）
 */
export const upload = <T = unknown>(
  url: string,
  formData: FormData,
  onProgress?: (percent: number) => void,
): Promise<T> => {
  return request<T>({
    url,
    method: 'POST',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (e) => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded * 100) / e.total))
      }
    },
  })
}

/**
 * 静默请求（业务错误不弹通知，由调用方决定如何提示）
 */
export const silent = {
  get: <T = unknown>(url: string, params?: Record<string, unknown>) =>
    get<T>(url, params, { silent: true }),
  post: <T = unknown>(url: string, data?: unknown) =>
    post<T>(url, data, { silent: true }),
  put: <T = unknown>(url: string, data?: unknown) =>
    put<T>(url, data, { silent: true }),
  del: <T = unknown>(url: string) => del<T>(url, { silent: true }),
}

export default service