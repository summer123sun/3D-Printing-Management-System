# 3D 打印科创会管理系统

> 大学生 3D 打印兴趣社团的一站式管理平台，期末项目实战。
> 覆盖**任务流转**、**项目管理**、**设备/耗材**、**作品库**、**统计**、**审计**六大场景。

## 🌐 生产环境（v2.1 已上线）

| 入口 | 地址 |
|---|---|
| **前端** | https://3d-printing-management-system.pages.dev |
| **后端 API** | https://api.3dprint.ccwu.cc/api |
| **架构** | Cloudflare Pages（前端）+ Cloudflare 反代（域名 → ECS）+ 阿里云 ECS Ubuntu 24.04（Spring Boot + Nginx + MySQL） |
| **域名** | `3dprint.ccwu.cc`（免费子域名，NS 在 Cloudflare，规避备案） |
| **测试账号** | `2023010001` 张明 / `2023010002` 李强 / `2023010005` 刘洋 / `2024010001` 马超（密码全部 `123456`） |

> 部署踩坑 + 完整流程见 [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) 14.13+ 章节

## 团队分工

| 角色 | 负责模块 |
|---|---|
| 组长（后端 E + 前端 M2/M3） | 任务模块、项目模块（任务全流程 + 项目立项/阶段/成员/文件） |
| 后端 D + 前端 M1 | 基础设施（脚手架/通用工具）+ 基础页面（登录/首页/个人中心） |
| 后端 F + 前端 M4/M5/M6 | 作品库、设备、耗材、统计、日志、成员管理 |
| 全员 | 数据库设计、需求文档、测试 |

## 技术栈

### 后端
- **Spring Boot 3.3.5** + **JDK 21** + **Maven 3.8+**
- **MyBatis-Plus 3.5.9**（LambdaQueryWrapper + 分页插件）
- **JWT 0.12.6**（无状态认证）
- **Lombok 1.18.34**（简化样板代码）
- **Hutool 5.8.x**（BCrypt 密码 + StrUtil）
- **Knife4j 4.x**（Swagger UI 增强）
- **mysql-connector-j 8.3.0**

### 前端
- **Vue 3.5** + **Vite 5** + **TypeScript 5**（Composition API + `<script setup>`）
- **Element Plus 2.14**（UI 组件库，CSS 变量主题色动态注入）
- **Pinia 3**（状态管理 + localStorage 持久化）
- **Vue Router 4**（6 种路由切换动画）
- **axios** + 拦截器（统一错误处理 + JWT 注入 + 离线检测）
- **SCSS**（深海蓝金配色变量 + 9 个关键帧动画）
- **@vueuse/core**（防抖等组合式 API）

## 目录结构

```
3D-Printing-Management-System/
├── vue-3d/                  # 前端（Vue 3 + Vite + TS + Element Plus + Pinia）
│   ├── src/
│   │   ├── api/             # 接口封装（按模块拆）
│   │   ├── stores/          # Pinia 状态（auth / task / project / artwork / material / printer / app）
│   │   ├── types/           # TS 类型 + 枚举 + 文本映射
│   │   ├── components/      # 通用 / 业务组件（AppDialog / EmptyState / PageHeader / StlUploader ...）
│   │   ├── views/           # 页面（按模块拆：artwork / project / task / admin）
│   │   ├── router/          # 路由 + 6 种切换动画
│   │   ├── styles/          # 全局样式（深海蓝金 + dark mode + AppDialog 覆盖）
│   │   ├── directives/      # 全局指令（v-debounce-click）
│   │   └── utils/           # 工具函数
│   └── public/favicon.svg   # 深海蓝 3D logo
│
├── spring-boot-backend/     # 后端（Spring Boot 3 + JDK 21 + MyBatis-Plus + JWT）
│   ├── src/main/java/com/printclub/
│   │   ├── common/          # 通用（Result / BusinessException / JWT / SecurityContext / PageUtils）
│   │   ├── config/          # 配置（WebMvcConfig 静态资源 / CorsConfig / Knife4jConfig）
│   │   ├── module/
│   │   │   ├── auth/        # 登录鉴权（D）
│   │   │   ├── user/        # 成员管理（F）
│   │   │   ├── task/        # 打印任务（E）
│   │   │   ├── project/     # 项目（E）
│   │   │   ├── artwork/     # 作品库（F）
│   │   │   ├── material/    # 耗材（F）
│   │   │   ├── printer/     # 打印机（F）
│   │   │   ├── stats/       # 统计（F）
│   │   │   ├── file/        # 文件上传（D）
│   │   │   └── log/         # 系统日志（F，6 个 service 共 25+ 操作点接入）
│   │   └── PrintClubApplication.java   # 启动类（@EnableAsync）
│   ├── src/main/resources/
│   │   ├── application.yml            # 主配置
│   │   ├── application-dev.yml        # dev 配置（用 ${DB_PASSWORD:root} 占位符）
│   │   ├── application-prod.yml       # prod 配置
│   │   └── static/placeholder.png    # 通用占位图
│   ├── docs/SQL/init-bcrypt.sql      # 一键建库建表插数据
│   └── pom.xml
│
├── scripts/                 # 启动 / 部署脚本（start-backend.ps1 / deploy.sh ...）
└── docs/                    # 项目文档
    ├── requirements_v2.md        # 需求规格说明书 v2
    ├── frontend_outline_v1.md    # 前端开发大纲
    ├── backend_outline_v1.md     # 后端开发大纲
    ├── team_outline_v1.md        # 团队分工 + 接口契约
    ├── db_design.sql             # 完整建表 SQL
    └── STYLE_GUIDE.md            # v2 视觉规范（配色 + 弹窗 + 动效 + dark mode）
```

## 快速启动

### 1. 准备环境

- **JDK 21+**（推荐 Temurin / Zulu）
- **Maven 3.8+**
- **Node.js 18+**（推荐 20 LTS）
- **MySQL 8.0+**

### 2. 初始化数据库

```bash
# 登录 MySQL
mysql -uroot -p

# 执行一键初始化脚本（含 11 张表 + 模拟数据 + BCrypt 密码）
source /path/to/spring-boot-backend/docs/SQL/init-bcrypt.sql
```

初始化后会有 4 个测试账号（密码全部 `123456`）：

| 账号 | 姓名 | 角色 | 用途 |
|---|---|---|---|
| `2023010001` | 张明 | 社长 | 全权限（系统日志只看社长可见） |
| `2023010002` | 李强 | 技术骨干 | 审批 + 分配打印机 + 完成 |
| `2023010005` | 刘洋 | 普通社员 | 申请任务 + 作品库 |
| `2024010001` | 马超 | 新社员（2024 级） | 受限权限 |

### 3. 启动后端

```bash
cd spring-boot-backend

# 方式 A：用环境变量注入数据库密码（推荐，不修改文件）
DB_PASSWORD=你的密码 mvn spring-boot:run

# 方式 B：直接修改 application-dev.yml 的 password 字段
mvn spring-boot:run
```

或者用 **IDEA** 打开 `spring-boot-backend` 模块直接 Run。

启动后访问：
- 接口根地址：`http://localhost:8080`
- Knife4j 接口文档：`http://localhost:8080/doc.html`

### 4. 启动前端

```bash
cd vue-3d
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`，用上面任意一个账号登录。

## 生产部署（v2.1）

部署到生产环境（阿里云 ECS + Cloudflare Pages + Cloudflare 反代）的完整流程 + 7 个生产专属踩坑（CORS token-level 匹配 / PowerShell HTTPS body 破坏 / el-tag 浅色对比度 / 中国大陆访问不到 *.workers.dev / NS+A 记录 Proxied 方案 / ssh 密码粘贴 ESC sequence / 1.6 GiB 内存压榨）详见：

👉 **[TROUBLESHOOTING.md 14.13+ 章节](./TROUBLESHOOTING.md)**

**30 秒速查**：

```bash
# 前端：git push → Cloudflare Pages 自动构建部署
cd vue-3d
git add . && git commit -m "..." && git push

# 后端：scp jar + systemctl restart
cd ../spring-boot-backend
mvn clean package -DskipTests
scp target/print-club-backend.jar root@8.137.80.194:/opt/printclub/app/
ssh root@8.137.80.194 "systemctl restart printclub && journalctl -u printclub -n 30 -f"
```

**部署前检查清单**：
- ✅ `application-prod.yml` 用 `${DB_PASSWORD:please-set-via-env-var}` 占位符（**绝对不能提交真实密码**）
- ✅ `vue-3d/.env.production` 的 `VITE_API_BASE_URL=https://api.3dprint.ccwu.cc/api`
- ✅ 阿里云 ECS 防火墙开 80/TCP（22 默认开）
- ✅ nginx + Spring Boot 双保险 CORS（详见 [TROUBLESHOOTING.md 14.13](./TROUBLESHOOTING.md)）
- ✅ 所有 el-tag 加 `effect="dark"`（浅色主题对比度）

## 已完成模块

### 后端（10 大模块 / 80+ 接口）
- ✅ **基础设施**：统一返回 / 异常 / JWT 拦截器 / CORS / Knife4j / 文件上传
- ✅ **auth**（D）：登录 / 当前用户信息（BCrypt + JWT）
- ✅ **user**（A）：成员列表 / 改角色 / 改技能等级 / 改密码 / 改个人信息
- ✅ **task**（E）：任务申请 / 审批 / 驳回 / 分配打印机 / 开始打印 / 完成 / 取件 / 取消
- ✅ **project**（E）：立项 / 阶段管理 / 成员管理 / 文件 / 完成 / 取消
- ✅ **artwork**（F）：作品列表 / 我的作品 / 详情 / 创建（手动登记，不自动归档）/ 编辑 / 推荐 / 删除 / 浏览量 / **作者姓名注入**
- ✅ **material**（F）：库存 / 入库 / 操作流水 / 汇总 / 预警
- ✅ **printer**（F）：打印机台账 / 维护记录 / 状态变更 / 可分配列表（**自动过滤 QUEUED + PRINTING**）
- ✅ **stats**（F）：数据驾驶舱（核心数字 + 7 天趋势 + 月度柱状 + 状态分布 + 耗材排行 + 活跃社员）
- ✅ **log**（F）：系统日志（写入由 6 个 service 共 25+ 关键操作点触发，@Async 异步）

> **任务完成流程（v2）**：任务完成（DONE）**不再自动**归档到作品库。作品由用户**手动**通过"我的作品 → 登记作品"入口上传（带照片 + 心得）。详见 [CHANGELOG.md](./CHANGELOG.md) ②。

### 前端（25+ 页面 / 5 大模块）
- ✅ **基础**：登录 / 首页（Dashboard 普通用户版） / 个人中心（改密码 + 改信息）
- ✅ **M2 任务社员端**：任务申请 / 我的任务 / 任务详情 / 打印队列
- ✅ **M3 项目社员端**：项目列表 / 项目详情（申请加入 / 查看阶段 / 查看成员）
- ✅ **M4 作品库**：作品列表 / 作品详情 / 我的作品 / 登记作品 / 编辑作品
- ✅ **M5 设备耗材管理**：打印机台账 / 维护记录 / 耗材库存 / 耗材入库 / 耗材流水
- ✅ **M6 管理后台**：统计看板 / 待审批任务 / 进行中任务 / 历史任务 / 任务统计 / 作品推荐 / 项目管理 / 项目立项 / 成员管理 / 系统日志

## 视觉设计 v2（深海蓝金）

- **主色**：深海蓝 `#0A2540`（权威 / 商务 / 企业级）
- **辅色**：薄荷青 `#00D4AA`（成功 / 强调 / 活力）
- **点缀**：金色 `#FFD700`（警告 / 重要 / 排名）
- **背景**：浅色 `#F6F9FC` / 深色 `#0B1424`（暗色模式）
- **统一弹窗**：AppDialog 组件（圆角 16px + 顶部 6px 渐变 + 弹跳动画 + 右上角圆形关闭按钮）
- **全局动效**：按钮 hover 上浮 + 主题色阴影 / 菜单 hover 左移 / 表格 hover 浅紫 / 输入框 focus 薄荷青光晕
- **路由切换**：6 种动画（fade / slide-up / zoom / slide-left / flip / slide-down）

完整规范见 [docs/STYLE_GUIDE.md](./docs/STYLE_GUIDE.md)。

## 安全提醒

`application-dev.yml` 默认使用 `${DB_PASSWORD:root}` 占位符，**请勿将真实密码提交到 Git 仓库**。

- 本机开发：在环境变量里设 `DB_PASSWORD=xxx` 即可
- CI/CD：用密钥管理服务（GitHub Secrets / Vault）注入
- 部署：使用 `application-prod.yml` 覆盖

`.gitignore` 已屏蔽：
- `spring-boot-backend/uploads/`（运行时上传文件）
- `spring-boot-backend/src/main/resources/static/uploads/`（占位图）
- `vue-3d/.env.local` / `vue-3d/.env.*.local`（本地环境变量）
- `spring-boot-backend/src/main/resources/application-local.yml`（本地后端配置）
- IDE 配置（`.idea/`、`.vscode/`、`.DS_Store`）
- 临时文件（`*.bak`、`*.tmp`、`*.swp`）

## 文档

### 项目文档（设计 + 规划）
- 需求规格说明书：[docs/requirements_v2.md](./docs/requirements_v2.md)
- 前端开发大纲：[docs/frontend_outline_v1.md](./docs/frontend_outline_v1.md)
- 后端开发大纲：[docs/backend_outline_v1.md](./docs/backend_outline_v1.md)
- 团队分工 + 接口契约：[docs/team_outline_v1.md](./docs/team_outline_v1.md)
- 数据库设计 SQL：[docs/db_design.sql](./docs/db_design.sql)
- 变更日志：[CHANGELOG.md](./CHANGELOG.md)

### 工程文档（开发用）

| 文档 | 用途 | 谁该读 |
|---|---|---|
| **[STYLE_GUIDE.md](./docs/STYLE_GUIDE.md)** | v2 视觉规范（配色 + 弹窗 + 动效 + dark mode） | 前端 + UI 设计 |
| **[CONTRIBUTING.md](./CONTRIBUTING.md)** | 分支命名、commit 规范、PR 流程 | 所有贡献者 |
| **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** | 13 大类常见问题 + 解决方案 | 遇到问题时 |
| **[DATA_DICTIONARY.md](./DATA_DICTIONARY.md)** | 11 张表的字段含义 + 枚举值 | 后端 + 前端 + 测试 |
| **[PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md)** | 4 角色 × 所有接口/页面/操作权限 | 后端 + 前端 + 产品 |
| **[API.md](./API.md)** | 26 个接口的完整定义 + curl 示例 | 前端 + 联调 + 测试 |

## 许可

本项目仅用于课程学习与团队内部交流，未做商用授权。
