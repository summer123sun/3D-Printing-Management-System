/**
 * 文件 API
 */
import { del, get, upload } from '@/utils/request'

interface UploadResult {
  url: string
  fileName: string
  size: string
}

/** 文件上传（带进度回调） */
export const uploadFile = (formData: FormData, onProgress?: (pct: number) => void) =>
  upload<UploadResult>('/file/upload', formData, onProgress)

/**
 * ✅ v2.2 新增：管理员列出所有已上传文件
 * @param type 不传 = 全部；传 stl/img/project = 按类型筛
 */
export const listFiles = (type?: string) =>
  get<Array<{
    name: string
    type: string
    path: string
    size: number
    lastModified: number
  }>>('/file/list', { type })

/**
 * ✅ v2.2 新增：管理员删除任意文件
 * @param path 相对路径（stl/xxx.stl）
 */
export const adminDeleteFile = (path: string) =>
  del<void>('/file/admin', { params: { path } })
