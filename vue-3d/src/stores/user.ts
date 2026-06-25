/**
 * 用户 Store：成员列表 / 个人统计
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Member } from '@/types/member'
import * as userApi from '@/api/user'
import type { PageQuery, PageResult } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  const memberList = ref<Member[]>([])
  const total = ref<number>(0)
  const loading = ref<boolean>(false)

  /**
   * 获取成员列表（分页）
   */
  const fetchMemberList = async (query: PageQuery = {}) => {
    loading.value = true
    try {
      const res = await userApi.getMemberList(query) as unknown as PageResult<Member>
      memberList.value = res.list
      total.value = res.total
      return res
    } finally {
      loading.value = false
    }
  }

  /**
   * 修改角色
   */
  const updateRole = async (studentId: string, role: number) => {
    await userApi.updateRole(studentId, role)
  }

  /**
   * 修改技能等级
   */
  const updateSkill = async (studentId: string, skillLevel: number) => {
    await userApi.updateSkill(studentId, skillLevel)
  }

  return {
    memberList,
    total,
    loading,
    fetchMemberList,
    updateRole,
    updateSkill,
  }
})