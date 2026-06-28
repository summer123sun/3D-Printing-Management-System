# CHANGELOG

> 3D 打印科创会管理系统 — 变更日志
> 记录每次修复 / 重构 / 新功能的时间、内容、影响范围

---

## 2026-06-28 · v2 重构批次：复用现有代码，零硬编码数字

本轮按用户全局规则"**写代码前先审查复用，避免造轮子，只写必要代码**"对整个项目做了一遍系统性重构。**没有新增任何业务功能**——只是把现有散乱的实现合并 / 类型化。

### ① 后端：抽 MemberMapper.selectIdNameMap() 公共方法（5 处 → 1 处）

**问题**：5 个 service 都用 `memberMapper.selectBatchIds(ids)` 批量查 member 翻译姓名——逻辑一模一样，复制粘贴了 5 份。

**改动**：
- `MemberMapper.java` 加 `default Map<String,String> selectIdNameMap(Collection<String> ids)` 方法（MyBatis-Plus mapper interface 支持 default 方法）
- 5 个 service 改用这个公共方法：
  - `TaskServiceImpl.fillTaskRelationalNames`
  - `ArtworkServiceImpl.fillArtworkAuthorNames`
  - `ProjectServiceImpl.fillMemberNames`
  - `ProjectServiceImpl.fillStageResponsibleNames`
  - `MaterialServiceImpl.fillOperatorNames`
  - `StatsServiceImpl`（行 191 直接 selectBatchIds 调用）

**净删代码**：~30 行重复 selectBatchIds 循环。

---

### ② 后端：抽 TaskMapper.selectBusyPrinterIds() 公共方法（2 处 → 1 处）

**问题**：`STATUS_QUEUED + STATUS_PRINTING` 查"忙打印机"逻辑在 2 处复制：
- `TaskServiceImpl.assignPrinter` 占用检查（用 selectCount）
- `PrinterServiceImpl.availablePrinters` 下拉过滤（用 selectObjs）

**改动**：
- `TaskMapper.java` 加 `default List<String> selectBusyPrinterIds()` 方法
- 2 处调用方都改成 `selectBusyPrinterIds()` + `.contains(dto.getPrinterId())` 判断

**净删代码**：~10 行重复查询。

---

### ③ 前端：42 处硬编码状态数字改 enum（**风险最高**）

**问题**：业务代码到处写 `status === 1` / `role === 2` / `code === 200` / `isRecommended === 1` / `priority === 3`。后端改 enum 值，前端**静默失效**（TypeScript 不报错，因为两边都是 number）。

**改动**：
- 新建 `vue-3d/src/utils/enum.ts` 集中导出所有 enum（`TaskStatus` / `Role` / `ProjectStatus` / `Priority` / `StageStatus` / `PrinterStatus` / `RecommendedFlag` / `ErrorCode` 等）
- 新增 `types/artwork.ts` 的 `RecommendedFlag` enum（之前没有，专门为 `isRecommended` 加的）
- 42 处硬编码数字全改 enum：

| 文件 | 改动 |
|---|---|
| `views/artwork/my/index.vue` | `isRecommended === 1` → `RecommendedFlag.YES` |
| `views/artwork/list/index.vue` | 同上 |
| `views/artwork/detail/index.vue` | 同上 + `role === 1/2` → `Role.PRESIDENT/TECH_LEAD` |
| `views/artwork/create/index.vue` | `code === 200` → `ErrorCode.SUCCESS` ×2 |
| `views/artwork/edit/index.vue` | `code === 200` → `ErrorCode.SUCCESS` ×2 |
| `views/admin/artwork/recommend/index.vue` | `isRecommended === 1` → `RecommendedFlag.YES` ×7 |
| `views/admin/printer/list/index.vue` | `role === 1` → `Role.PRESIDENT` |
| `views/admin/printer/maintenance/index.vue` | 同上 |
| `views/task/detail/index.vue` | `role === 1/2` → `Role.PRESIDENT/TECH_LEAD` |
| `views/task/my/index.vue` | `priority === 1/3` → `Priority.URGENT/LOW` |
| `views/task/queue/index.vue` | 同上 |
| `views/admin/task/pending/index.vue` | 同上 |
| `views/project/detail/index.vue` | `project.status === 0/1` / `projectType === 1/2/3` / `s.status === 0/1/2` / `role === 1/2` 全改 enum |
| `views/project/list/index.vue` | status 数字改 enum |
| `views/admin/project/manage/index.vue` | status 数字改 enum |
| `components/layout/AppHeader.vue` | `role === 2/3/4` 改 enum |

**风险消除**：后端改 enum 值，前端 TypeScript 立刻报错。

---

### ④ 前端：新增 `utils/enum.ts` 集中导出

避免每个文件都写 `import { TaskStatus, TaskStatusText } from '@/types/task'` + `import { Role } from '@/types/member'` 等长链。统一从 `@/utils/enum` 导入：

```ts
import { TaskStatus, Role, ErrorCode, RecommendedFlag } from '@/utils/enum'
```

---

## 2026-06-28 · v2 修复批次

本轮集中修复了 7 个用户报修 + 1 个流程改进。所有改动都向后兼容，刷新即可见效。

### ① 任务状态机：`APPROVED` 加入"进行中"列表

**问题**：待审批通过后，任务从 `/pending` 消失，但 `/queue`（进行中）只查 `QUEUED + PRINTING`，**不包括 `APPROVED`** —— 已通过未分配打印机的任务在所有列表里都查不到，像"凭空消失"。

**改动**：
- 后端 `TaskServiceImpl.queue()` wrapper 加 `STATUS_APPROVED`
- 前端 `views/admin/task/active/index.vue` 按钮条件扩展：`status === QUEUED || status === APPROVED`

**影响**：审批通过的任务立即出现在"进行中任务"页，可点"分配"选打印机。

---

### ② 流程改进：完成打印不再自动归档作品库

**问题**：`TaskServiceImpl.finishPrint()` 完成打印时**自动** `artworkMapper.insert()` 一条空作品 —— 这跟用户的"登记作品"功能**重复**，数据库会冒出两条作品（一条自动的空作品、一条用户填的完整作品）。

**改动**：
- 后端 `TaskServiceImpl.finishPrint()` 移除"5. 自动归档作品库"整段代码
- 清理：删除 `import Artwork` / `import ArtworkMapper`、删除 `artworkMapper` 字段、javadoc 同步更新
- 前端 5 处提示语更新（见 ⑦）

**新流程**：

```
DONE（任务完成）→ 用户到【我的作品】→ 登记作品（带照片 + 心得）→ 作品库
                ↑ 不再有"自动空作品"了
```

---

### ③ 分配打印机下拉过滤工作/排队中

**问题**：`PrinterServiceImpl.availablePrinters()` 查"忙"任务时只查 `STATUS_PRINTING`（打印中），**漏了 `STATUS_QUEUED`（已分配排队）** —— 已分配给任务 A 但还没开始打印的 P-001 还会出现在任务 B 的下拉里。

**改动**：
```java
// 之前
.eq(PrintTask::getStatus, PrintTask.STATUS_PRINTING)

// 现在
.in(PrintTask::getStatus, PrintTask.STATUS_QUEUED, PrintTask.STATUS_PRINTING)
```

**影响**：下拉里只剩 `STATUS_NORMAL`（正常）且没被任何 QUEUED/PRINTING 任务占用的打印机。彻底避免重复分配。

---

### ④ 作品库作者列显示姓名（不再显示学号）

**问题**：`Artwork` 实体只有 `authorId`（学号），没有 `authorName` 字段。后端 list API 只返回学号，前端 `item.authorName || item.authorId` 兜底显示学号。

**改动**：
- 后端 `Artwork` entity 加 `authorName` 字段（`@TableField(exist = false)`，DB 不存）
- 后端 `ArtworkServiceImpl` 加 `fillArtworkAuthorNames()` 私有方法（仿 `TaskServiceImpl.fillTaskRelationalNames`），N+1 → 1 次查
- 4 个查询出口（`list` / `myArtworks` / `detail` / `recommended`）调用 fill
- 前端 0 改动（已有 `authorName || authorId` 兜底逻辑）

**影响**：作品卡片"作者：2023010005" → "作者：刘洋"。

---

### ⑤ 全站弹窗：统一用 AppDialog + 修复塌陷

**问题**：部分维护记录/打印机弹窗用 `<el-form :model label-width="100px">`，el-form-item flexbox 布局把弹窗压成 300px（塌陷）。

**改动**：
- `components/common/AppDialog.vue` 的 `.el-dialog` 加 `min-width: 540px !important` 兜底
- `styles/index.scss` 的 `.app-dialog .el-dialog` 加 `min-width: 540px !important`
- 弹窗居中加显式兜底 `position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%)`
- 新增/编辑弹窗（打印机、耗材、维护记录）改为分组卡片样式：section 标题 + icon + 虚线分隔

---

### ⑥ ElNotification 通知：从"跑到底部 + 文字重叠"到"右上角 380px 标准"

**问题**：
- 通知跑到页面底部（`fixed` 被祖先 `transform` 破坏）
- 通知收缩成 126px（width 丢了），标题和内容文字重叠
- 关闭键 X 跑到标题位置（absolute 定位失效）
- 业务错误时弹 2 个通知（业务层 catch + request.ts 拦截器各弹一次）

**改动**：
- `styles/index.scss` 的 `.el-notification` 加：
  - `position: fixed !important; top: 80px; right: 24px` 强制右上角
  - `width: 380px !important` 强制宽度
  - `display: flex; flex-direction: column` 标题/内容分两行
  - 标题 `::before` 加 4 类型圆形 icon（success ✓ / warning ! / error ✕ / info i）
  - 关闭键 `position: absolute` 强制右上
- `styles/index.scss` 的 `.el-notification__icon` / `__statusIcon` 隐藏（避免重复 icon）
- 业务层 7 处重复 `ElNotification.error(e?.message...)` 全部去掉（依赖 request.ts 拦截器统一弹）
- `utils/request.ts` 的 `showError` duration 改 2000（业务错误 2 秒自动消失）
- `main.ts` 加全局 click 代理：点通知空白处（非关闭键）立即关闭

**影响**：
- 通知 380px 标准宽度，标题/内容两行清晰
- 2 秒自动消失
- 点通知任何空白处立即关闭
- 业务错误只弹一次（request.ts 拦截器兜底）

---

### ⑦ 提示语统一更新

5 处提示语从"自动归档作品库"改为"手动登记作品"：

| 文件 | 行 | 旧 | 新 |
|---|---|---|---|
| `views/admin/task/active/index.vue` | 161 | 已自动扣减库存 + 归档作品库 | 已自动扣减库存 + 累计打印次数。请到【我的作品】手动登记作品（带照片 + 心得） |
| `views/admin/task/active/index.vue` | 286 | 完成后会自动：扣减库存 + 累计打印次数 + 归档作品库 | 完成后会自动：扣减库存 + 累计打印次数。<br/><b>作品需要用户手动登记</b> |
| `views/task/detail/index.vue` | 114 | 同上 | 同上 |
| `views/task/detail/index.vue` | 298 | 同上 | 同上 |
| `views/artwork/my/index.vue` | 80 | 完成打印任务后会自动归档到这里~ | 完成打印任务后，请到这里点击「登记作品」上传照片 + 心得~ |

---

## 配套维护

### 路由器新增菜单
- `router/routes.ts` 加 `/admin/printer/maintenance`（维护记录页，页面早就存在但路由没注册）

### 维护记录功能完善
- `views/admin/printer/maintenance/index.vue` 弹窗改为分组卡片 + radio button 横排（保养/维修/换件/校准）
- `handleSubmit` 加联动：选"维修"保存时自动把维修中的打印机切回"正常"（仅当 printer.status == MAINTENANCE 时切）

### 错误处理统一
- `views/artwork/list/index.vue`、`views/artwork/detail/index.vue` 删除时加 try/catch（之前的"Uncaught (in promise) Error" 错误直接冒泡到控制台）

---

## 部署提示

- **后端改了**：Maven 自动编译，但需要 `Ctrl+C` 那个 `mvn spring-boot:run` 进程再重启
- **前端改了**：Vite HMR 自动热重载，但 `main.ts` 改动需要 `Ctrl+Shift+R` 硬刷新
