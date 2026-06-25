/**
 * 认证相关 API
 */
import { get, post } from '@/utils/request'
import type { LoginDTO, ChangePasswordDTO, Member } from '@/types/member'

/** 登录 */
export const login = (dto: LoginDTO) => {
  return post('/auth/login', dto)
}

/** 退出 */
export const logout = () => {
  return post('/auth/logout')
}

/** 获取当前用户信息 */
export const getUserInfo = () => {
  return get<Member>('/user/info')
}

/** 修改个人信息 */
export const updateUserInfo = (user: Partial<Member>) => {
  return post('/user/info', user)
}

/** 修改密码 */
export const changePassword = (dto: ChangePasswordDTO) => {
  return post('/user/password', dto)
}