# 角色权限矩阵（PERMISSION_MATRIX.md）

> 4 个角色 × 全部页面/接口/操作的权限表。
> 后端做 `@PreAuthorize`、前端做路由守卫，都靠这张表。

---

## 1. 角色定义

| 代码 | 中文 | 英文常量 | 一句话 |
|---|---|---|---|
| `MEMBER` | 普通社员 | `Role.MEMBER = 1` | 提交任务、查自己、看项目 |
| `STAFF` | 技术骨干 | `Role.STAFF = 2` | + 审批任务、分配打印机 |
| `LEADER` | 项目负责人 | `Role.LEADER = 3` | + 创建/编辑/完成/取消项目 |
| `ADMIN` | 系统管理员 | `Role.ADMIN = 9` | + 用户管理、打印机管理、库存管理 |

**权限继承关系**：

```
MEMBER (1) ⊂ STAFF (2) ⊂ LEADER (3) ⊂ ADMIN (9)
```

ADMIN 拥有所有权限（除了不能跨权限绕过审计日志）。

---

## 2. 页面访问权限

> ✅ 可以访问 / ❌ 不能访问 / ⚠️ 部分受限（按数据范围）

| 路由 | 页面 | MEMBER | STAFF | LEADER | ADMIN | 说明 |
|---|---|:---:|:---:|:---:|:---:|---|
| `/login` | 登录页 | ✅ | ✅ | ✅ | ✅ | 不需要登录 |
| `/403` | 无权限页 | ✅ | ✅ | ✅ | ✅ | 任意角色 |
| `/404` | 404 页 | ✅ | ✅ | ✅ | ✅ | 任意角色 |
| `/home` | 首页 Dashboard | ✅ | ✅ | ✅ | ✅ | 内容按角色不同 |
| `/profile` | 个人中心 | ✅ | ✅ | ✅ | ✅ | 只能看自己的 |
| `/task/apply` | 提交打印任务 | ✅ | ✅ | ✅ | ✅ | |
| `/task/queue` | 打印队列（社员端） | ✅ | ✅ | ✅ | ✅ | 只看公开队列 |
| `/task/my` | 我的任务 | ✅ | ✅ | ✅ | ✅ | 只能看自己的 |
| `/task/:id` | 任务详情 | ✅ | ✅ | ✅ | ✅ | 操作按钮按角色权限 |
| `/project/list` | 项目列表（社员端） | ✅ | ✅ | ✅ | ✅ | Tab 区分"我参与的"和"全部" |
| `/project/create` | 创建项目 | ❌ | ❌ | ✅ | ✅ | |
| `/project/:id` | 项目详情 | ✅ | ✅ | ✅ | ✅ | 编辑按钮按角色 + 是否项目负责人 |
| `/admin/dashboard` | 管理驾驶舱 | ❌ | ⚠️ | ⚠️ | ✅ | STAFF/LEADER 看自己负责的，ADMIN 看全部 |
| `/admin/task/pending` | 待审批任务 | ❌ | ✅ | ✅ | ✅ | |
| `/admin/task/active` | 进行中任务 | ❌ | ✅ | ✅ | ✅ | |
| `/admin/task/history` | 历史任务 | ❌ | ✅ | ✅ | ✅ | |
| `/admin/project/manage` | 项目管理 | ❌ | ⚠️ | ✅ | ✅ | STAFF 只能看自己参与的 |
| `/admin/material` | 材料库存 | ❌ | ⚠️ | ⚠️ | ✅ | STAFF/LEADER 只读 |
| `/admin/material/log` | 材料消耗日志 | ❌ | ⚠️ | ⚠️ | ✅ | 同上 |
| `/admin/printer` | 打印机管理 | ❌ | ⚠️ | ⚠️ | ✅ | STAFF/LEADER 只读 |
| `/admin/member` | 成员管理 | ❌ | ❌ | ❌ | ✅ | |
| `/admin/log/login` | 登录日志 | ❌ | ❌ | ❌ | ✅ | |
| `/admin/log/operation` | 操作日志 | ❌ | ❌ | ❌ | ✅ | |

---

## 3. 后端接口权限（按模块）

> ✅ 可调用 / ❌ 拒绝（403） / ⚠️ 按数据所有权判断

### 3.1 认证模块 `/api/auth/*`

| 接口 | 方法 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/auth/login` | POST | ✅ | ✅ | ✅ | ✅ |
| `/auth/logout` | POST | ✅ | ✅ | ✅ | ✅ |
| `/auth/info` | GET | ✅ | ✅ | ✅ | ✅ |
| `/auth/password` | PUT | ✅ | ✅ | ✅ | ✅ |

### 3.2 用户模块 `/api/user/*`

| 接口 | 方法 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/user/info` | GET | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ✅ |
| `/user/info` | POST | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ✅ |
| `/user/password` | PUT | ✅ | ✅ | ✅ | ✅ |
| `/user/list` | GET | ✅ | ✅ | ✅ | ✅ |
| `/user/role` | PUT | ❌ | ❌ | ❌ | ✅ |
| `/user/skill` | PUT | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ✅ |
| `/user/stats` | GET | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ✅ |
| `/user/{id}/disable` | POST | ❌ | ❌ | ❌ | ✅ |
| `/user/{id}/enable` | POST | ❌ | ❌ | ❌ | ✅ |

### 3.3 打印任务模块 `/api/task/*`

| 接口 | 方法 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/task` | POST（提交） | ✅ | ✅ | ✅ | ✅ |
| `/task/{id}` | GET | ✅ | ✅ | ✅ | ✅ |
| `/task/my` | GET | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ✅ |
| `/task/queue` | GET | ✅ | ✅ | ✅ | ✅ |
| `/task/pending` | GET | ❌ | ✅ | ✅ | ✅ |
| `/task/stats` | GET | ⚠️ 自己 | ✅ | ✅ | ✅ |
| `/task/{id}/approve` | POST | ❌ | ✅ | ✅ | ✅ |
| `/task/{id}/reject` | POST | ❌ | ✅ | ✅ | ✅ |
| `/task/{id}/cancel` | POST | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ✅ |
| `/task/{id}/pickup` | POST | ⚠️ 自己 | ⚠️ 自己 | ⚠️ 自己 | ✅ |
| `/task/{id}/assign` | POST | ❌ | ✅ | ✅ | ✅ |
| `/task/{id}/start` | POST | ❌ | ✅ | ✅ | ✅ |
| `/task/{id}/finish` | POST | ❌ | ✅ | ✅ | ✅ |

### 3.4 项目模块 `/api/project/*`

| 接口 | 方法 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/project` | POST（创建） | ❌ | ❌ | ✅ | ✅ |
| `/project/{id}` | GET | ✅ | ✅ | ✅ | ✅ |
| `/project/{id}` | PUT | ❌ | ❌ | ⚠️ 项目负责人 | ✅ |
| `/project/{id}` | DELETE | ❌ | ❌ | ⚠️ 项目负责人 | ✅ |
| `/project/{id}/complete` | POST | ❌ | ❌ | ⚠️ 项目负责人 | ✅ |
| `/project/{id}/cancel` | POST | ❌ | ❌ | ⚠️ 项目负责人 | ✅ |
| `/project/list` | GET | ✅ | ✅ | ✅ | ✅ |
| `/project/{id}/member` | POST | ❌ | ❌ | ⚠️ 项目负责人 | ✅ |
| `/project/{id}/member/{mid}` | DELETE | ❌ | ❌ | ⚠️ 项目负责人 | ✅ |
| `/project/{id}/stage` | POST | ❌ | ⚠️ 核心成员 | ⚠️ 项目负责人 | ✅ |
| `/project/{id}/stage/{sid}` | PUT | ❌ | ⚠️ 核心成员 | ⚠️ 项目负责人 | ✅ |
| `/project/{id}/stage/{sid}` | DELETE | ❌ | ⚠️ 核心成员 | ⚠️ 项目负责人 | ✅ |
| `/project/{id}/file/upload` | POST | ✅ | ✅ | ✅ | ✅ |
| `/project/{id}/file/{fid}` | DELETE | ⚠️ 自己上传的 | ⚠️ 自己上传的 | ⚠️ 项目负责人 | ✅ |

### 3.5 打印机模块 `/api/printer/*`

| 接口 | 方法 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/printer/list` | GET | ✅ | ✅ | ✅ | ✅ |
| `/printer/{id}` | GET | ✅ | ✅ | ✅ | ✅ |
| `/printer/{id}/available` | GET | ❌ | ✅ | ✅ | ✅ |
| `/printer` | POST | ❌ | ❌ | ❌ | ✅ |
| `/printer/{id}` | PUT | ❌ | ❌ | ❌ | ✅ |
| `/printer/{id}/maintain` | POST | ❌ | ❌ | ❌ | ✅ |

### 3.6 材料模块 `/api/material/*`

| 接口 | 方法 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/material/list` | GET | ✅ | ✅ | ✅ | ✅ |
| `/material/{id}` | GET | ✅ | ✅ | ✅ | ✅ |
| `/material/warning` | GET | ❌ | ✅ | ✅ | ✅ |
| `/material` | POST | ❌ | ❌ | ❌ | ✅ |
| `/material/{id}` | PUT | ❌ | ❌ | ❌ | ✅ |
| `/material/{id}/restock` | POST | ❌ | ❌ | ❌ | ✅ |
| `/material/log` | GET | ❌ | ✅ | ✅ | ✅ |

### 3.7 文件模块 `/api/file/*`

| 接口 | 方法 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/file/upload` | POST | ✅ | ✅ | ✅ | ✅ |
| `/file/download/**` | GET | ⚠️ 关联项目的成员 | ⚠️ 关联项目的成员 | ✅ | ✅ |
| `/file/**` | DELETE | ⚠️ 自己上传的 | ⚠️ 自己上传的 | ✅ | ✅ |

### 3.8 日志模块 `/api/log/*`

| 接口 | 方法 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|:---:|:---:|:---:|:---:|
| `/log/login` | GET | ❌ | ❌ | ❌ | ✅ |
| `/log/operation` | GET | ❌ | ❌ | ❌ | ✅ |

---

## 4. 操作权限（按钮级）

> 详情页/列表页里的"小按钮"按这个表判断。

### 4.1 任务详情页 `/task/:id`

| 按钮 | 显示条件 |
|---|---|
| 「取件签到」 | 当前用户 = 申请人 AND 任务状态 = 已完成(6) |
| 「取消任务」 | 当前用户 = 申请人 AND 任务状态 ∈ {待审批, 已通过, 排队中} |
| 「审批通过」 | 当前用户 ∈ {STAFF, LEADER, ADMIN} AND 任务状态 = 待审批 |
| 「驳回」 | 同上 |
| 「分配打印机」 | 当前用户 ∈ {STAFF, LEADER, ADMIN} AND 任务状态 = 已通过 |
| 「开始打印」 | 当前用户 ∈ {STAFF, LEADER, ADMIN} AND 任务状态 = 排队中 AND 已分配打印机 |
| 「标记完成」 | 当前用户 ∈ {STAFF, LEADER, ADMIN} AND 任务状态 = 打印中 |

### 4.2 项目详情页 `/project/:id`

| 按钮 | 显示条件 |
|---|---|
| 「编辑项目」 | 当前用户 = 项目负责人 OR ADMIN |
| 「标记完成」 | 当前用户 = 项目负责人 AND 项目状态 = 进行中 |
| 「取消项目」 | 当前用户 = 项目负责人 AND 项目状态 ∈ {筹备中, 进行中} |
| 「添加成员」 | 当前用户 = 项目负责人 OR 项目内角色 = 核心成员 |
| 「移除成员」 | 当前用户 = 项目负责人 |
| 「编辑阶段」 | 当前用户 = 项目负责人 OR 项目内角色 = 核心成员 |
| 「删除文件」 | 当前用户 = 项目负责人 OR 上传人 = 自己 |

### 4.3 用户列表 `/admin/member`

| 按钮 | 显示条件 |
|---|---|
| 「修改角色」 | 当前用户 = ADMIN |
| 「禁用/启用」 | 当前用户 = ADMIN AND 目标 ≠ 自己的最后一名 ADMIN |

---

## 5. 数据范围权限

> 即使有权限读，某些数据也只能看自己的或自己负责的。

### 5.1 任务数据

| 接口 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|---|---|---|
| `/task/my` | 仅自己 | 仅自己 | 仅自己 | 全部 |
| `/task/pending` | 无权 | 全部 | 全部 | 全部 |
| `/task/queue` | 全部 | 全部 | 全部 | 全部 |
| `/task/stats` | 仅自己 | 全部 | 全部 | 全部 |

### 5.2 项目数据

| 接口 | MEMBER | STAFF | LEADER | ADMIN |
|---|---|---|---|---|
| `/project/list?scope=mine` | 仅参与的 | 仅参与的 | 仅参与的 | 全部 |
| `/project/list?scope=all` | 全部 | 全部 | 全部 | 全部 |
| `/project/{id}` | 看参与的 + 公开项目 | 同左 | 同左 | 全部 |

### 5.3 统计 Dashboard

| 角色 | 数据范围 |
|---|---|
| MEMBER | 仅自己的任务数、材料消耗 |
| STAFF | + 自己审批的任务统计、打印机使用率 |
| LEADER | + 自己负责的项目的进度 |
| ADMIN | 全局统计 + 各成员对比 + 趋势 |

---

## 6. 后端实现要点

### 6.1 Spring Security 注解

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/user/{id}")
public Result<Void> deleteUser(@PathVariable Long id) { ... }

@PreAuthorize("hasAnyRole('STAFF', 'LEADER', 'ADMIN')")
@PostMapping("/task/{id}/approve")
public Result<Void> approve(@PathVariable String id) { ... }

// 自定义权限（项目负责人）
@PreAuthorize("@projectAuth.isLeader(#id, authentication.principal.studentId)")
@PutMapping("/project/{id}")
public Result<Void> update(@PathVariable Long id, @RequestBody ProjectDTO dto) { ... }
```

### 6.2 角色常量（Java）

```java
public enum Role {
    MEMBER(1, "普通社员"),
    STAFF(2, "技术骨干"),
    LEADER(3, "项目负责人"),
    ADMIN(9, "系统管理员");

    private final int code;
    private final String label;

    // getter...
}
```

### 6.3 前端路由守卫

```ts
// src/router/index.ts
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 不需要登录的页面
  if (to.meta.public) return next()

  // 没登录
  if (!authStore.token) return next('/login')

  // 角色不够
  const requiredRoles = to.meta.roles as Role[] | undefined
  if (requiredRoles && !requiredRoles.includes(authStore.user!.role)) {
    ElMessage.error('无权访问此页面')
    return next('/403')
  }

  next()
})
```

```ts
// 路由 meta 示例
{
  path: '/admin/member',
  component: () => import('@/views/admin/member/index.vue'),
  meta: {
    title: '成员管理',
    requiresAuth: true,
    roles: [Role.ADMIN],  // 只有管理员能进
  },
}
```

---

## 7. 权限边界场景

### 7.1 跨用户操作

| 场景 | 规则 |
|---|---|
| 用户 A 想取消用户 B 的任务 | ❌ 不允许（除非 ADMIN） |
| 用户 A 想审批用户 B 的任务 | ✅ 允许（STAFF+） |
| 用户 A 想加入用户 B 的项目 | ❌ 不允许，必须项目负责人邀请 |
| 用户 A 想下载用户 B 上传的文件 | ⚠️ 仅当 A 是项目成员 |

### 7.2 自我权限降级

> ADMIN 不能把自己降为非 ADMIN（防误操作把自己锁死）

```java
@PreAuthorize("#id != authentication.principal.id or hasRole('ADMIN')")
@PostMapping("/user/{id}/disable")
public Result<Void> disable(@PathVariable Long id) { ... }
```

### 7.3 最后一名管理员

> 系统必须有至少 1 名 ADMIN，否则禁止禁用/降级

```java
@PostMapping("/user/{id}/disable")
public Result<Void> disable(@PathVariable Long id) {
    User target = userService.getById(id);
    if (target.getRole() == Role.ADMIN) {
        long adminCount = userService.countAdmin();
        if (adminCount <= 1) {
            throw new BizException("系统至少保留 1 名管理员");
        }
    }
    userService.disable(id);
    return Result.ok();
}
```

---

## 8. 审计日志规则

### 8.1 必须记录的操作

| 操作 | 记录字段 |
|---|---|
| 登录/登出 | member_id, ip, user_agent, status |
| 修改用户角色 | operator_id, target_id, old_role, new_role |
| 禁用/启用账号 | operator_id, target_id, action |
| 审批/驳回任务 | operator_id, task_id, action, remark |
| 创建/完成/取消项目 | operator_id, project_id, action |
| 修改材料库存 | operator_id, material_id, old_stock, new_stock |
| 修改打印机状态 | operator_id, printer_id, old_status, new_status |
| 删除任务/项目/文件 | operator_id, target_type, target_id, reason |

### 8.2 日志保留期

- 登录日志：1 年
- 操作日志：3 年
- 任务时间线：永久（业务核心）

---

## 9. 权限矩阵速查（精简版）

```text
                 MEMBER  STAFF  LEADER  ADMIN
登录/登出           ✅     ✅     ✅     ✅
个人中心            ✅     ✅     ✅     ✅
提交打印任务         ✅     ✅     ✅     ✅
查看队列            ✅     ✅     ✅     ✅
查看自己的任务        ✅     ✅     ✅     ✅
审批/驳回任务        ❌     ✅     ✅     ✅
分配打印机           ❌     ✅     ✅     ✅
创建项目            ❌     ❌     ✅     ✅
编辑自己的项目        ❌     ❌     ✅     ✅
完成/取消项目        ❌     ❌     ✅     ✅
编辑项目阶段         ❌     ⚠️    ⚠️     ✅
查看材料库存         ✅     ✅     ✅     ✅
补充材料库存         ❌     ❌     ❌     ✅
查看打印机           ✅     ✅     ✅     ✅
添加/修改打印机      ❌     ❌     ❌     ✅
成员管理            ❌     ❌     ❌     ✅
修改用户角色         ❌     ❌     ❌     ✅
查看登录日志         ❌     ❌     ❌     ✅
查看操作日志         ❌     ❌     ❌     ✅
```

> ⚠️ = 部分受限（按数据所有权 + 项目内角色）

---

## 10. 权限变更流程

### 10.1 修改本文件

1. 在群里发起 RFC（Request for Comments）
2. 24 小时内无人反对 → 通过
3. 同步更新：
   - 本文件
   - 后端 `SecurityConfig` + `@PreAuthorize` 注解
   - 前端 `router/index.ts` 的 `meta.roles`
4. 提交 PR，标注 `chore(permission)`

### 10.2 紧急权限变更

> 例如发现一个权限漏洞，必须立刻关掉

1. 组长在 `main` 分支直接 hotfix
2. 后端优先关掉接口（加 `@PreAuthorize`）
3. 前端隐藏入口（删除路由或加 `v-if`）
4. 事后补 PR 和文档

---

**最后更新**：项目启动时定稿**
