/**
 * 成员/用户 API（v2）
 */
import { get, put } from '@/utils/request'
import type { Member, Role, SkillLevel, UserStatsVO } from '@/types/member'
import type { PageResult } from '@/types/api'

/** 成员列表（分页 + 关键字搜索 + 角色筛选） */
export const memberList = (params: { page?: number; size?: number; keyword?: string; role?: number }) =>
  get<PageResult<Member>>('/user/list', params as Record<string, unknown>)

/** 修改成员角色（仅社长） */
export const updateMemberRole = (studentId: string, role: Role) =>
  put<void>(`/user/${studentId}/role`, { role })

/** 修改成员技能等级（社长+技术骨干） */
export const updateMemberSkill = (studentId: string, skillLevel: SkillLevel) =>
  put<void>(`/user/${studentId}/skill`, { skillLevel })

/**
 * 获取成员统计信息（个人中心用）
 * 对应后端 GET /api/user/{studentId}/stats（路径变量风格）
 * 返回字段：totalPrints（打印次数）/ totalProjects（参与项目）/ totalArtworks（作品数）
 */
export const getUserStats = (studentId?: string) =>
  get<UserStatsVO>(`/user/${studentId}/stats`)
