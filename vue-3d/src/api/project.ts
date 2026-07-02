/**
 * 项目 API（E）
 */
import { get, post, put, del } from '@/utils/request'
import type {
  ProjectCreateDTO,
  AddMemberDTO,
  ProjectQuery,
  ProjectDetailVO,
} from '@/types/project'
import type { Project } from '@/types/project'
import type { PageResult } from '@/types/api'

/** 创建项目 */
export const createProject = (dto: ProjectCreateDTO) =>
  post<number>('/project', dto)

/** 项目列表 */
export const projectList = (query: ProjectQuery) =>
  get<PageResult<Project>>('/project/list', query as Record<string, unknown>)

/** 项目详情（聚合 4 张表） */
export const projectDetail = (id: number) =>
  get<ProjectDetailVO>(`/project/${id}`)

/** 标记完成 */
export const completeProject = (id: number) =>
  put<void>(`/project/${id}/complete`)

/**
 * 修改项目（项目负责人 + 技术骨干+）
 * 后端：PUT /api/project/{id}
 */
export const updateProject = (id: number, dto: ProjectCreateDTO) =>
  put<void>(`/project/${id}`, dto)

/** 取消项目 */
export const cancelProject = (id: number) =>
  put<void>(`/project/${id}/cancel`)

/**
 * 添加项目成员
 * ✅ v2.13 修复（审查发现）：之前叫 addMember，和 api/user.ts 的 addMember（添加成员账号）重名
 *    命名空间调用容易错位（参数签名完全不同），改名 addProjectMember 明确语义
 */
export const addProjectMember = (id: number, dto: AddMemberDTO) =>
  post<void>(`/project/${id}/member`, dto)

/** 移除成员 */
export const removeMember = (id: number, mid: string) =>
  del<void>(`/project/${id}/member/${mid}`)

/** 更新阶段状态 */
export const updateStageStatus = (id: number, pid: number, status: number) =>
  put<void>(`/project/${id}/progress/${pid}/status`, undefined, {
    params: { status },
  })
