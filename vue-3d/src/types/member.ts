/**
 * 成员类型
 */

/** 角色枚举 */
export enum Role {
  PRESIDENT = 1, // 社长
  TECH_LEAD = 2, // 技术骨干
  MEMBER = 3,    // 普通社员
  NEWBIE = 4,    // 新成员
}

/** 角色文本映射 */
export const RoleText: Record<Role, string> = {
  [Role.PRESIDENT]: '社长',
  [Role.TECH_LEAD]: '技术骨干',
  [Role.MEMBER]: '普通社员',
  [Role.NEWBIE]: '新成员',
}

/** 技能等级枚举 */
export enum SkillLevel {
  NONE = 0,           // 未入门
  TINKERCAD = 1,      // Tinkercad
  FUSION360 = 2,      // Fusion 360
  BLENDER = 3,        // Blender
  TUNING = 4,         // 调机熟练
}

/** 技能等级文本 */
export const SkillLevelText: Record<SkillLevel, string> = {
  [SkillLevel.NONE]: '未入门',
  [SkillLevel.TINKERCAD]: 'Tinkercad',
  [SkillLevel.FUSION360]: 'Fusion 360',
  [SkillLevel.BLENDER]: 'Blender',
  [SkillLevel.TUNING]: '调机熟练',
}

/** 成员状态 */
export enum MemberStatus {
  ACTIVE = 1,    // 正常
  QUIT = 2,      // 退出
}

/** 成员状态文本 */
export const MemberStatusText: Record<MemberStatus, string> = {
  [MemberStatus.ACTIVE]: '正常',
  [MemberStatus.QUIT]: '退出',
}

/** 成员完整信息 */
export interface Member {
  studentId: string
  name: string
  role: Role
  skillLevel: SkillLevel
  joinDate: string
  totalPrints: number
  phone?: string
  email?: string
  avatar?: string
  status: MemberStatus
  createTime?: string
  updateTime?: string
}

/** 登录请求 */
export interface LoginDTO {
  studentId: string
  password: string
}

/** 登录返回 */
export interface LoginResult {
  token: string
  user: Member
}

/** 修改密码请求 */
export interface ChangePasswordDTO {
  oldPassword: string
  newPassword: string
}