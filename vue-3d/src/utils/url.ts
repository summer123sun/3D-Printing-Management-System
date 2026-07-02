/**
 * URL 处理工具（v2.12）
 *
 * 核心问题：之前所有 :src 直接吃 "/uploads/xxx.png" 相对路径，
 * 本地 Vite proxy 转发 OK，但生产部署后所有图片 404。
 *
 * 修法：所有 :src 一律走 fileUrl()，自动处理：
 *   1. 空值 / null / undefined → 兜底返回 ''
 *   2. 已经是 http(s):// 开头 → 原样返回
 *   3. 以 / 开头（相对路径）→ 拼上 VITE_API_BASE_URL 的 origin
 *   4. 其他情况（不带 / 前缀）→ 加 / 再拼
 */

const envBase = import.meta.env.VITE_API_BASE_URL || ''

/**
 * 把后端返回的 file path 转成可在 :src / :href 直接用的完整 URL
 * @param path 后端返回的相对路径（如 "stl/202607/xxx.stl" 或 "/uploads/stl/202607/xxx.stl"）
 * @returns 完整 URL（开发环境用 vite proxy，生产环境用 API baseURL）
 */
export const fileUrl = (path: string | null | undefined): string => {
  if (!path) return ''
  // 已经是完整 URL，原样返回
  if (/^https?:\/\//i.test(path)) return path
  // data: / blob: 也原样返回
  if (/^(data|blob):/i.test(path)) return path
  // 提取 baseURL 的 origin 部分（去掉 /api 后缀）
  // 例如 'https://api.3dprint.ccwu.cc/api' → 'https://api.3dprint.ccwu.cc'
  let origin = ''
  if (envBase) {
    try {
      const u = new URL(envBase)
      origin = u.origin
    } catch {
      origin = envBase.replace(/\/api\/?$/, '')
    }
  }
  // path 已经以 / 开头
  if (path.startsWith('/')) return origin + path
  // path 不以 / 开头（"stl/202607/xxx.stl"）
  return origin + '/' + path
}

/**
 * 文件下载链接（用于 <el-link :href>）
 * 走 /api/file/download/ 转发，后端会做权限校验（v2.12 加了 @RequireAuth）
 */
export const fileDownloadUrl = (path: string | null | undefined): string => {
  if (!path) return ''
  // 已经是完整 URL（http/https）原样返回
  if (/^https?:\/\//i.test(path)) return path
  // 去掉开头的 /
  const p = path.replace(/^\/+/, '')
  // 走 /api/file/download/<path>
  const base = envBase || ''
  return base.endsWith('/api') ? `${base}/file/download/${p}` : `${base}/api/file/download/${p}`
}
