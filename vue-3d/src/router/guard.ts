/**
 * 路由守卫
 * - 未登录访问受保护页面 → 重定向到 /login
 * - 已登录访问 /login → 重定向到 /
 * - 无权限访问 → 重定向到 /403
 */
import type { NavigationGuardWithThis } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

export const setupRouterGuard: NavigationGuardWithThis<undefined> = function (to) {
  NProgress.start()

  const authStore = useAuthStore()
  const token = authStore.token

  // 设置页面 title
  const title = (to.meta?.title as string) || ''
  document.title = title
    ? `${title} - ${import.meta.env.VITE_APP_TITLE || '3D打印科创会'}`
    : (import.meta.env.VITE_APP_TITLE || '3D打印科创会')

  // 公开页面：/login, /403, /404
  const isPublicRoute = to.path === '/login' || to.path === '/403' || to.path === '/404'

  // 已登录访问 /login：跳回首页
  if (to.path === '/login' && token) {
    NProgress.done()
    return { path: '/' }
  }

  // 公开路由直接放行
  if (isPublicRoute) {
    NProgress.done()
    return true
  }

  // 未登录：跳登录
  if (!token) {
    NProgress.done()
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 角色权限校验
  const requiredRoles = to.meta?.roles as number[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    const userRole = authStore.user?.role ?? 0
    if (!requiredRoles.includes(userRole)) {
      NProgress.done()
      return { path: '/403' }
    }
  }

  NProgress.done()
  return true
}