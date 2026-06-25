# 顶层目录

| 目录 | 用途 | 谁负责 |
|---|---|---|
| [api/](./api/) | 接口请求层，按业务模块拆文件 | 全员 |
| [assets/](./assets/) | 静态资源（图片/icon 等） | 全员 |
| [components/](./components/) | 公共组件 | 全员 |
| [composables/](./composables/) | 组合式函数（useTable / useDict 等） | A |
| [directives/](./directives/) | Vue 自定义指令 | A |
| [mock/](./mock/) | mock 数据（前端独立开发期） | 全员 |
| [plugins/](./plugins/) | Vue 插件（element-plus 按需引入等） | A |
| [router/](./router/) | vue-router 配置 + 路由守卫 | A |
| [stores/](./stores/) | Pinia 状态管理 | 全员 |
| [styles/](./styles/) | 全局样式（SCSS 变量 / 主题） | A |
| [types/](./types/) | TypeScript 类型定义 | 全员 |
| [utils/](./utils/) | 工具函数（request / format / auth） | A |
| [views/](./views/) | 页面文件（按业务模块拆目录） | 全员 |

## 模块分工（按 [前端开发大纲 v1](../documents/3D打印科创会管理系统_前端开发大纲_v1.md)）

- **B（组长，本仓 owner）**：[`views/task/`](./views/task/) + [`views/project/`](./views/project/) + [`views/admin/task/`](./views/admin/task/) + [`views/admin/project/`](./views/admin/project/) + [`components/task/`](./components/task/) + [`components/project/`](./components/project/) + [`stores/task.ts`](./stores/) + [`stores/project.ts`](./stores/) + [`api/task.ts`](./api/) + [`api/project.ts`](./api/)
- **A**：脚手架 + [`components/layout/`](./components/layout/) + [`components/common/`](./components/common/) + [`views/login/`](./views/login/) + [`views/profile/`](./views/profile/) + [`views/error/`](./views/error/) + 全局 utils/styles/router/plugins/composables
- **C**：[`views/artwork/`](./views/artwork/) + [`views/admin/dashboard/`](./views/admin/dashboard/) + [`views/admin/printer/`](./views/admin/printer/) + [`views/admin/material/`](./views/admin/material/) + [`views/admin/member/`](./views/admin/member/) + [`views/admin/log/`](./views/admin/log/) + [`views/admin/artwork/`](./views/admin/artwork/) + 对应 components