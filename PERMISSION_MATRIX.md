# 角色权限矩阵（PERMISSION_MATRIX.md）

> 4 个角色 × 全部页面/接口/操作的权限表。
> 后端做 `@PreAuthorize`、前端做路由守卫（`router meta.roles`），都靠这张表。

---

## 1. 角色定义

| 值 | 名称 | 英文常量 | 角色等级 | 一句话 |
|---|---|:---:|:---:|---|
| 1 | 社长 | `Role.PRESIDENT` | 最高 | 全权限 + 系统日志（**仅社长可见**） |
| 2 | 技术骨干 | `Role.TECH_LEAD` | 高 | 审批任务 + 分配打印机 + 完成打印 |
| 3 | 普通社员 | `Role.MEMBER` | 中 | 申请任务 + 查看项目 + 作品库 |
| 4 | 新成员 | `Role.NEWBIE` | 低 | 受限权限（不能审批 / 不能创建项目） |

> 前端类型定义：[`vue-3d/src/types/member.ts`](vue-3d/src/types/member.ts) `enum Role`
>
> 后端权限路由（meta.roles）：[`vue-3d/src/router/routes.ts`](vue-3d/src/router/routes.ts)

**继承关系**（前端用 `roles: [1, 2]` 数组包含即有权限）：

```
PRESIDENT(1)  ⊃  TECH_LEAD(2)  ⊃  MEMBER(3)  ⊃  NEWBIE(4)
     最高              高              中              低
```

> **注意**：枚举值越小权限越高，路由用 `roles: [1, 2]` 数组控制哪些角色可见。
> 例如 `/admin/member` 的 `roles: [1, 2]`，只有社长 + 技术骨干可见。

**测试账号**（密码全部 `123456`）：

| 学号 | 姓名 | 角色 | 实际权限 |
|---|---|---|---|
| `2023010001` | 张明 | 社长 (1) | 全权限（含系统日志） |
| `2023010002` | 李强 | 技术骨干 (2) | 审批 / 分配 / 完成 |
| `2023010005` | 刘洋 | 普通社员 (3) | 申请 / 作品库 |
| `2024010001` | 马超 | 新成员 (4) | 受限（只能看公开内容） |

---

## 2. 页面访问权限

> ✅ 可以访问 / ❌ 不能访问 / ⚠️ 部分受限（按数据范围）

| 路由 | 页面 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE | 说明 |
|---|:---:|:---:|:---:|:---:|:---:|---|
| `/login` | 登录页 | ✅ | ✅ | ✅ | ✅ | 不需要登录 |
| `/home` | 首页 Dashboard | ✅ | ✅ | ✅ | ✅ | 管理员视角仅 PRESIDENT/TECH_LEAD 可见"管理后台"入口 |
| `/profile` | 个人中心 | ✅ | ✅ | ✅ | ✅ | 只能看自己的 |
| `/task/apply` | 提交打印任务 | ✅ | ✅ | ✅ | ✅ | |
| `/task/my` | 我的任务 | ✅ | ✅ | ✅ | ✅ | 只能看自己的 |
| `/task/queue` | 打印队列（社员端） | ✅ | ✅ | ✅ | ✅ | 公开队列 |
| `/task/:id` | 任务详情 | ✅ | ✅ | ✅ | ✅ | 操作按钮按角色权限 |
| `/project/list` | 项目列表（社员端） | ✅ | ✅ | ✅ | ✅ | Tab 区分"我参与的"和"全部" |
| `/project/create` | 创建项目 | ✅ | ✅ | ✅ | ❌ | |
| `/project/:id` | 项目详情 | ✅ | ✅ | ✅ | ✅ | 编辑按钮按角色 + 是否项目负责人 |
| `/artwork/list` | 作品库 | ✅ | ✅ | ✅ | ✅ | |
| `/artwork/my` | 我的作品 | ✅ | ✅ | ✅ | ✅ | |
| `/artwork/create` | 登记作品 | ✅ | ✅ | ✅ | ✅ | 仅已完结任务可登记 |
| `/artwork/:id` | 作品详情 | ✅ | ✅ | ✅ | ✅ | 公开访问 |
| `/admin/dashboard` | 管理驾驶舱 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/task/pending` | 待审批任务 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/task/active` | 进行中任务 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/task/history` | 历史任务 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/task/stats` | 任务统计 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/project/manage` | 项目管理 | ✅ | ✅ | ❌ | ❌ | TECH_LEAD 只能看自己负责的 |
| `/admin/project/create` | 创建项目 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/material` | 耗材库存 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/material/inbound` | 耗材入库 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/material/log` | 耗材流水 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/printer` | 打印机管理 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/printer/maintenance` | 维护记录 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/member` | 成员管理 | ✅ | ✅ | ❌ | ❌ | 改角色 / 改技能 |
| `/admin/artwork/recommend` | 作品推荐 | ✅ | ✅ | ❌ | ❌ | |
| `/admin/log` | 系统日志 | ✅ | ❌ | ❌ | ❌ | **仅社长可见**（审计） |

---

## 3. 后端接口权限

> ✅ 可调用 / ❌ 拒绝（403） / ⚠️ 按数据所有权判断

### 3.1 认证模块 `/api/auth/*`

| 接口 | 方法 | 说明 |
|---|---|---|
| `/auth/login` | POST | 公开 |
| `/auth/info` | GET | 已登录用户 |

### 3.2 用户模块 `/api/user/*`

| 接口 | 方法 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE |
|---|:---:|:---:|:---:|:---:|:---:|
| `/user/list` | GET | ✅ | ✅ | ❌ | ❌ |
| `/user/{studentId}/role` | PUT | ✅ | ✅ | ❌ | ❌ |
| `/user/{studentId}/skill` | PUT | ✅ | ✅ | ❌ | ❌ |
| `/user/{studentId}/stats` | GET | ✅ | ✅ | ⚠️ 自己 | ⚠️ 自己 |
| `/user/{studentId}` | GET | ✅ | ✅ | ⚠️ 自己 | ⚠️ 自己 |
| `/user/info` | PUT | ✅ | ✅ | ⚠️ 自己 | ⚠️ 自己 |
| `/user/password` | PUT | ✅ | ✅ | ⚠️ 自己 | ⚠️ 自己 |

### 3.3 打印任务模块 `/api/task/*`

| 接口 | 方法 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE |
|---|:---:|:---:|:---:|:---:|:---:|
| `/task` | POST | ✅ | ✅ | ✅ | ✅ |
| `/task/{id}` | GET | ✅ | ✅ | ⚠️ 自己 | ⚠️ 自己 |
| `/task/my` | GET | ✅ | ✅ | ⚠️ 自己 | ⚠️ 自己 |
| `/task/queue` | GET | ✅ | ✅ | ✅ | ✅ |
| `/task/pending` | GET | ✅ | ✅ | ❌ | ❌ |
| `/task/active` | GET | ✅ | ✅ | ❌ | ❌ |
| `/task/history` | GET | ✅ | ✅ | ❌ | ❌ |
| `/task/stats` | GET | ✅ | ✅ | ❌ | ❌ |
| `/task/{id}/approve` | POST | ✅ | ✅ | ❌ | ❌ |
| `/task/{id}/reject` | POST | ✅ | ✅ | ❌ | ❌ |
| `/task/{id}/assign` | POST | ✅ | ✅ | ❌ | ❌ |
| `/task/{id}/start` | POST | ✅ | ✅ | ❌ | ❌ |
| `/task/{id}/finish` | POST | ✅ | ✅ | ❌ | ❌ |
| `/task/{id}/pickup` | POST | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 |
| `/task/{id}/cancel` | POST | ✅ | ✅ | ⚠️ 自己 | ⚠️ 自己 |

### 3.4 项目模块 `/api/project/*`

| 接口 | 方法 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE |
|---|:---:|:---:|:---:|:---:|:---:|
| `/project` | POST | ✅ | ✅ | ✅ | ❌ |
| `/project/list` | GET | ✅ | ✅ | ✅ | ✅ |
| `/project/{id}` | GET | ✅ | ✅ | ✅ | ✅ |
| `/project/{id}` | PUT | ⚠️ 负责人 | ⚠️ 负责人 | ⚠️ 负责人 | ❌ |
| `/project/{id}/complete` | POST | ⚠️ 负责人 | ⚠️ 负责人 | ⚠️ 负责人 | ❌ |
| `/project/{id}/cancel` | POST | ⚠️ 负责人 | ⚠️ 负责人 | ⚠️ 负责人 | ❌ |
| `/project/{id}/member` | POST | ⚠️ 负责人 | ⚠️ 负责人 | ⚠️ 负责人 | ❌ |
| `/project/{id}/member/{mid}` | DELETE | ⚠️ 负责人 | ⚠️ 负责人 | ⚠️ 负责人 | ❌ |
| `/project/{id}/member/{mid}/role` | PUT | ⚠️ 负责人 | ⚠️ 负责人 | ⚠️ 负责人 | ❌ |
| `/project/{id}/stage` | POST | ⚠️ 负责人 | ⚠️ 负责人 | ⚠️ 负责人 | ❌ |
| `/project/{id}/stage/{pid}/status` | PUT | ⚠️ 负责人 | ⚠️ 负责人 | ⚠️ 负责人 | ❌ |

### 3.5 作品库模块 `/api/artwork/*`

| 接口 | 方法 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE |
|---|:---:|:---:|:---:|:---:|:---:|
| `/artwork/list` | GET | ✅ | ✅ | ✅ | ✅ |
| `/artwork/my` | GET | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 |
| `/artwork/recommend` | GET | ✅ | ✅ | ✅ | ✅ |
| `/artwork/{id}` | GET | ✅ | ✅ | ✅ | ✅ |
| `/artwork` | POST | ⚠️ 自己的任务 | ⚠️ 自己的任务 | ⚠️ 自己的任务 | ⚠️ 自己的任务 |
| `/artwork/{id}` | PUT | ⚠️ 作者 | ⚠️ 作者 | ⚠️ 作者 | ⚠️ 作者 |
| `/artwork/{id}` | DELETE | ⚠️ 作者 | ⚠️ 作者 | ⚠️ 作者 | ⚠️ 作者 |
| `/artwork/{id}/recommend` | PUT | ✅ | ✅ | ❌ | ❌ |

### 3.6 耗材模块 `/api/material/*`

| 接口 | 方法 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE |
|---|:---:|:---:|:---:|:---:|:---:|
| `/material/stock` | GET | ✅ | ✅ | ❌ | ❌ |
| `/material/stock/warning` | GET | ✅ | ✅ | ❌ | ❌ |
| `/material/inbound` | POST | ✅ | ✅ | ❌ | ❌ |
| `/material/log` | GET | ✅ | ✅ | ❌ | ❌ |
| `/material/summary` | GET | ✅ | ✅ | ❌ | ❌ |

### 3.7 打印机模块 `/api/printer/*`

| 接口 | 方法 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE |
|---|:---:|:---:|:---:|:---:|:---:|
| `/printer/list` | GET | ✅ | ✅ | ❌ | ❌ |
| `/printer/available` | GET | ✅ | ✅ | ❌ | ❌ |
| `/printer/{id}/status` | PUT | ✅ | ✅ | ❌ | ❌ |
| `/printer/{id}/maintenance` | POST | ✅ | ✅ | ❌ | ❌ |
| `/printer/{id}/maintenance` | GET | ✅ | ✅ | ❌ | ❌ |

### 3.8 统计模块 `/api/stats/*`

| 接口 | 方法 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE |
|---|:---:|:---:|:---:|:---:|:---:|
| `/stats/dashboard` | GET | ✅ | ✅ | ❌ | ❌ |
| `/stats/member-ranking` | GET | ✅ | ✅ | ❌ | ❌ |

### 3.9 日志模块 `/api/log/*` ⚠️ **仅社长可见**

| 接口 | 方法 | PRESIDENT | TECH_LEAD | MEMBER | NEWBIE |
|---|:---:|:---:|:---:|:---:|:---:|
| `/log/list` | GET | ✅ | ❌ | ❌ | ❌ |
| `/log/clean` | DELETE | ✅ | ❌ | ❌ | ❌ |

---

## 4. 数据所有权（按 OWNER 判断）

以下接口对所有非管理员角色**只看自己创建的 / 属于自己的数据**：

| 模块 | 数据 | 过滤字段 |
|---|---|---|
| task | 自己的任务 | `applicant_id = 当前学号` |
| artwork | 自己的作品 | `author_id = 当前学号` |
| project | 自己负责的项目 | `leader_id = 当前学号` 或 `project_member.member_id = 当前学号` |
| member | 自己的统计 | `student_id = 当前学号` |

---

## 5. 关键权限点 ⚠️

| 操作 | 需要角色 | 实现位置 |
|---|---|---|
| 审批任务 | TECH_LEAD+ | `TaskServiceImpl.approve()` + 路由 `meta.roles: [1, 2]` |
| 分配打印机 | TECH_LEAD+ | `TaskServiceImpl.assignPrinter()` + 路由 |
| 完成打印 | TECH_LEAD+ | `TaskServiceImpl.finishPrint()` + 路由 |
| 创建项目 | MEMBER+ | `ProjectServiceImpl.create()` + 路由 `meta.roles: [1, 2, 3]` |
| 改成员角色 | TECH_LEAD+ | `UserController.updateRole()` + 路由 |
| 改成员技能 | TECH_LEAD+ | `UserController.updateSkill()` + 路由 |
| **系统日志** | **PRESIDENT ONLY** | `LogController`（路由 `meta.roles: [1]`） |
| 切换暗色模式 | 任意 | 个人偏好，无角色限制 |

---

## 变更记录

- **v2 (2026-06)**：重写整个角色定义（PRESIDENT/TECH_LEAD/MEMBER/NEWBIE），跟实际项目一致；新增 log 模块的"仅社长"权限；补全 artwork/material/printer/stats 的接口权限
- v1：旧版（MEMBER/STAFF/LEADER/ADMIN）— 已废弃

