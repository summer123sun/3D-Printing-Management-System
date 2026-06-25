# annotation

自定义注解

- `RequireAuth.java` — 标记接口需要登录
- `RequireRole.java` — 标记接口需要特定角色（如 `{1, 2}` 仅社长/技术骨干）
- `AuditLog.java` — 标记需要记日志的操作（**F** 实现 AOP）
