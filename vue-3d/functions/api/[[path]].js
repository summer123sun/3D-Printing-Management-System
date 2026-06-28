/**
 * Cloudflare Pages Function：把前端的 /api/* 反代到阿里云 ECS 后端
 * 部署时间：2026-06-28
 * 为什么不用 _redirects：Cloudflare Pages _redirects 不能做真正的反向代理，会丢 POST body / 改 method
 */

// 后端地址（阿里云 ECS 公网 IP）
const BACKEND_ORIGIN = 'http://8.137.80.194'

export async function onRequest(context) {
  const { request } = context

  // 1. 构造转发 URL
  const url = new URL(request.url)
  const targetUrl = BACKEND_ORIGIN + url.pathname + url.search

  // 2. 复制请求头，但去掉 Cloudflare 相关和 Host 头
  const headers = new Headers(request.headers)
  headers.set('Host', new URL(BACKEND_ORIGIN).host)
  headers.set('Origin', BACKEND_ORIGIN)
  headers.delete('cf-connecting-ip')
  headers.delete('cf-ray')
  headers.delete('cf-worker')
  headers.delete('cf-cache-status')

  // 3. 构造转发请求（保留 method + body）
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

  // 5. 构造返回响应（保留后端 status + headers）
  const responseHeaders = new Headers(response.headers)
  // 关键：CORS 头由后端 CorsConfig 控制，这里不覆盖
  // 但因为是 server-to-server 调用，浏览器看到的 Origin 还是 pages.dev
  // 所以后端 CorsConfig 必须放行 *.pages.dev（已加）

  return new Response(response.body, {
    status: response.status,
    headers: responseHeaders,
  })
}
