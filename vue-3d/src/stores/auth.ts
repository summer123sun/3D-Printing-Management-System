/**
 * 认证 Store：登录态 / token / 当前用户
 */
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { LoginDTO, LoginResult, Member, ChangePasswordDTO } from '@/types/member'
import { Role } from '@/types/member'
import { getToken, setToken, clearAuth, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import * as authApi from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const user = ref<Member | null>(getUserInfo<Member>())

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role ?? Role.NEWBIE)
  const isPresident = computed(() => role.value === Role.PRESIDENT)
  const isTechLead = computed(() => role.value === Role.TECH_LEAD || role.value === Role.PRESIDENT)

  /**
   * 登录
   */
  const login = async (dto: LoginDTO) => {
    const result = await authApi.login(dto) as unknown as LoginResult
    token.value = result.token
    user.value = result.user
    setToken(result.token)
    setUserInfo(result.user) // 持久化用户信息，防止刷新后权限丢失
    return result
  }

  /**
   * 退出
   */
  const logout = () => {
    token.value = null
    user.value = null
    clearAuth()
  }

  /**
   * 刷新当前用户信息
   */
  const fetchUserInfo = async () => {
    const info = await authApi.getUserInfo() as unknown as Member
    user.value = info
    setUserInfo(info) // 同步持久化
    return info
  }

  /**
   * 修改密码
   */
  const changePassword = async (dto: ChangePasswordDTO) => {
    return authApi.changePassword(dto)
  }

  /**
   * ✅ v2.2 修复（用户反馈）：同浏览器多窗口（Tab）不同账号登录导致权限混乱
   *    原因：localStorage 是浏览器级共享的，Tab A 改 token 后 Tab B 内存里的 store.user 没更新
   *    修复：监听 'storage' 事件（同一浏览器不同 Tab 触发），token 变化时重新拉用户信息
   *    注：storage 事件**不会**在当前 Tab 自己改 localStorage 时触发，所以新登录 Tab 自己 store 同步没问题
   *
   * 必须在 main.ts 启动时调用一次：useAuthStore().setupStorageSync()
   */
  const setupStorageSync = () => {
    if (typeof window === 'undefined') return
    window.addEventListener('storage', async (e) => {
      // 监听 auth-token 变化（setToken 写入 'auth-token' 这个 key）
      if (e.key !== 'auth-token') return
      const newToken = e.newValue
      const oldToken = e.oldValue
      if (newToken === oldToken) return

      // 另一 Tab 退出登录（newValue=null）→ 自己 Tab 也跟着退出
      if (!newToken) {
        logout()
        // 路由守卫会自动跳 /login
        return
      }

      // 另一 Tab 登录/换号 → 重新拉 user info（用新 token）
      token.value = newToken
      try {
        await fetchUserInfo()
      } catch {
        // 拉失败（可能 token 已失效）→ 退出
        logout()
      }
    })
  }

  return {
    token,
    user,
    isLoggedIn,
    role,
    isPresident,
    isTechLead,
    login,
    logout,
    fetchUserInfo,
    changePassword,
    setupStorageSync,
  }
})