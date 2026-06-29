/**
 * 认证相关 API
 */
import { get, post } from '@/utils/request'
import type { LoginDTO, ChangePasswordDTO, Member, UpdateUserInfoDTO } from '@/types/member'

/** 登录 */
export const login = (dto: LoginDTO) => {
  return post('/auth/login', dto)
}

/** 退出 */
export const logout = () => {
  return post('/auth/logout')
}

/** 获取当前用户信息（从 /api/auth/info 取，不再依赖 D 的 user 模块） */
export const getUserInfo = () => {
  return get<Member>('/auth/info')
}

/**
 * 修改个人信息（对应后端 POST /api/user/info）
 * 后端只接受 phone/email/avatar，其他字段会被忽略
 */
export const updateUserInfo = (dto: UpdateUserInfoDTO) => {
  return post<void>('/user/info', dto)
}

/** 修改密码 */
export const changePassword = (dto: ChangePasswordDTO) => {
  return post('/user/password', dto)
}