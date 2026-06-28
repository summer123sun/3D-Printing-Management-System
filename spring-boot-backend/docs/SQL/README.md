# SQL 脚本目录

> **权威 SQL 在这里** — 一键建库 + 完整模拟数据（v2 统一版）。

## 文件清单

| 文件 | 大小 | 用途 |
|---|---|---|
| **`init-bcrypt.sql`** | 38 KB | **🚀 唯一权威脚本**：建库 + 删表 + 建表 + 模拟数据（BCrypt 密码） |
| **`README.md`** | — | 本说明 |

## 快速使用

```bash
# 1. 登录 MySQL
mysql -uroot -p

# 2. 一键初始化（推荐）
source /path/to/spring-boot-backend/docs/SQL/init-bcrypt.sql
```

## 初始化内容

- **11 张表**（utf8mb4 字符集 + InnoDB 引擎 + 完整外键 + 索引）
- **29 名成员**（4 个角色：社长 / 技术骨干 / 普通社员 / 新成员）
- **3 台打印机**（含品牌/型号/累计时长/位置）
- **20+ 打印任务**（覆盖待审批 / 排队 / 打印中 / 已完成 / 已取件 / 已取消 全流程）
- **4 个项目**（含阶段/成员/文件）
- **12 个作品**（含预览图/成品照/心得）
- **9 条耗材流水**（入库 + 任务消耗）

## 4 个测试账号（密码全部 `123456`）

| 学号 | 姓名 | 角色 | 主要权限 |
|---|---|---|---|
| `2023010001` | 张明 | 社长 (1) | 全权限 + 系统日志 |
| `2023010002` | 李强 | 技术骨干 (2) | 审批 / 分配 / 完成 |
| `2023010005` | 刘洋 | 普通社员 (3) | 申请任务 / 作品库 |
| `2024010001` | 马超 | 新成员 (4) | 受限（只能看公开内容） |

## 设计参考

- **schema only**（无数据）：[`docs/db_design.sql`](../../../docs/db_design.sql) — 11 张表的完整定义
- **字段说明**：[`DATA_DICTIONARY.md`](../../../DATA_DICTIONARY.md)
- **ER 关系图**：见 `docs/requirements_v2.md` / `docs/db_design.sql` 注释

## 编码

- **文件编码**：UTF-8
- **数据库字符集**：utf8mb4
- **排序规则**：utf8mb4_unicode_ci
- **避免中文乱码**三件套：JDBC URL `characterEncoding=UTF-8` + `connectionCollation=utf8mb4_unicode_ci` + `server.servlet.encoding.force=true`

## 安全

- **密码统一为 `123456`**，已用 BCrypt 加密（不同用户不同密文）
- **真实生产** 部署时改 `application-prod.yml` 注入强密码
- **.gitignore** 屏蔽 `*.sql` 之外的所有 .env / 密码文件

## 历史

- **2026-06-25 v2.0**：合并 01/02/03 拆分为单个 `init-bcrypt.sql`，统一 utf8mb4，加完整审计日志表
- 2026-05 v1.x：早期 3 拆分文件 + GBK 编码
