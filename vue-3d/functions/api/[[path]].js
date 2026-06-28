/**
 * Cloudflare Pages Function：把前端的 /api/* 反代到阿里云 ECS 后端
 * 部署时间：2026-06-28
 *
 * 关键点：
 * 1. 不覆盖 Origin 头（让浏览器原始 Origin 传到后端，让 CORS 校验通过）
 * 2. 保留 method 和 body（POST + JSON body 必须完整转发）
 * 3. 改写 Host 头到 ECS（否则后端 Nginx default_server 可能误判）
 * 4. 处理 OPTIONS preflight（CORS 预检）
 */

// 后端地址（阿里云 ECS 公网 IP）
const BACKEND_ORIGIN = 'http://8.137.80.194'

export async function onRequest(context) {
  const { request } = context

  // CORS preflight 直接返回（不转后端）
  if (request.method === 'OPTIONS') {
    return new Response(null, {
      status: 204,
      headers: {
        'Access-Control-Allow-Origin': request.headers.get('Origin') || '*',
        'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS, PATCH',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Requested-With',
        'Access-Control-Allow-Credentials': 'true',
        'Access-Control-Max-Age': '86400',
      },
    })
  }

  // 1. 构造转发 URL
  const url = new URL(request.url)
  const targetUrl = BACKEND_ORIGIN + url.pathname + url.search

  // 2. 复制请求头，但去掉 Cloudflare 相关
  const headers = new Headers(request.headers)
  // 不要覆盖 Origin！让浏览器原始的 Origin 传过去，让后端 CorsConfig 校验通过
  // headers.set('Origin', BACKEND_ORIGIN)  ← 之前的错误：覆盖了
  headers.set('Host', '8.137.80.194')
  headers.delete('cf-connecting-ip')
  headers.delete('cf-ray')
  headers.delete('cf-worker')
  headers.delete('cf-cache-status')

  // 3. 构造转发请求
  const init = {
    method: request.method,
    headers,
    body: request.method === 'GET' || request.method === 'HEAD' ? undefined : request.body,
    redirect: 'manual',
  }

  // 4. 调后端
  let response
  try {
    response = await fetch(targetUrl, init)
  } catch (err) {
    return new Response(`Backend unreachable: ${err.message}`, {
      status: 502,
      headers: { 'Content-Type': 'text/plain; charset=utf-8' },
    })
  }

  // 5. 构造返回响应
  const responseHeaders = new Headers(response.headers)
  // 不覆盖 CORS 头：让后端 CorsConfig 返回的 Access-Control-Allow-Origin 保留
  // （后端看到的是浏览器原始 Origin，会返回匹配的 Allow-Origin）

  return new Response(response.body, {
    status: response.status,
    headers: responseHeaders,
  })
}
