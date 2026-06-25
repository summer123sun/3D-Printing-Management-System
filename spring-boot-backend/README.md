# 3D 打印科创会管理系统 - 后端

> Spring Boot 3 + MyBatis-Plus + JWT + Knife4j
> 对应前端：[vue-3d](../vue-3d/)
> 接口契约：用 [Apifox](https://www.apifox.cn/) 维护（D 建项目）

## 快速开始

```bash
# 1. 准备 MySQL 8.0，建库 + 初始化数据
mysql -u root -p < docs/SQL/03-init.sql

# 2. 启动 Spring Boot
mvn spring-boot:run
# 或 IDE 里跑 PrintClubApplication.java

# 3. 访问接口文档
http://localhost:8080/doc.html
```

## 3 人分工

- **D**：基础设施（项目脚手架 + 鉴权 + 用户 + 文件上传）
- **E**（前端 B）：核心业务（**task/** + **project/**）
- **F**：辅助业务（artwork/ + printer/ + material/ + stats/ + log/）

详细分工见：[后端开发大纲 v1](../../documents/3D打印科创会管理系统_后端开发大纲_v1.md)
