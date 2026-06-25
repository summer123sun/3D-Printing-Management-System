# module

业务模块（按功能拆 8 个）

- `auth/` — 登录/登出（D）
- `user/` — 用户 CRUD（D）
- `task/` — 打印任务（**E** 核心）
- `project/` — 项目（**E** 核心）
- `artwork/` — 作品库（**F**）
- `printer/` — 打印机（**F**）
- `material/` — 耗材（**F**）
- `stats/` — 统计看板（**F**）
- `log/` — 系统日志（**F**）

每个模块固定包含：`controller/` + `service/` + `mapper/` + `entity/` + 可选 `dto/`
