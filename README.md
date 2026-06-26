# 3D 打印科创会管理系统

> 大学生 3D 打印兴趣社团的一站式管理平台，期末项目实战。
> 覆盖**任务流转**、**项目管理**、**设备/耗材**、**作品库**、**统计**五大场景。

## 团队分工

| 角色 | 负责模块 |
|---|---|
| 组长（后端 E + 前端 M2/M3） | 任务模块、项目模块（任务全流程 + 项目立项/阶段/成员/文件） |
| 后端 D + 前端 M1 | 基础设施（脚手架/通用工具）+ 基础页面（登录/首页/个人中心） |
| 后端 F + 前端 M4 | 作品库、设备、统计、日志 |
| 全员 | 数据库设计、需求文档、测试 |

## 目录结构

```
3D-Printing-Management-System/
+- vue-3d/                  # 前端（Vue 3 + Vite + TS + Element Plus + Pinia）
|  +- src/api/              # 接口封装
|  +- src/stores/           # Pinia 状态
|  +- src/types/            # TS 类型
|  +- src/components/       # 通用 / 业务组件
|  +- src/views/            # 页面
|  +- src/router/           # 路由
|  +- src/styles/           # 全局样式
|
+- spring-boot-backend/     # 后端（Spring Boot 3 + JDK 17 + MyBatis-Plus + JWT）
|  +- src/main/java/com/printclub/
|  |  +- common/            # 通用（Result / 异常 / JWT / 分页 / 文件上传）
|  |  +- module/
|  |  |  +- auth/           # 登录鉴权
|  |  |  +- user/           # 用户管理
|  |  |  +- task/           # 打印任务（E）
|  |  |  +- project/        # 项目（E）
|  |  |  +- artwork/        # 作品库（F）
|  |  |  +- device/         # 设备/耗材（F）
|  |  |  +- stats/          # 统计（F）
|  |  |  +- log/            # 日志（F）
|  |  +- PrintClubApplication.java
|  +- src/main/resources/
|  |  +- application.yml
|  |  +- application-dev.yml
|  |  +- application-prod.yml
|  +- docs/SQL/init-bcrypt.sql    # 一键建库建表插数据
|  +- pom.xml
|
+- docs/                    # 项目文档（英文文件名，兼容 Windows / Mac / Linux）
   +- requirements_v2.md        # 需求规格说明书 v2
   +- frontend_outline_v1.md    # 前端开发大纲
   +- backend_outline_v1.md     # 后端开发大纲
   +- db_design.sql             # 完整建表 SQL
```

## 快速启动

### 1. 准备环境

- **JDK 17+**
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

| 账号 | 姓名 | 角色 |
|---|---|---|
| `2023010001` | 张明 | 社长 |
| `2023010002` | 李强 | 技术骨干 |
| `2023010005` | 刘洋 | 普通社员 |
| `2024010001` | 马超 | 新社员（2024 级） |

### 3. 启动后端

```bash
cd spring-boot-backend

# 方式 A：用环境变量注入数据库密码（推荐，不修改文件）
DB_PASSWORD=你的密码 mvn spring-boot:run

# 方式 B：直接修改 application-dev.yml 的 password 字段
mvn spring-boot:run
```

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

## 已完成模块（截至当前）

- 基础设施（统一返回 / 异常 / JWT / CORS / Knife4j）
- 登录鉴权（JWT 颁发 + 拦截器 + 前端 Pinia 持久化）
- **任务模块（E）**：申请、审批、分配、打印、完成、取件、归档全流程
- **项目模块（E）**：立项、阶段、成员、文件、案例推荐

## TODO（其他同学负责）

- 用户管理（增删改查 / 角色分配 / 状态管理）
- 作品库（学生上传优秀作品 / 浏览 / 推荐 / 分类）
- 设备管理（打印机台账 / 维护记录 / 状态切换）
- 耗材管理（库存 / 出入库 / 预警）
- 数据统计（任务量 / 设备利用率 / 耗材消耗）
- 操作日志

## 安全提醒

`application-dev.yml` 默认使用 `${DB_PASSWORD:root}` 占位符，**请勿将真实密码提交到 Git 仓库**。

- 本机开发：在环境变量里设 `DB_PASSWORD=xxx` 即可
- 部署：用生产配置 + 密钥管理服务（KMS）注入

## 文档

### 项目文档（设计 + 规划）

- 需求规格说明书：[docs/requirements_v2.md](./docs/requirements_v2.md)
- 前端开发大纲：[docs/frontend_outline_v1.md](./docs/frontend_outline_v1.md)
- 后端开发大纲：[docs/backend_outline_v1.md](./docs/backend_outline_v1.md)
- 团队分工 + 接口契约：[docs/team_outline_v1.md](./docs/team_outline_v1.md)
- 数据库设计 SQL：[docs/db_design.sql](./docs/db_design.sql)

### 工程文档（开发用）

| 文档 | 用途 | 谁该读 |
|---|---|---|
| **[CONTRIBUTING.md](./CONTRIBUTING.md)** | 分支命名、commit 规范、PR 流程 | 所有贡献者 |
| **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** | 13 大类常见问题 + 解决方案 | 遇到问题时 |
| **[DATA_DICTIONARY.md](./DATA_DICTIONARY.md)** | 11 张表的字段含义 + 枚举值 | 后端 + 前端 + 测试 |
| **[PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md)** | 4 角色 × 所有接口/页面/操作权限 | 后端 + 前端 + 产品 |
| **[API.md](./API.md)** | 26 个接口的完整定义 + curl 示例 | 前端 + 联调 + 测试 |

## 许可

本项目仅用于课程学习与团队内部交流，未做商用授权。
