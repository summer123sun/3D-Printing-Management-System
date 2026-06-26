# 3D 打印科创会管理系统 - 团队开发总纲

> **本文件 = 前后端两份大纲的整合版**，给全组 3 人共同参考。
> 详细分述见：
> - 前端：[docs/frontend_outline_v1.md](./frontend_outline_v1.md)
> - 后端：[docs/backend_outline_v1.md](./backend_outline_v1.md)
> - 需求：[docs/requirements_v2.md](./requirements_v2.md)
> - 数据库：[docs/db_design.sql](./db_design.sql)

---

## 〇、TL;DR

- **6 大模块、约 35 个页面、26 个核心接口**
- **3 人小组**，每人包一组"前后端"——同一个人既写后端又写前端，互相熟悉对方代码
- **8 周开发 + 1 周演示准备**
- **接口契约先行**：第 1 周末锁定 API 文档，前端用 mock，后端按契约实现
- **第 7 周全员联调**，第 8 周演示 + 答辩

---

## 一、团队结构（3 人 × 2 角色）

| 角色 | 同学 | 后端负责 | 前端负责 | 工作量 |
|---|---|---|---|---|
| **A**（基础设施型） | 待定 | 后端 D | 前端 A（M1 用户 + 公共组件） | ~30% |
| **B**（核心业务型） | **你（组长）** | 后端 E | 前端 M2 + M3（任务 + 项目） | ~40% |
| **C**（辅助业务型） | 待定 | 后端 F | 前端 M4 + M5 + M6（作品库 + 设备耗材 + 统计日志） | ~30% |

> **为什么这么分？**
> - 核心业务（M2 + M3）工作量大、端到端流程复杂、bug 修复反复——需要 1 人专注
> - 辅助业务（M4 + M5 + M6）相对独立、可以串联做完——适合 1 人稳扎稳打
> - 基础设施（M1）必须有人专门做，**否则后面所有人都会被卡住**

---

## 二、6 个角色详细分工

### 🅰️ A 同学（后端 D + 前端 M1）— 基础设施型

#### 后端 D（第 1-3 周主线）

| 周 | 任务 | 关键产出 |
|---|---|---|
| 1 | 全员一起学 Spring Boot 基础（3 人同一 demo） | 3 人能各自 `mvn spring-boot:run` |
| 2 | 项目脚手架（pom.xml + application.yml + 启动类 + 包结构） | 项目能跑 |
| 2 | 通用组件：Result / ResultCode / PageResult / BusinessException / GlobalExceptionHandler | 统一返回格式 |
| 2 | JwtUtil + SecurityContext + RequireAuth/RequireRole 注解 + JwtInterceptor | JWT 鉴权 |
| 2 | CorsConfig + MybatisPlusConfig + WebMvcConfig + SwaggerConfig/Knife4j | 跨域 + 分页 + 文档 |
| 2-3 | 登录模块（AuthController + AuthService + LoginDTO/VO + BCrypt 校验） | `/api/auth/login` 通 |
| 3 | 用户模块（UserController + UserService + Member entity + UserMapper + CRUD） | 用户增删改查通 |
| 3 | 文件上传（FileController + FileUploadUtil + 路径规范） | `/api/file/upload` 通 |
| 5 | 接口文档完善（所有 Controller 加 `@Operation`） | Knife4j 完整 |
| 6 | 跨模块 bug 修复 + 代码 review | 性能 + 一致性 |
| 7 | 部署文档（Nginx + jar 包启动） | 部署手册 |
| 8 | 用户手册 + 答辩 | PPT + 演讲 |

#### 前端 M1（第 1-2 周 + 第 7 周）

| 周 | 任务 | 关键产出 |
|---|---|---|
| 1 | Vite 脚手架 + 依赖安装 + vite.config.ts + tsconfig.json | `npm run dev` 跑通 |
| 1 | vue-router + Pinia + axios 封装 + request 拦截器 | 基础脚手架 |
| 1-2 | 登录页 + 路由守卫（未登录跳 /login）+ 403/404 + 个人中心 + 修改密码 | M1 完整 |
| 2 | AppLayout 布局（Header / Sidebar / Main）+ 全局样式（SCSS 变量、主题） | 整体布局 |
| 2-3 | 公共组件：`PageHeader` / `StatusTag` / `EmptyState` / `DataTable` / `SearchForm` / `UserAvatar` | 后续模块可复用 |
| 7 | Vitest 单元测试 + Playwright E2E + 性能优化 + 部署文档 | 测试 + 优化 |
| 8 | 用户手册 + 答辩 | 文档 |

### 🅱️ B 同学（你 — 后端 E + 前端 M2 + M3）— 核心业务型 ✅ **已完成**

#### 后端 E（第 2-4 周主线）

| 周 | 任务 | 关键产出 |
|---|---|---|
| 1 | 全员一起学 Spring Boot 基础 | — |
| 2 | 学 MyBatis 复杂查询 + 任务表 ER + 与前端 B（你）契约对齐 | 接口契约锁定 |
| 3 | 任务模块全流程：申请 → 审批 → 分配 → 打印 → 完成 → 取件 → 自动归档作品库 | 13 个接口全通 |
| 3 | 状态流转合法性校验（枚举类 + 状态机） | 状态不合法返回 4002 |
| 3 | 完成事务：扣库存 + 累加打印次数 + 累加打印机时长 | 4 步在同一事务 |
| 4 | 项目模块全流程：创建 → 招募成员 → 阶段管理 → 文件管理 → 完成 | 13 个接口全通 |
| 4 | 关联任务查询（项目详情页展示关联打印任务） | 详情 VO 含 tasks |
| 5 | 联调（与前端真实接口对接）+ 修 bug | 端到端跑通 |
| 6 | 事务边界优化（避免长事务）、库存扣减逻辑 | 性能 + 一致性 |
| 7 | JUnit 5 接口测试 + 边界条件 | 测试覆盖 |
| 8 | 演示数据准备 + 答辩 | 3 个典型场景 |

**后端 E 关键文件**：
- `module/task/`（任务模块）：Controller + 7 DTO + Entity + Mapper + Service/Impl = 12 文件
- `module/project/`（项目模块）：Controller + 5 DTO + 4 Entity + 4 Mapper + Service/Impl = 16 文件

#### 前端 M2 + M3（第 2-4 周）

| 周 | 任务 | 关键产出 |
|---|---|---|
| 2 | 任务状态枚举 + TS 类型 + 接口请求层 `api/task.ts` | 类型完整 |
| 3 | 任务社员端 4 页：申请 / 我的 / 队列 / 详情 | 4 个页面 |
| 3 | 任务管理端 4 页：待审批 / 进行中 / 历史 / 统计 | 4 个页面 |
| 3 | 任务核心组件：`StlUploader` / `ParamForm` / `TaskTimeline` | 4 个组件 |
| 4 | 项目社员端 3 页：列表 / 详情 / 申请加入 | 3 个页面 |
| 4 | 项目管理端 2 页：创建 / 管理 | 2 个页面 |
| 4 | 项目核心组件：`StageEditor`（动态阶段编辑） | 1 个组件 |
| 5 | M2 + M3 联调 + 修 bug | 端到端跑通 |
| 6 | wangeditor 集成到项目心得 | 富文本 |
| 7 | 接口联调 + 浏览器兼容性 | 兼容性测试 |
| 8 | 演示场景数据 + 答辩 | 演示准备 |

**前端 E 关键文件**：
- 任务页面 8 个：`views/task/{apply,detail,my,queue}/` + `views/admin/task/{pending,active,history,stats}/`
- 项目页面 5 个：`views/project/{apply,detail,list}/` + `views/admin/project/{create,manage}/`
- 核心组件 4 个：`components/task/{apply/StlUploader,apply/ParamForm,detail/TaskTimeline}` + `components/project/manage/StageEditor`
- API/Store/Types：`api/task.ts` + `api/project.ts` + `stores/task.ts` + `stores/project.ts` + `types/task.ts` + `types/project.ts`

### 🅲 C 同学（后端 F + 前端 M4 + M5 + M6）— 辅助业务型

#### 后端 F（第 3-6 周主线）

| 周 | 任务 | 关键产出 |
|---|---|---|
| 1 | 全员一起学 Spring Boot 基础 | — |
| 2 | 学 Spring AOP + 切面编程 | 准备日志 |
| 3 | 作品库模块：artworkController + service + entity + 上传/浏览/推荐/分类筛选 | 作品 CRUD + 推荐 |
| 3 | **协作点**：任务完成时自动归档作品库（与 B 协作） | 归档逻辑 |
| 4 | 设备管理：printerController + service + 状态切换/维护记录 | 设备 CRUD |
| 4 | 实时查询当前任务（替代 v2 删掉的 `current_task_id` 字段） | `WHERE printer_id=X AND status=4` |
| 5 | 耗材管理：materialController + materialLog + 入库/消耗/库存预警 | 库存 + 预警 |
| 5 | 统计看板 SQL（4 类：任务趋势/材料占比/成员排行/项目状态） | 看板数据接口 |
| 6 | 系统日志：logController + AOP 切面 + `@AuditLog` 注解 + 关键操作自动记录 | 日志自动入库 |
| 7 | 联调 + 部署 + 答辩 | 收尾 |

**后端 F 关键模块**：
- `module/artwork/`（作品库）：作品列表 / 详情 / 我的作品 / 推荐
- `module/device/printer/`（设备）：台账 / 维护记录 / 状态变更
- `module/device/material/`（耗材）：库存 / 入库 / 消耗 / 预警
- `module/stats/`（统计）：4 个看板接口
- `module/log/`（日志）：AOP 切面 + 查询接口

#### 前端 M4 + M5 + M6（第 3-6 周）

| 周 | 任务 | 关键产出 |
|---|---|---|
| 2-3 | 字典数据定义 + 类型定义（`types/artwork.ts`、`types/printer.ts` 等） | 类型完整 |
| 5 | 作品库 4 页：列表 / 详情 / 我的作品 / 推荐管理 | 4 个页面 |
| 5-6 | 设备 3 页：打印机台账 / 维护记录 / 状态变更 | 3 个页面 |
| 5-6 | 耗材 4 页：库存列表 / 入库操作 / 操作记录 / 库存预警 | 4 个页面 |
| 6 | 统计 1 页 + 日志 1 页 + 成员管理 1 页 = 3 页 | 3 个页面 |
| 6 | ECharts 集成（4 张图：折线/饼图/柱状/仪表盘） | 可视化 |
| 7 | 联调 + 修 bug | 收尾 |
| 8 | PPT（你这一部分）+ 答辩演练 | 演示 |

**前端 F 关键页面**：
- M4 作品库：`views/artwork/{list,detail}` + `views/admin/artwork/recommend`
- M5 设备耗材：`views/admin/printer/*` + `views/admin/material/*`
- M6 统计日志：`views/admin/{dashboard,member,log}`

---

## 三、8 周时间表

```
周次    | 全员                      | A（基础设施）         | B（核心业务 — 你）    | C（辅助业务）
--------|---------------------------|---------------------|----------------------|--------------------
第 1 周 | 集体学 Spring Boot 基础   | 脚手架 + 路由 + Pinia | 学 MyBatis 复杂查询   | 学 AOP
第 2 周 | 锁定接口契约（前后端对齐）| 登录 + 通用组件       | 任务 DTO + 类型      | 类型定义
第 3 周 | —                         | 用户 CRUD + 文件上传 | 任务全流程（13 接口）| 作品库
第 4 周 | —                         | Knife4j 完善        | 项目全流程（13 接口）| 设备管理
第 5 周 | 联调启动                   | bug 修复            | M2 + M3 联调 + 修 bug| 耗材 + 统计 SQL
第 6 周 | 性能优化                   | 性能优化            | wangeditor + 性能    | 日志 + 看板
第 7 周 | 全栈联调 + 测试 + 部署     | Vitest + Playwright | JUnit + 兼容测试     | 部署 + 测试
第 8 周 | 演示 + 答辩                | PPT + 用户手册      | 演示数据 + 答辩      | PPT + 答辩
```

---

## 四、启动步骤（组员 clone 后跟着做）

### 1. 准备环境

| 软件 | 版本 | 验证命令 |
|---|---|---|
| JDK | 17 LTS | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 20 LTS | `node -v` |
| MySQL | 8.0+ | `mysql -V` |
| Git | 最新 | `git --version` |
| IntelliJ IDEA | 2023.x Ultimate（学生免费） | — |

### 2. clone 仓库

```bash
git clone https://github.com/你的用户名/3D-Printing-Management-System.git
cd 3D-Printing-Management-System
```

### 3. 初始化数据库（所有人共用一份）

```bash
# 登录 MySQL
mysql -uroot -p

# 执行一键脚本（含 11 张表 + 模拟数据 + BCrypt 密码）
source /path/to/3D-Printing-Management-System/spring-boot-backend/docs/SQL/init-bcrypt.sql
```

初始化后会有 **4 个测试账号**（密码全部 `123456`）：

| 账号 | 姓名 | 角色 |
|---|---|---|
| `2023010001` | 张明 | 社长 |
| `2023010002` | 李强 | 技术骨干 |
| `2023010005` | 刘洋 | 普通社员 |
| `2024010001` | 马超 | 新社员（2024 级） |

### 4. 启动后端

**A 同学**首次启动用环境变量方式（密码不入仓）：

**方式 A：命令行（推荐）**

```bash
cd spring-boot-backend
DB_PASSWORD=你的MySQL密码 mvn spring-boot:run
```

**方式 B：IDEA 启动配置**

1. `Run` → `Edit Configurations...`
2. 选 `PrintClubApplication` → 右侧 `Environment variables` → 加一行：
   ```
   DB_PASSWORD=你的MySQL密码
   ```
3. 点 OK，启动

启动后访问：

- 接口根地址：`http://localhost:8080`
- Knife4j 接口文档：`http://localhost:8080/doc.html`（中文 Swagger UI）

### 5. 启动前端

```bash
cd vue-3d
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`，用上面任意一个账号登录。

---

## 五、协作约定

### 5.1 Git workflow

```
main                 # 稳定版本（演示前打 tag）
develop              # 日常开发分支

feature/d-auth       # A 的鉴权模块
feature/d-user       # A 的用户模块
feature/e-task       # B 的任务模块（你）
feature/e-project    # B 的项目模块（你）
feature/f-artwork    # C 的作品库
feature/f-device     # C 的设备耗材
```

**合并流程**：`feature/<name>` → `develop` → （周中演示通过后）→ `main`

### 5.2 提交信息规范

```bash
feat(task): 新增任务提交接口
fix(auth): 修复 JWT 过期返回 500
docs: 更新接口文档
refactor(common): 重构 Result 工具
style: 格式化代码
test(task): 添加任务模块单元测试
```

### 5.3 接口文档

后端用 **Knife4j（Swagger 中文版）**：`http://localhost:8080/doc.html`

每个 Controller 加 `@Operation` 注解，前端可以在线看接口 + 测试。

### 5.4 每晚 stand-up（21:00 - 21:30）

3 人 30 分钟同步：
- 今天完成什么
- 明天计划做什么
- 遇到什么卡点（互相帮）

> 强烈建议——3 人从零学，肯定有人卡住，互相帮比各查资料快。

### 5.5 代码 review

- **第 5 周起每周一次** 3 人互看核心代码
- 重点 review：事务边界、状态机校验、SQL 注入、JWT 鉴权

---

## 六、接口契约交付节奏

> **接口契约 = 前后端对齐的 API 文档**。第 1 周末锁定，前端先用 mock，第 3 周末开始联调。

| 时间 | 后端交付 | 前端可开始真实联调 |
|---|---|---|
| ✅ 已完成 | 登录 + 用户 CRUD | M1 |
| ✅ 已完成 | 任务全流程（13 接口）| M2 |
| ✅ 已完成 | 项目全流程（13 接口）| M3 |
| **第 5 周** | 作品库 + 设备 + 耗材 | M4 + M5 |
| **第 6 周** | 统计 + 日志 | M6 |
| **第 7 周** | 联调 + 测试 + 部署 | 联调 + 测试 + 部署 |

---

## 七、风险与对策

| 风险 | 概率 | 影响 | 对策 |
|---|---|---|---|
| 3 人学习速度不一致 | 高 | 高 | 第 1 周全员一起学，互相帮 |
| A 进度慢拖累 B 和 C | 中 | 高 | A 第 2 周必须交付项目脚手架，否则 B/C 起不来 |
| B 进度慢拖累 C | 中 | 高 | B 第 3 周末必须交付任务全流程（已 ✅），第 4 周末项目全流程（已 ✅）|
| 接口契约反复修改 | 中 | 中 | 第 1 周末锁定，第 2 周后改要全组同意 |
| 文件上传路径前后端不一致 | 中 | 中 | A 第 3 周出统一规范 |
| 事务边界没考虑清楚 | 中 | 中 | B 第 3-4 周重点关注（已 ✅，完成事务已实现）|
| 联调时接口对不上 | 高 | 高 | 第 3 周末是硬节点，前端 B（已 ✅）必须出 mock 跑通流程 |
| 答辩时被问代码细节 | 中 | 高 | 3 人必须互相 review 第 7 周，至少要能讲清楚对方的核心代码 |
| GitHub 提交时泄露密码 | 中 | 高 | `.gitignore` 已配，本地用环境变量 `DB_PASSWORD` 注入；上传前 `git diff` 校验 |

---

## 八、演示与答辩准备（第 8 周）

### 8.1 演示数据准备（3 个典型场景）

| 场景 | 准备内容 |
|---|---|
| **场景 1：完整打印流程** | 1 个普通社员登录 → 提交任务（带 STL）→ 1 个技术骨干审批 → 分配打印机 → 开始打印 → 完成（填实际耗材/耗时/质量分）→ 取件签到 → 自动归档作品库 |
| **场景 2：项目管理** | 1 个技术骨干创建项目（含动态阶段）→ 添加 3 个成员（不同角色）→ 推进阶段 → 上传项目文件 |
| **场景 3：管理后台** | 1 个社长登录 → 看板（4 张 ECharts 图：任务趋势/材料占比/成员排行/项目状态）→ 库存预警 → 系统日志 |

### 8.2 PPT 分工

每人 1/3 页面：

| 同学 | 负责演示 | 关键页 |
|---|---|---|
| A | 登录 + 个人中心 + 路由守卫 + 公共组件 | 第 1-3 页 |
| B（你） | 打印任务全流程 + 项目全流程 | 第 4-7 页（核心） |
| C | 作品库 + 设备耗材 + 统计看板 | 第 8-11 页 |

### 8.3 答辩 Q&A 准备

高频被问：

- **"JWT 为什么不用 session？"** —— 前后端分离不能用 session，必须 token
- **"为什么 MyBatis-Plus 而不是 MyBatis？"** —— 新手友好，CRUD 自动
- **"为什么用 BCrypt 不用 MD5？"** —— MD5 可逆，BCrypt 加盐
- **"事务边界怎么定的？"** —— 任务完成的 4 步在同一事务（扣库存/累加次数/累加时长/归档）
- **"为什么 v2 砍了点赞？"** —— 29 人小社团点赞是伪需求，"推荐案例"更有意义

---

## 九、立即可做（第 1 周立刻行动）

### 全员（30 分钟）

- [ ] 建 Git 仓库（GitHub 或 Gitee），建 `main` 和 `develop` 分支
- [ ] 约定 commit 规范（见 5.2）
- [ ] 拉个微信群/钉钉群，方便随时问问题

### 每人各自（本周内完成）

- [ ] 装 JDK 17 + Maven 3.9 + IntelliJ IDEA Ultimate（学生免费）
- [ ] 装 MySQL 8.0，导入 `docs/db_design.sql` 一键脚本
- [ ] 跑通 spring initializr 生成的 Hello World

### A 同学额外

- [ ] 在 Apifox 建好 API 项目骨架（按本大纲第 6 节的契约表）
- [ ] 写一份"3 人代码风格指南"放在仓库根 `docs/STYLE.md`

---

> **大纲结束**。下一步：先按本大纲第 4 节把项目跑起来，**确认 4 个测试账号都能登录**，
> 再各自按分工表领任务。
