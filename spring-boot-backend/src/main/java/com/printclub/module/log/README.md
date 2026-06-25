# log

系统日志模块（**F**）

接口：`/api/log/list`

## 业务要点

- AOP 自动记录：标注 `@AuditLog` 的 Controller 方法会自动写日志
- 不存数据快照（v2 简化版）
