# api

按业务模块拆文件，每个文件对应一类后端接口。

## 文件清单

- `auth.ts` — 登录/登出（A）
- `user.ts` — 用户 CRUD（A）
- `task.ts` — 打印任务全流程（**B**）
- `project.ts` — 项目全流程（**B**）
- `artwork.ts` — 作品库（C）
- `printer.ts` — 打印机（C）
- `material.ts` — 耗材（C）
- `stats.ts` — 统计看板（C）
- `log.ts` — 系统日志（C）

## 调用方式

所有请求通过 `../utils/request.ts` 封装的 axios 实例发起，自动带 JWT 拦截。

## Mock 阶段

第 1-3 周后端接口未就绪时，本目录下的 `*.ts` 文件可以临时改用 mockjs 拦截；联调前再切回真实接口。
