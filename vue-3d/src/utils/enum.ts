/**
 * 枚举集中导出
 *
 * 用法：
 *   import { TaskStatus, Role, ErrorCode, RecommendedFlag } from '@/utils/enum'
 *
 * v2 重构：项目里之前到处写 `status === 1` / `role === 1` / `code === 200` / `isRecommended === 1`
 * 这种硬编码数字，后端改 enum 值就静默失效。统一通过这里导出，前端 import 后用枚举常量。
 */
export { TaskStatus, TaskStatusText, TaskStatusTagType, Priority, PriorityText } from '@/types/task'
export { Role, RoleText, SkillLevel, SkillLevelText, MemberStatus, MemberStatusText } from '@/types/member'
export { ProjectStatus, ProjectStatusText, ProjectStatusTagType, ProjectType, ProjectTypeText, ProjectRole, ProjectRoleText, ProjectMemberStatus, StageStatus, StageStatusText, StageStatusTagType } from '@/types/project'
export { RecommendedFlag } from '@/types/artwork'
export { PrinterStatus, PrinterStatusText, PrinterStatusTagType, MaintType, MaintTypeText, MaintTypeTagType } from '@/types/printer'
export { ErrorCode } from '@/types/api'