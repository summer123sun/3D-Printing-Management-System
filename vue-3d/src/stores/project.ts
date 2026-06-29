/**
 * 项目 Store（E）
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as projectApi from '@/api/project'
import type {
  Project,
  ProjectCreateDTO,
  AddMemberDTO,
  ProjectQuery,
  ProjectDetailVO,
  StageStatus,
} from '@/types/project'
import type { PageResult } from '@/types/api'

export const useProjectStore = defineStore('project', () => {
  // ============== State ==============
  const projectList = ref<PageResult<Project> | null>(null)
  const currentProject = ref<ProjectDetailVO | null>(null)

  const loading = ref(false)
  const submitting = ref(false)

  // ============== Actions ==============

  const fetchList = async (query: ProjectQuery = {}) => {
    loading.value = true
    try {
      projectList.value = await projectApi.projectList(query)
    } finally {
      loading.value = false
    }
  }

  const fetchDetail = async (id: number) => {
    loading.value = true
    try {
      currentProject.value = await projectApi.projectDetail(id)
      return currentProject.value
    } finally {
      loading.value = false
    }
  }

  const create = async (dto: ProjectCreateDTO) => {
    submitting.value = true
    try {
      const id = await projectApi.createProject(dto)
      return id
    } finally {
      submitting.value = false
    }
  }

  /**
   * 修改项目（项目负责人）
   * v2.2 新增：之前前端没封装 update action，给 admin/project/manage 加编辑按钮必须先加这个
   */
  const update = async (id: number, dto: ProjectCreateDTO) => {
    submitting.value = true
    try {
      await projectApi.updateProject(id, dto)
    } finally {
      submitting.value = false
    }
  }

  const complete = async (id: number) => {
    await projectApi.completeProject(id)
  }

  const cancel = async (id: number) => {
    await projectApi.cancelProject(id)
  }

  // 成员
  const addMember = async (id: number, dto: AddMemberDTO) => {
    await projectApi.addMember(id, dto)
  }

  const removeMember = async (id: number, mid: string) => {
    await projectApi.removeMember(id, mid)
  }

  const updateStageStatus = async (id: number, pid: number, status: StageStatus) => {
    await projectApi.updateStageStatus(id, pid, status)
  }

  return {
    projectList,
    currentProject,
    loading,
    submitting,
    fetchList,
    fetchDetail,
    create,
    update,
    complete,
    cancel,
    addMember,
    removeMember,
    updateStageStatus,
  }
})