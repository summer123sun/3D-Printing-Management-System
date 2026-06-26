# 后端（Spring Boot 3 + JDK 17 + MyBatis-Plus + JWT）

3D 打印科创会管理系统的后端服务。

## 技术栈

- **Spring Boot** 3.3.5
- **JDK** 17
- **MyBatis-Plus** 3.5.9
- **MySQL** 8.0+（驱动 `com.mysql:mysql-connector-j`）
- **JWT** 0.12.6（jjwt-api / jjwt-impl / jjwt-jackson）
- **Knife4j** 4.5.0（Swagger UI 中文版）
- **Hutool** 5.8.x（工具集）
- **Lombok**（简化 getter/setter）

## 快速启动

```bash
# 1. 准备数据库：见根 README + docs/SQL/init-bcrypt.sql
# 2. 注入数据库密码并启动
DB_PASSWORD=你的密码 mvn spring-boot:run

# 或者：直接修改 src/main/resources/application-dev.yml 的 password 字段
mvn spring-boot:run
```

启动后：

- 接口根地址：`http://localhost:8080`
- Knife4j 接口文档：`http://localhost:8080/doc.html`

## 模块清单

| 模块 | 包路径 | 状态 | 负责人 |
|---|---|---|---|
| 基础设施 | `com.printclub.common` | ✅ 完成 | D |
| 登录鉴权 | `com.printclub.module.auth` | ✅ 完成 | D |
| 用户管理 | `com.printclub.module.user` | ⏳ TODO | F |
| **任务** | `com.printclub.module.task` | ✅ **完成** | **E** |
| **项目** | `com.printclub.module.project` | ✅ **完成** | **E** |
| 作品库 | `com.printclub.module.artwork` | ⏳ TODO | F |
| 设备/耗材 | `com.printclub.module.device` | ⏳ TODO | F |
| 统计 | `com.printclub.module.stats` | ⏳ TODO | F |
| 操作日志 | `com.printclub.module.log` | ⏳ TODO | F |

## 目录约定

```
module/<name>/
├─ controller/   # REST 接口
├─ service/      # 业务接口
│  └─ impl/      # 业务实现
├─ mapper/       # MyBatis-Plus Mapper
├─ entity/       # 数据库实体
└─ dto/          # 请求/响应 DTO
```

## 通用返回格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1700000000000
}
```

异常情况 `code` 会是 4xx/5xx，`message` 是用户可读的错误信息。

## 鉴权约定

- 登录返回 JWT，存到请求头 `Authorization: Bearer <token>`
- 自定义注解 `@RequireAuth` 强制要求登录
- 自定义注解 `@RequireRole("ADMIN")` 强制要求某角色
- 拦截器 `JwtInterceptor` 自动从请求头解析用户信息到 `SecurityContext`

## 数据库

见根目录的 [docs/db_design.sql](../docs/db_design.sql) 与
[spring-boot-backend/docs/SQL/init-bcrypt.sql](docs/SQL/init-bcrypt.sql)。

> **安全提示**：`application-dev.yml` 用 `${DB_PASSWORD:root}` 占位符，
> 不要把真实密码 commit 到仓库。生产部署用 KMS 注入。
