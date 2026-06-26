# 3D打印科创会管理系统 - 前端开发大纲

> **小组**：3 人前端 + 3 人后端（前后端分离）
> **周期**：8 周（与 v2 排期对齐）
> **基线**：[v2 需求规格说明书](./requirements_v2.md)
> **数据**：全部模拟数据，无真实信息

---

## 〇、TL;DR（一分钟看完）

- 前端一共 **6 大模块、约 35 个页面**
- 分 **5 个阶段**开发，每阶段结束都有可演示的产出
- 3 人分工：1 人负责**基础架构 + 公共组件 + 用户**，1 人负责**核心业务（打印任务 + 项目）**，1 人负责**辅助业务（作品库 + 设备 + 统计 + 日志）**
- 测试：**Vitest 单元测试 + 接口联调 + Playwright E2E + 手动验收清单**
- 协作核心：**接口契约先行**——前端先按 mock 写，后端按契约实现，第 4 周开始联调

---

## 一、前端模块全景图

```
┌─────────────────────────────────────────────────────────────┐
│                     【模块 M1】用户与权限                      │
│  M1.1 登录页    M1.2 路由守卫    M1.3 个人中心    M1.4 修改密码  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   【模块 M2】打印任务（核心）                  │
│  ┌────────────────────────────────────────────────────┐    │
│  │  社员端                                              │    │
│  │   M2.1 提交申请    M2.2 我的任务    M2.3 打印队列     │    │
│  │   M2.4 任务详情    M2.5 取件签到                     │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  管理端（技术骨干+社长）                              │    │
│  │   M2.6 待审批列表   M2.7 审批详情  M2.8 进行中任务    │    │
│  │   M2.9 历史任务     M2.10 任务统计                  │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     【模块 M3】项目管理                        │
│  ┌────────────────────────────────────────────────────┐    │
│  │  社员端                                              │    │
│  │   M3.1 项目列表    M3.2 项目详情  M3.3 我的项目      │    │
│  │   M3.4 申请加入                                     │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  管理端                                              │    │
│  │   M3.5 创建项目    M3.6 项目管理列表                 │    │
│  │   M3.7 成员管理    M3.8 阶段管理  M3.9 文件管理       │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     【模块 M4】作品库                          │
│  M4.1 作品列表    M4.2 作品详情    M4.3 我的作品               │
│  M4.4 推荐案例管理（管理端）                                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  【模块 M5】设备耗材（管理端）                  │
│  ┌────────────────────────────────────────────────────┐    │
│  │  设备                                                │    │
│  │   M5.1 打印机台账   M5.2 维护记录   M5.3 状态变更      │    │
│  ├────────────────────────────────────────────────────┤    │
│  │  耗材                                                │    │
│  │   M5.4 库存列表     M5.5 入库操作   M5.6 操作记录      │    │
│  │   M5.7 库存预警                                      │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  【模块 M6】统计与日志（管理端）                │
│  M6.1 统计看板    M6.2 成员管理    M6.3 系统日志                │
└─────────────────────────────────────────────────────────────┘
```

---

## 二、模块详细说明

> **约定**：每个模块标注
> - **页面**：路由 + 组件
> - **状态**：Pinia store
> - **接口**：调用的后端 API
> - **权限**：谁可以访问
> - **依赖**：被谁依赖

---

### M1 用户与权限（基础模块，**最先做**）

#### M1.1 登录页

| 项目 | 内容 |
|---|---|
| 路由 | `/login` |
| 组件 | `Login.vue`、`LoginForm.vue` |
| 状态 | `useAuthStore`（token、userInfo） |
| 接口 | `POST /api/auth/login` |
| 权限 | 公开 |
| 依赖 | 无 |
| 备注 | **前端 BCrypt 解密？不需要**——前端只管发明文密码，后端 BCrypt 校验 |

#### M1.2 路由守卫

| 项目 | 内容 |
|---|---|
| 文件 | `src/router/guard.ts` |
| 功能 | 未登录访问 → 重定向 `/login`；无权限访问 → 重定向 `/403`；角色不匹配 → 重定向首页 |
| 依赖 | M1.1 |

#### M1.3 个人中心

| 项目 | 内容 |
|---|---|
| 路由 | `/profile` |
| 组件 | `Profile.vue`、`ProfileInfo.vue`、`SkillBadge.vue`（技能等级进度条） |
| 状态 | `useUserStore` |
| 接口 | `GET /api/user/info`、`GET /api/user/{id}/stats` |
| 权限 | 登录用户 |
| 依赖 | M1.1 |

#### M1.4 修改密码

| 项目 | 内容 |
|---|---|
| 路由 | `/profile/password`（抽屉或弹窗） |
| 接口 | `PUT /api/user/password` |
| 权限 | 登录用户 |
| 依赖 | M1.1 |

---

### M2 打印任务（**核心模块**，第 3 周主线）

#### 社员端

| ID | 页面 | 路由 | 组件 | 接口 |
|---|---|---|---|---|
| M2.1 | 提交申请 | `/task/new` | `TaskApply.vue`、`StlUploader.vue`、`ParamForm.vue`、`PrioritySelect.vue` | `POST /api/task`、`POST /api/file/upload` |
| M2.2 | 我的任务 | `/task/my` | `TaskList.vue`、`TaskStatusTag.vue`（状态徽章）、`TaskFilter.vue` | `GET /api/task/my?status=&page=&size=` |
| M2.3 | 打印队列 | `/task/queue` | `TaskQueue.vue`（v2 排序：priority → apply_time） | `GET /api/task/queue` |
| M2.4 | 任务详情 | `/task/:id` | `TaskDetail.vue`、`TaskTimeline.vue`（状态流转时间线）、`CancelButton.vue` | `GET /api/task/{id}` |
| M2.5 | 取件签到 | `/task/:id` 内嵌 | `PickupButton.vue`、`QualityRate.vue`（1-5星评分） | `PUT /api/task/{id}/pickup` |

#### 管理端

| ID | 页面 | 路由 | 组件 | 接口 |
|---|---|---|---|---|
| M2.6 | 待审批列表 | `/admin/task/pending` | `PendingTaskList.vue`、`StlPreview.vue`（简易 STL 预览） | `GET /api/task/pending` |
| M2.7 | 审批详情 | `/admin/task/pending/:id` | `ApprovePanel.vue`、`RejectDialog.vue`（含原因）、`Stl3DViewer.vue`（three.js 预览，可选） | `PUT /api/task/{id}/approve`、`PUT /api/task/{id}/reject` |
| M2.8 | 进行中任务 | `/admin/task/active` | `ActiveTaskList.vue`、`AssignPrinterDialog.vue`、`StartPrintDialog.vue`、`FinishPrintDialog.vue`（填实际耗材/耗时/质量分） | `PUT /api/task/{id}/assign`、`PUT /api/task/{id}/start`、`PUT /api/task/{id}/finish` |
| M2.9 | 历史任务 | `/admin/task/history` | `HistoryTaskList.vue`（按时间/状态/打印机筛选） | `GET /api/task/my`（复用，但权限过滤） |
| M2.10 | 任务统计 | `/admin/task/stats` | `TaskStatsCards.vue`（本月任务数/通过率/平均耗时） | `GET /api/task/stats` |

**M2 涉及的 Pinia stores**：

```typescript
// src/stores/task.ts
export const useTaskStore = defineStore('task', {
  state: () => ({
    myTasks: [] as Task[],
    pendingTasks: [] as Task[],
    activeTasks: [] as Task[],
    queue: [] as Task[],
    currentTask: null as Task | null,
  }),
  actions: {
    fetchMyTasks() { /* ... */ },
    fetchPendingTasks() { /* ... */ },
    submitTask(payload: TaskApplyDTO) { /* ... */ },
    approveTask(id: string) { /* ... */ },
    // ...
  },
})
```

---

### M3 项目管理（第 4 周主线）

#### 社员端

| ID | 页面 | 路由 | 组件 | 接口 |
|---|---|---|---|---|
| M3.1 | 项目列表 | `/project` | `ProjectList.vue`（Tab：我参与的 / 全部）、`ProjectCard.vue` | `GET /api/project/list?scope=mine|all` |
| M3.2 | 项目详情 | `/project/:id` | `ProjectDetail.vue`、`MemberList.vue`、`ProgressTimeline.vue`、`FileList.vue`、`RelatedTasks.vue`（关联的打印任务） | `GET /api/project/{id}` |
| M3.3 | 我的项目 | `/project?scope=mine`（复用 M3.1） | 同 M3.1 | 同 M3.1 |
| M3.4 | 申请加入 | `/project/:id` 内嵌弹窗 | `JoinProjectDialog.vue`（填申请理由） | 内部接口：`POST /api/project/{id}/apply`（**需要后端补一个申请接口**） |

#### 管理端

| ID | 页面 | 路由 | 组件 | 接口 |
|---|---|---|---|---|
| M3.5 | 创建项目 | `/admin/project/new` | `ProjectForm.vue`、`CoverUploader.vue`、`StageEditor.vue`（动态增删阶段） | `POST /api/project` |
| M3.6 | 项目管理列表 | `/admin/project` | `ProjectManageList.vue` | `GET /api/project/list` |
| M3.7 | 成员管理 | `/admin/project/:id/members` | `MemberManage.vue`、`AddMemberDialog.vue`（搜索成员）、`RoleSelect.vue` | `POST /api/project/{id}/member`、`DELETE /api/project/{id}/member/{mid}` |
| M3.8 | 阶段管理 | `/admin/project/:id/progress` | `StageManage.vue`、`StageStatusSelect.vue` | `POST /api/project/{id}/progress`、`PUT /api/project/{id}/progress/{pid}` |
| M3.9 | 文件管理 | `/admin/project/:id/files` | `FileUploader.vue`、`FileList.vue` | `POST /api/project/{id}/file` |

**M3 Pinia store**：`useProjectStore`（项目列表、当前项目、成员列表、阶段列表、文件列表）

---

### M4 作品库（第 5 周）

| ID | 页面 | 路由 | 组件 | 接口 |
|---|---|---|---|---|
| M4.1 | 作品列表 | `/artwork` | `ArtworkList.vue`、`ArtworkFilter.vue`（按材料/推荐筛选）、`ArtworkCard.vue` | `GET /api/artwork/list?material=&recommended=` |
| M4.2 | 作品详情 | `/artwork/:id` | `ArtworkDetail.vue`、`PhotoGallery.vue`、`PrintParamsCard.vue`（**关联查询 print_task**）、`ExperienceContent.vue`（富文本） | `GET /api/artwork/{id}`、`PUT /api/artwork/{id}/view` |
| M4.3 | 我的作品 | `/artwork?scope=mine` | 复用 M4.1 | `GET /api/artwork/my` |
| M4.4 | 推荐管理 | `/admin/artwork/recommend` | `RecommendList.vue` | `PUT /api/artwork/{id}/recommend` |

**M4 Pinia store**：`useArtworkStore`

---

### M5 设备耗材（第 5 周）

#### 设备

| ID | 页面 | 路由 | 组件 | 接口 |
|---|---|---|---|---|
| M5.1 | 打印机台账 | `/admin/printer` | `PrinterList.vue`、`PrinterStatusTag.vue`、`CurrentTaskBadge.vue` | `GET /api/printer/list` |
| M5.2 | 维护记录 | `/admin/printer/:id/maintenance` | `MaintenanceList.vue`、`MaintenanceForm.vue` | `POST /api/printer/{id}/maint` |
| M5.3 | 状态变更 | `/admin/printer` 内嵌 | `StatusChangeDialog.vue` | `PUT /api/printer/{id}/status` |

#### 耗材

| ID | 页面 | 路由 | 组件 | 接口 |
|---|---|---|---|---|
| M5.4 | 库存列表 | `/admin/material` | `MaterialStockList.vue`、`StockWarningTag.vue` | `GET /api/material/stock` |
| M5.5 | 入库操作 | `/admin/material/inbound` | `InboundForm.vue` | `POST /api/material/inbound` |
| M5.6 | 操作记录 | `/admin/material/log` | `MaterialLogList.vue` | `GET /api/material/log?material=&color=&type=` |
| M5.7 | 库存预警 | `/admin/material?filter=warning` | 复用 M5.4 | 同上 |

---

### M6 统计与日志（第 6 周）

| ID | 页面 | 路由 | 组件 | 接口 |
|---|---|---|---|---|
| M6.1 | 统计看板 | `/admin/dashboard` | `DashboardCards.vue`（4 个数据卡片）、`TaskTrendChart.vue`（echarts 折线）、`MaterialPieChart.vue`（echarts 饼图）、`MemberRankChart.vue`（echarts 柱状）、`ProjectGauge.vue`（echarts 仪表盘）、`WarningList.vue`（库存预警） | `GET /api/stats/dashboard` |
| M6.2 | 成员管理 | `/admin/member` | `MemberList.vue`、`RoleEditDialog.vue`、`SkillEditDialog.vue` | `GET /api/user/list`、`PUT /api/user/{id}/role`、`PUT /api/user/{id}/skill` |
| M6.3 | 系统日志 | `/admin/log` | `LogList.vue`、`LogFilter.vue`（按操作人/类型/时间筛选）、分页 | `GET /api/log/list?user=&operation=&page=&size=` |

---

> **本文件为精简版大纲**。详细目录结构、命名规范、测试策略、3 人分工、风险对策
> 都在本仓库 `vue-3d/README.md` 里有引申说明，组员 clone 后可对照执行。
