# CHANGELOG

> 3D 打印科创会管理系统 — 变更日志
> 记录每次修复 / 重构 / 新功能的时间、内容、影响范围

---

## 2026-06-29 · 生产部署批次（v2.1）

本轮完成 **生产环境首次部署 + 上线前修复**，从前到后踩了 7 个生产专属坑，都已填平。

### ① 部署架构：阿里云 ECS + Cloudflare Pages + Cloudflare 反代

**最终架构**（对比之前的本地 dev → 完全不同）：

```
[浏览器]
    ↓ https://3d-printing-management-system.pages.dev
[Cloudflare Pages（前端静态资源，Vue 3 + Element Plus 构建产物）]
    ↓ VITE_API_BASE_URL=https://api.3dprint.ccwu.cc/api
[Cloudflare 反代（api.3dprint.ccwu.cc · NS 在 Cloudflare · A 记录 Proxied 8.137.80.194 · SSL Flexible）]
    ↓ HTTP（80 端口）
[阿里云 ECS（Ubuntu 24.04 · 8.137.80.194）]
    ├─ Nginx（80 → 127.0.0.1:8080 反代 · OPTIONS 预检 204 · CORS 头处理）
    └─ Spring Boot 3.3.5（systemd printclub.service · -Xmx512m · profile=prod）
            ↓ JDBC utf8mb4_unicode_ci
        [MySQL 8.0.46（utf8mb4_unicode_ci · innodb_buffer_pool_size=128M）]
```

**关键决策**：
- **不用 Cloudflare Worker / Tunnel**（`*.workers.dev` / `api.trycloudflare.com` 在中国大陆被 GFW 屏蔽，用户访问不到）
- **用免费子域名 `3dprint.ccwu.cc`**（NS 切到 Cloudflare + A 记录 Proxied → Cloudflare 反代流量，不走直接 IP，避开阿里云域名备案）
- **CORS 双保险**：nginx 处理 OPTIONS 预检（直接 `return 204`）+ Spring Boot `@Bean CorsConfigurationSource` 接受所有 origin（`setAllowedOriginPatterns(List.of("*"))` + `setAllowCredentials(false)`，因为前端用 JWT Authorization 头不传 cookie）

### ② Spring Boot CORS 修复（最阴的坑）

**症状**：本地 `mvn spring-boot:run` 跑 + 浏览器 fetch 调用完全 OK，但部署到 ECS + 经过 Cloudflare 反代后，**所有 POST/PUT 接口返回 403 Forbidden**。

**原因**：之前的 CorsConfig 用的是：
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns("https://*.pages.dev")  // ❌ 这个写法从一开始就是错的
        .allowedMethods("GET", "POST", ...)
        .allowCredentials(true);
}
```

**Spring 6.x `CorsConfiguration.matchOriginPattern` 是 token-level 匹配**：用 `.` 拆分后每个 token 必须完全相等或等于 `*`。`https://*.pages.dev` 拆成 `["https://*", "pages", "dev"]`，实际请求 `https://3d-printing-management-system.pages.dev` 拆成 `["https://3d-...", "pages", "dev"]` —— **第一段 `https://*` vs `https://3d-...` 不相等 → 不匹配 → 403**。

**为什么本地没暴露**：本地 curl 不带 Origin 头，绕过 CorsFilter；浏览器 fetch 才带。

**改动**（spring-boot-backend/src/main/java/com/printclub/config/CorsConfig.java）：
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));  // 接受所有 origin
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(false);  // 前端用 JWT Authorization 头，不用 cookie
    config.setMaxAge(3600L);  // 预检缓存 1 小时
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**nginx 同步处理 OPTIONS**（/etc/nginx/sites-available/printclub）：
```nginx
location / {
    if ($request_method = 'OPTIONS') {
        add_header Access-Control-Allow-Origin "$http_origin" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
        add_header Access-Control-Max-Age 3600;
        add_header Content-Type 'text/plain; charset=utf-8';
        add_header Content-Length 0;
        return 204;  # 预检直接返，不进 Spring Boot
    }
    # 普通请求反代
    proxy_pass http://127.0.0.1:8080;
    # 所有响应加 CORS 头（关键：always，覆盖错误响应）
    add_header Access-Control-Allow-Origin "$http_origin" always;
    add_header Access-Control-Allow-Credentials "true" always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
}
```

### ③ 前端 TypeScript 错误修复（4 处，影响生产构建）

**症状**：`vue-tsc --build` 退出码非 0，但 `npm run build` 还能"成功"（Vite 跳过严格类型检查）—— **生产部署后才在浏览器 console 暴露运行时错误**。

**修复**：
- `views/admin/dashboard/index.vue:251` —— `(item as any).applicant_name` → `item.applicant_name`，对应在 `types/stats.ts` 的 `MemberRank` 接口加 `applicant_name?: string`
- `views/admin/material/inbound/index.vue:59` —— `MaterialTypeText` keyof cast
- `views/admin/member/index.vue:211/215` —— `openRoleDialog(openSkillDialog)` 参数 `m: any` 类型声明
- `views/artwork/create/index.vue:61` —— `status: '5,8' as any` 改成 typed union

**防御性改动**：以后 `vue-tsc --noEmit && npm run build` 都要过，CI/CD 加 type-check gate。

### ④ el-tag 浅色主题对比度修复（**30+ 处**）

**症状**：浅色主题下，状态标签（如 `<el-tag type="success">已发布</el-tag>`）**背景是浅绿、文字是浅绿**，对比度只有 ~1.5:1，肉眼几乎看不清。

**原因**：Element Plus 2.14 默认 `effect="light"`（浅色背景 + 浅色文字），**只在暗色主题或 `effect="dark"` 才有足够对比度**。项目全局默认 light，所以全站都糊。

**修复**：
1. 手工修 3 处最高频（member/index.vue 技能等级列 / task/stats/index.vue 任务状态 / StatusTag 组件默认 effect）
2. **Python 脚本批量改**：正则 `<el-tag (?![^>]*effect=)((?:[^>]*?))(/?)>` 匹配所有无 effect 的 el-tag，加 `effect="dark"`，**覆盖 13 个文件 30+ 处**：
   - `views/admin/printer/list/index.vue`、`maintenance/index.vue`
   - `views/admin/material/list/index.vue`、`log/index.vue`
   - `views/admin/log/index.vue`
   - `views/task/queue/index.vue`、`my/index.vue`、`active/index.vue`、`detail/index.vue`、`pending/index.vue`
   - `views/project/list/index.vue`、`manage/index.vue`、`detail/index.vue`
   - `views/admin/artwork/recommend/index.vue`
   - `views/profile/index.vue`
3. 脚本踩坑：**第一版正则用 `[^>]+?`（+ = 至少 1 字符），漏掉了无属性的 `<el-tag>`**（如 profile/index.vue:159），改用 `[^>]*?` 才正确

### ⑤ 代码质量优化（5 处）

| 文件 | 改动 |
|---|---|
| `types/stats.ts` | `MemberRank` 加 `applicant_name?: string` |
| `views/admin/dashboard/index.vue` | `import { watch }` 从方法内移到顶部（规范） |
| `views/admin/dashboard/index.vue:251` | 去掉 `(item as any)`，用 typed optional |
| `views/admin/printer/maintenance/index.vue` | `printers: ref<any[]>` → `ref<Printer[]>` |
| `views/admin/printer/maintenance/index.vue` | 函数 `printerName` 重命名为 `printerIdById`（误导命名修） |

### ⑥ 生产部署资源适配（1.6 GiB RAM 内存压榨）

**ECS 实际配置**：2 vCPU / **1.6 GiB RAM** / 40 GiB SSD / 200 Mbps 带宽 / 成都 / Ubuntu 24.04。

**内存压榨**：
- 加 4 GiB swap：`fallocate -l 4G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile` + `/etc/fstab` 持久化
- MySQL `innodb_buffer_pool_size=128M`（默认 800M+ 太大）
- JVM `-Xmx512m`（原来想用 768m 内存吃紧）
- 总进程占用：MySQL ~150M + Spring Boot ~500M + Nginx ~5M + OS ~300M = ~1G，留 ~600M 给文件系统缓存

### ⑦ 清理冗余：删除 vue-3d/functions/ 目录

之前用 Cloudflare Pages Functions 做反代代理的方案，已被 Cloudflare 反代（NS + A 记录）替代，留着会和前端 `vite build` 打架导致部署失败。**已删除**。

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

### 本地开发

- **后端改了**：Maven 自动编译，但需要 `Ctrl+C` 那个 `mvn spring-boot:run` 进程再重启
- **前端改了**：Vite HMR 自动热重载，但 `main.ts` 改动需要 `Ctrl+Shift+R` 硬刷新

### 生产部署（v2.1）

详细部署流程 + 踩坑记录见 [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) 的 14.13+ 章节。简要：

**前端**：
```bash
git add . && git commit -m "..." && git push  # Cloudflare Pages 自动构建部署（1-2 分钟）
```

**后端**：
```bash
# 本地构建
cd spring-boot-backend
mvn clean package -DskipTests

# 上传 jar 到 ECS
# ⚠️ scp 目标必须和 systemctl cat printclub.service 里 WD + ExecStart -jar 路径对齐
#    验证：ssh root@8.137.80.194 "systemctl cat printclub.service | grep -E 'WorkingDirectory|ExecStart'"
scp target/print-club-backend.jar root@8.137.80.194:/opt/printclub/app/spring-boot-backend/target/print-club-backend.jar

# 在 ECS 上重启服务
ssh root@8.137.80.194
systemctl restart printclub
systemctl status printclub
journalctl -u printclub -n 50 -f  # 看日志
```

**域名 + 反代**：
- 前端：`https://3d-printing-management-system.pages.dev`（Cloudflare Pages 自动）
- 后端 API：`https://api.3dprint.ccwu.cc/api`（Cloudflare 反代 → ECS Nginx :80 → Spring Boot :8080）
- 改 NS + A 记录在 Cloudflare Dashboard → 3dprint.ccwu.cc 域名

### 生产环境清单（必读）

- ✅ DB 密码用 `${DB_PASSWORD:...}` 占位符 + systemd ExecStart 注入（**不提交 Git**）
- ✅ nginx OPTIONS 预检 return 204 + 响应加 `always` 标志
- ✅ Spring Boot `@Bean CorsConfigurationSource` + `setAllowedOriginPatterns("*")` + `setAllowCredentials(false)`
- ✅ `vue-tsc --noEmit` 在 CI 必过
- ✅ 所有 el-tag 加 `effect="dark"`（浅色主题对比度）
- ✅ 阿里云防火墙开 80/TCP（22 默认开）
