/** 系统日志 */
export interface SystemLog {
  logId: number
  userId?: string
  username?: string
  operation: string
  targetType?: string
  targetId?: string
  description?: string
  ipAddress?: string
  createTime?: string
}

/** 日志查询 */
export interface LogQuery {
  page?: number
  size?: number
  userId?: string
  operation?: string
  targetType?: string
  targetId?: string
  startTime?: string
  endTime?: string
}

/** 目标类型 → 中文映射 */
export const TargetTypeText: Record<string, string> = {
  task: '打印任务',
  project: '项目',
  printer: '打印机',
  material: '耗材',
  artwork: '作品',
  member: '成员',
  file: '文件',
}

/** 操作 → 中文映射 */
export const OperationText: Record<string, string> = {
  'task.apply': '提交任务',
  'task.approve': '审批通过',
  'task.reject': '驳回任务',
  'task.cancel': '取消任务',
  'task.pickup': '取件签到',
  'task.assign': '分配打印机',
  'task.start': '开始打印',
  'task.finish': '打印完成',
  'project.create': '创建项目',
  'project.update': '更新项目',
  'project.complete': '完成项目',
  'project.cancel': '取消项目',
  'printer.create': '新增打印机',
  'printer.update': '更新打印机',
  'printer.maintain': '打印机维护',
  'material.inbound': '耗材入库',
  'artwork.create': '创建作品',
  'artwork.update': '更新作品',
  'artwork.recommend': '设置推荐',
  'member.update': '更新成员',
  'member.role': '修改角色',
  'auth.login': '登录',
  'auth.logout': '登出',
}
