/**
 * 用户管理 API（C 负责成员管理页面时填写）
 */
import { get, put } from '@/utils/request'
import type { Member } from '@/types/member'
import type { PageQuery, PageResult } from '@/types/api'

/** 成员列表（分页） */
export const getMemberList = (query: PageQuery = {}) => {
  return get<PageResult<Member>>('/user/list', query as Record<string, unknown>)
}

/** 修改角色（仅社长） */
export const updateRole = (studentId: string, role: number) => {
  return put(`/user/${studentId}/role`, { role })
}

/** 修改技能等级 */
export const updateSkill = (studentId: string, skillLevel: number) => {
  return put(`/user/${studentId}/skill`, { skillLevel })
}

/** 个人统计 */
export const getUserStats = (studentId: string) => {
  return get<{
    totalPrints: number
    totalProjects: number
    totalArtworks: number
  }>(`/user/${studentId}/stats`)
}