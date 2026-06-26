/**
 * 项目类型（E）
 *
 * @see 需求规格说明书 v2 §3.6 / §3.7 / §3.8 / §3.9
 */

/** 项目状态 */
export enum ProjectStatus {
  PREPARING = 0, // 筹备中
  RUNNING = 1,   // 进行中
  DONE = 2,      // 已完成
  CANCELLED = 3, // 已取消
}

export const ProjectStatusText: Record<ProjectStatus, string> = {
  [ProjectStatus.PREPARING]: '筹备中',
  [ProjectStatus.RUNNING]: '进行中',
  [ProjectStatus.DONE]: '已完成',
  [ProjectStatus.CANCELLED]: '已取消',
}

export const ProjectStatusTagType: Record<ProjectStatus, 'info' | 'primary' | 'success' | 'danger'> = {
  [ProjectStatus.PREPARING]: 'info',
  [ProjectStatus.RUNNING]: 'primary',
  [ProjectStatus.DONE]: 'success',
  [ProjectStatus.CANCELLED]: 'danger',
}

/** 项目类型 */
export enum ProjectType {
  CREATION = 1, // 作品创作
  COMPETE = 2,  // 竞赛备赛
  ORDER = 3,    // 定制订单
  ACTIVITY = 4, // 社团活动
}

export const ProjectTypeText: Record<ProjectType, string> = {
  [ProjectType.CREATION]: '作品创作',
  [ProjectType.COMPETE]: '竞赛备赛',
  [ProjectType.ORDER]: '定制订单',
  [ProjectType.ACTIVITY]: '社团活动',
}

/** 项目内角色 */
export enum ProjectRole {
  LEADER = 1,       // 负责人
  CORE = 2,         // 核心成员
  PARTICIPANT = 3,  // 参与成员
}

export const ProjectRoleText: Record<ProjectRole, string> = {
  [ProjectRole.LEADER]: '负责人',
  [ProjectRole.CORE]: '核心成员',
  [ProjectRole.PARTICIPANT]: '参与成员',
}

/** 项目成员状态 */
export enum ProjectMemberStatus {
  ACTIVE = 1,
  QUIT = 2,
  COMPLETE = 3,
}

/** 阶段状态 */
export enum StageStatus {
  PENDING = 0,
  RUNNING = 1,
  DONE = 2,
}

export const StageStatusText: Record<StageStatus, string> = {
  [StageStatus.PENDING]: '未开始',
  [StageStatus.RUNNING]: '进行中',
  [StageStatus.DONE]: '已完成',
}

export const StageStatusTagType: Record<StageStatus, 'info' | 'primary' | 'success'> = {
  [StageStatus.PENDING]: 'info',
  [StageStatus.RUNNING]: 'primary',
  [StageStatus.DONE]: 'success',
}

/** 项目主表 */
export interface Project {
  projectId: number
  projectName: string
  projectType: ProjectType
  leaderId: string
  startDate: string
  endDate?: string
  actualEndDate?: string
  budget?: number
  actualCost?: number
  description?: string
  deliverables?: string
  coverImage?: string
  status: ProjectStatus
  createTime?: string
  updateTime?: string
}

/** 项目成员 */
export interface ProjectMember {
  pmId: number
  projectId: number
  memberId: string
  memberName?: string
  roleInProject: ProjectRole
  contribution?: string
  joinTime?: string
  status: ProjectMemberStatus
}

/** 阶段 */
export interface ProjectProgress {
  progressId: number
  projectId: number
  stageName: string
  stageOrder: number
  description?: string
  status: StageStatus
  responsibleId?: string
  responsibleName?: string
  startTime?: string
  endTime?: string
}

/** 项目文件 */
export interface ProjectFile {
  fileId: number
  projectId: number
  fileName: string
  filePath: string
  fileType: number
  fileSize?: number
  uploaderId: string
  uploadTime?: string
}

/** 创建项目请求（含嵌套 stages 和 initialMembers） */
export interface ProjectCreateDTO {
  projectName: string
  projectType: ProjectType
  leaderId?: string
  startDate: string
  endDate?: string
  budget?: number
  description?: string
  deliverables?: string
  coverImage?: string
  stages?: StageDTO[]
  initialMembers?: AddMemberDTO[]
}

/** 阶段 DTO */
export interface StageDTO {
  stageName: string
  stageOrder: number
  description?: string
  responsibleId?: string
}

/** 添加成员 DTO */
export interface AddMemberDTO {
  memberId: string
  roleInProject: ProjectRole
  contribution?: string
}

/** 项目查询参数 */
export interface ProjectQuery {
  page?: number
  size?: number
  status?: ProjectStatus
  projectType?: ProjectType
  /** mine = 仅我参与的；不传 = 全部 */
  scope?: string
  keyword?: string
}

/** 项目详情（聚合 4 张表 + 关联任务） */
export interface ProjectDetailVO {
  project: Project
  members: ProjectMember[]
  stages: ProjectProgress[]
  files: ProjectFile[]
  /** 关联的打印任务（来自 print_task WHERE project_id=?） */
  relatedTasks: Array<{
    taskId: string
    title: string
    status: number
    applyTime?: string
  }>
}