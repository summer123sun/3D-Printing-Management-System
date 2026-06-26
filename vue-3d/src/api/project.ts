/**
 * 项目 API（E）
 */
import { get, post, put, del } from '@/utils/request'
import type {
  ProjectCreateDTO,
  AddMemberDTO,
  StageDTO,
  ProjectQuery,
  ProjectDetailVO,
} from '@/types/project'
import type { Project } from '@/types/project'
import type { PageResult } from '@/types/api'

/** 创建项目 */
export const createProject = (dto: ProjectCreateDTO) => {
  return post<number>('/project', dto)
}

/** 项目列表 */
export const projectList = (query: ProjectQuery) => {
  return get<PageResult<Project>>('/project/list', query as Record<string, unknown>)
}

/** 项目详情（聚合 4 张表） */
export const projectDetail = (id: number) => {
  return get<ProjectDetailVO>(`/project/${id}`)
}

/** 修改项目 */
export const updateProject = (id: number, dto: ProjectCreateDTO) => {
  return put<void>(`/project/${id}`, dto)
}

/** 标记完成 */
export const completeProject = (id: number) => {
  return put<void>(`/project/${id}/complete`)
}

/** 取消项目 */
export const cancelProject = (id: number) => {
  return put<void>(`/project/${id}/cancel`)
}

/** 添加成员 */
export const addMember = (id: number, dto: AddMemberDTO) => {
  return post<void>(`/project/${id}/member`, dto)
}

/** 移除成员 */
export const removeMember = (id: number, mid: string) => {
  return del<void>(`/project/${id}/member/${mid}`)
}

/** 修改成员角色 */
export const updateMemberRole = (id: number, mid: string, dto: AddMemberDTO) => {
  return put<void>(`/project/${id}/member/${mid}`, dto)
}

/** 添加阶段 */
export const addStage = (id: number, dto: StageDTO) => {
  return post<number>(`/project/${id}/progress`, dto)
}

/** 修改阶段 */
export const updateStage = (id: number, pid: number, dto: StageDTO) => {
  return put<void>(`/project/${id}/progress/${pid}`, dto)
}

/** 更新阶段状态 */
export const updateStageStatus = (id: number, pid: number, status: number) => {
  return put<void>(`/project/${id}/progress/${pid}/status`, undefined, {
    params: { status },
  })
}

/** 删除阶段 */
export const deleteStage = (id: number, pid: number) => {
  return del<void>(`/project/${id}/progress/${pid}`)
}