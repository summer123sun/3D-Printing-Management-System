/**
 * 文件上传 API
 */
import { post } from '@/utils/request'

interface UploadResult {
  url: string
  fileName: string
  size: string
}

/** 上传文件（FormData） */
export const uploadFile = (formData: FormData): Promise<UploadResult> => {
  return post<UploadResult>('/file/upload', formData)
}
