/**
 * Axios 封装 + JWT 拦截器 + 统一错误处理
 * @see 需求规格说明书 v2 §7.1 统一返回格式
 */
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { ErrorCode, type ApiResponse } from '@/types/api'
import { getToken, clearAuth } from './auth'

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

// ============================================
// 响应拦截器：统一处理 code + 错误提示
// ============================================
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    // 后端约定：code === 200 表示成功
    if (res.code === ErrorCode.SUCCESS) {
      return res.data as unknown as AxiosResponse
    }

    // 401 未登录：清 token，跳登录
    if (res.code === ErrorCode.UNAUTHORIZED) {
      clearAuth()
      ElMessage.error('登录已过期，请重新登录')
      // 用 location 而不是 router（避免循环依赖）
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.msg))
    }

    // 403 无权限
    if (res.code === ErrorCode.FORBIDDEN) {
      ElMessage.error(res.msg || '无权限访问')
      return Promise.reject(new Error(res.msg))
    }

    // 其它业务错误
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg))
  },
  (error) => {
    // HTTP 层面错误（网络/超时/5xx）
    let msg = '网络错误，请稍后再试'
    if (error.response) {
      const status = error.response.status
      if (status === 401) msg = '登录已过期'
      else if (status === 403) msg = '无权限访问'
      else if (status === 404) msg = '资源不存在'
      else if (status >= 500) msg = '服务器错误'
      else msg = error.response.data?.msg || msg
    } else if (error.code === 'ECONNABORTED') {
      msg = '请求超时'
    }
    ElMessage.error(msg)
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

export default service