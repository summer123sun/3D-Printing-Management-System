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
  StageDTO,
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

  const update = async (id: number, dto: ProjectCreateDTO) => {
    await projectApi.updateProject(id, dto)
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

  // 阶段
  const addStage = async (id: number, dto: StageDTO) => {
    return projectApi.addStage(id, dto)
  }

  const updateStage = async (id: number, pid: number, dto: StageDTO) => {
    await projectApi.updateStage(id, pid, dto)
  }

  const updateStageStatus = async (id: number, pid: number, status: StageStatus) => {
    await projectApi.updateStageStatus(id, pid, status)
  }

  const deleteStage = async (id: number, pid: number) => {
    await projectApi.deleteStage(id, pid)
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
    addStage,
    updateStage,
    updateStageStatus,
    deleteStage,
  }
})