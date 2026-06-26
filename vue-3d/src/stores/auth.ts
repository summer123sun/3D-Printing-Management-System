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
  }
})