# 前端（Vue 3 + Vite + TypeScript + Element Plus + Pinia）

3D 打印科创会管理系统的前端。

## 技术栈

- **Vue 3.5** + `<script setup>` 组合式 API
- **Vite 5**（Rolldown 实验性）
- **TypeScript 5**
- **Element Plus 2.14**（UI 组件库）
- **Pinia 2**（状态管理 + 持久化）
- **Vue Router 4**（路由）
- **Axios**（HTTP）
- **ECharts 5**（统计图表）
- **Sass**（样式）

## 快速启动

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 5173 端口）
npm run dev

# 类型检查 + 生产构建
npm run build

# 预览生产包
npm run preview
```

## 环境变量

- `.env.development` — 开发环境（VITE_API_TARGET 指向 `http://localhost:8080`）
- `.env.production` — 生产环境（VITE_API_TARGET 留空，部署时填入）

> `.env.local` 已被 `.gitignore` 屏蔽，**不要把任何含真实密钥的环境变量 commit 进来**。

## 目录约定

```
src/
├─ api/          # 接口封装（按模块拆文件）
├─ stores/       # Pinia 状态（按模块拆文件）
├─ types/        # TS 类型（按模块拆文件）
├─ utils/        # 工具函数（request / format / storage / auth）
├─ components/   # 组件
│  ├─ common/    # 通用组件（StatusTag / PageHeader / EmptyState）
│  ├─ layout/    # 布局组件（AppHeader / AppSidebar / AppMain）
│  └─ <module>/  # 业务组件
├─ views/        # 页面（按模块拆目录）
│  ├─ auth/      # 登录
│  ├─ home/      # 首页
│  ├─ task/      # 任务（社员端）
│  ├─ project/   # 项目（社员端）
│  └─ admin/     # 管理端
│     ├─ task/
│     └─ project/
├─ router/       # 路由
└─ styles/       # 全局样式
```

## 角色路由

| 角色 | 可见路由前缀 |
|---|---|
| 普通社员 | `/task/*` `/project/*` |
| 技术骨干 | `/task/*` `/project/*` `/admin/task/*` `/admin/project/*` |
| 社长 | 全部 |
| 新社员 | `/task/*`（只读） |

## 与后端联调

- 后端接口根：`http://localhost:8080`
- 前端 dev server 代理：`/` → 后端（见 `vite.config.ts`）
- 请求头自动注入 `Authorization: Bearer <token>`（`utils/request.ts` 拦截器）
- 401 自动跳转登录页
- 业务异常（`code != 200`）弹 ElMessage 错误提示

## 状态管理

```ts
// 用法示例
import { useTaskStore } from '@/stores/task'

const taskStore = useTaskStore()
await taskStore.fetchMyTasks({ page: 1, size: 20 })
console.log(taskStore.myTasks?.list)
```

每个 store 都暴露：

- `state`（ref）
- `actions`（async function，调用 API）
- `getters`（computed）
