# 数据字典（DATA_DICTIONARY.md）

> 11 张表的字段含义 + 枚举值 + 关联关系。
> 后端开发 / 前端对接 / 测试 / 文档都靠这一篇。

---

## 目录

| 表名 | 中文 | 模块 | 关键 |
|---|---|---|---|
| `member` | 成员 | user | 4 种角色 |
| `login_log` | 登录日志 | user | 审计 |
| `operation_log` | 操作日志 | user | 审计 |
| `printer` | 打印机 | printer | 设备状态 |
| `material` | 材料库存 | material | 库存预警 |
| `print_task` | 打印任务 | task | 核心业务 |
| `task_timeline` | 任务时间线 | task | 状态流转 |
| `project` | 项目 | project | 核心业务 |
| `project_member` | 项目成员 | project | 多对多 |
| `project_progress` | 项目阶段 | project | 时间线 |
| `project_file` | 项目文件 | project | 文件元数据 |
| `material_log` | 材料消耗日志 | material | 审计 |

> 表结构定义在：`docs/db_design.sql` 和 `spring-boot-backend/src/main/resources/db/migration/V1__init.sql`

---

## 1. 通用约定

### 1.1 公共字段（每张表都有）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `BIGINT UNSIGNED AUTO_INCREMENT` | 主键，雪花算法可改 |
| `create_time` | `DATETIME DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |
| `deleted` | `TINYINT DEFAULT 0` | 软删除标记（0=未删，1=已删） |

### 1.2 命名约定

- **表名**：snake_case，单数（如 `member` 不是 `members`）
- **字段名**：snake_case（如 `student_id` 不是 `studentId`）
- **外键**：`<关联表名单数>_id`（如 `project_id`、`member_id`）
- **索引**：`idx_<表名>_<字段名>`（如 `idx_task_status`）
- **唯一约束**：`uk_<表名>_<字段名>`

---

## 2. `member`（成员表）

> 系统的核心用户表。存学生/教师/管理员的信息。

### 2.1 字段

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | AUTO | 主键 |
| `student_id` | VARCHAR(20) | ✅ | - | 学号（唯一，自然人标识） |
| `name` | VARCHAR(50) | ✅ | - | 姓名 |
| `password` | VARCHAR(100) | ✅ | - | bcrypt 加密后的密码 |
| `role` | TINYINT | ✅ | 1 | 角色（见下表） |
| `email` | VARCHAR(100) | ❌ | NULL | 邮箱 |
| `phone` | VARCHAR(20) | ❌ | NULL | 手机号 |
| `avatar` | VARCHAR(255) | ❌ | NULL | 头像 URL |
| `major` | VARCHAR(50) | ❌ | NULL | 专业 |
| `grade` | VARCHAR(10) | ❌ | NULL | 年级（如 "2021"） |
| `skills` | VARCHAR(255) | ❌ | NULL | 技能标签，逗号分隔 |
| `status` | TINYINT | ✅ | 1 | 账号状态（1=正常，0=禁用） |
| `last_login_time` | DATETIME | ❌ | NULL | 上次登录时间 |
| `last_login_ip` | VARCHAR(50) | ❌ | NULL | 上次登录 IP |
| 公共字段 | | | | |

### 2.2 `role` 角色枚举

| 值 | 名称 | 英文 | 权限 |
|---|---|---|---|
| 1 | 普通社员 | MEMBER | 提交任务、查看项目、查看自己数据 |
| 2 | 技术骨干 | STAFF | 普通社员权限 + 审批任务、分配打印机、查看所有任务 |
| 3 | 项目负责人 | LEADER | 技术骨干权限 + 创建/编辑/完成/取消项目 |
| 9 | 系统管理员 | ADMIN | 所有权限 + 用户管理 + 设备管理 + 库存管理 |

### 2.3 `status` 账号状态

| 值 | 含义 |
|---|---|
| 0 | 禁用（不能登录） |
| 1 | 正常 |

### 2.4 索引

- `UNIQUE KEY uk_student_id (student_id)`
- `KEY idx_role (role)`
- `KEY idx_status (status)`

### 2.5 测试账号（`init-bcrypt.sql` 里的）

| 学号 | 姓名 | 角色 | 密码 |
|---|---|---|---|
| `2021001` | 张三 | 1（社员） | `123456` |
| `2021002` | 李四 | 2（技术骨干） | `123456` |
| `2021003` | 王五 | 3（项目负责人） | `123456` |
| `admin` | 系统管理员 | 9（管理员） | `123456` |

---

## 3. `login_log`（登录日志）

### 3.1 字段

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | 主键 |
| `member_id` | VARCHAR(20) | ✅ | 登录的学号（不关联外键，删成员不删日志） |
| `ip` | VARCHAR(50) | ❌ | 登录 IP |
| `user_agent` | VARCHAR(255) | ❌ | 浏览器 UA |
| `status` | TINYINT | ✅ | 1=成功，0=失败 |
| `fail_reason` | VARCHAR(100) | ❌ | 失败原因（密码错/账号禁用） |
| `login_time` | DATETIME | ✅ | 登录时间 |

### 3.2 索引

- `KEY idx_member_id (member_id)`
- `KEY idx_login_time (login_time)`

---

## 4. `operation_log`（操作日志）

### 4.1 字段

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | 主键 |
| `member_id` | VARCHAR(20) | ✅ | 操作人学号 |
| `module` | VARCHAR(20) | ✅ | 模块（task/project/user/...） |
| `action` | VARCHAR(50) | ✅ | 动作（create/update/delete/approve/...） |
| `target_id` | VARCHAR(50) | ❌ | 操作对象 ID |
| `target_type` | VARCHAR(20) | ❌ | 对象类型（task/project/member/...） |
| `detail` | JSON | ❌ | 改动详情（如 `{"status": {"old": 1, "new": 5}}`） |
| `ip` | VARCHAR(50) | ❌ | 操作 IP |
| `operate_time` | DATETIME | ✅ | 操作时间 |

### 4.2 索引

- `KEY idx_member_id (member_id)`
- `KEY idx_module_action (module, action)`
- `KEY idx_target (target_type, target_id)`
- `KEY idx_operate_time (operate_time)`

---

## 5. `printer`（打印机）

### 5.1 字段

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | AUTO | 主键 |
| `printer_code` | VARCHAR(50) | ✅ | - | 打印机编号（如 `PR-001`，唯一） |
| `printer_name` | VARCHAR(100) | ✅ | - | 打印机名称（如 "创想三维 K1"） |
| `model` | VARCHAR(50) | ❌ | NULL | 型号 |
| `brand` | VARCHAR(50) | ❌ | NULL | 品牌 |
| `build_volume` | VARCHAR(50) | ❌ | NULL | 成型尺寸（如 "220x220x250mm"） |
| `supported_materials` | VARCHAR(255) | ❌ | NULL | 支持的材料（逗号分隔） |
| `status` | TINYINT | ✅ | 1 | 状态（见下表） |
| `location` | VARCHAR(100) | ❌ | NULL | 位置（如 "打印室A-3号机位"） |
| `total_print_hours` | INT UNSIGNED | ✅ | 0 | 累计打印小时数 |
| `total_print_count` | INT UNSIGNED | ✅ | 0 | 累计打印次数 |
| `last_used_time` | DATETIME | ❌ | NULL | 上次使用时间 |
| `remark` | VARCHAR(255) | ❌ | NULL | 备注 |

### 5.2 `status` 设备状态

| 值 | 名称 | 含义 | 是否可分配任务 |
|---|---|---|---|
| 0 | 离线 | 关机/断网 | ❌ |
| 1 | 空闲 | 在线无任务 | ✅ |
| 2 | 打印中 | 正在打印 | ❌ |
| 3 | 维护中 | 维修/保养 | ❌ |
| 4 | 故障 | 异常停机 | ❌ |

---

## 6. `material`（材料库存）

### 6.1 字段

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | AUTO | 主键 |
| `material_code` | VARCHAR(50) | ✅ | - | 材料编码（如 `PLA-RED-1KG`，唯一） |
| `material_name` | VARCHAR(100) | ✅ | - | 材料名称 |
| `material_type` | VARCHAR(20) | ✅ | - | 类型（PLA/ABS/PETG/TPU/RESIN） |
| `color` | VARCHAR(20) | ❌ | NULL | 颜色 |
| `spec` | VARCHAR(50) | ❌ | NULL | 规格（如 "1.75mm"） |
| `unit` | VARCHAR(10) | ✅ | 'g' | 单位（g/kg/卷） |
| `total_stock` | DECIMAL(10,2) | ✅ | 0 | 当前库存 |
| `warning_threshold` | DECIMAL(10,2) | ✅ | 100 | 库存预警阈值（低于这个值会高亮） |
| `unit_price` | DECIMAL(10,2) | ❌ | NULL | 单价（元） |
| `supplier` | VARCHAR(100) | ❌ | NULL | 供应商 |
| `status` | TINYINT | ✅ | 1 | 1=在用，0=停用 |

### 6.2 `material_type` 常用类型

| 类型 | 全称 | 适用 |
|---|---|---|
| PLA | 聚乳酸 | 一般打印，入门 |
| ABS | 工程塑料 | 高强度件 |
| PETG | 聚酯 | 户外/食品接触 |
| TPU | 热塑性聚氨酯 | 柔性件 |
| RESIN | 光固化树脂 | 高精度件 |

---

## 7. `print_task`（打印任务）⭐核心表

### 7.1 字段

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | AUTO | 主键 |
| `task_id` | VARCHAR(32) | ✅ | - | 业务 ID（UUID 前 8 位 + 日期，如 `T20251208A1B2`） |
| `title` | VARCHAR(100) | ✅ | - | 任务标题 |
| `description` | TEXT | ❌ | NULL | 任务描述 |
| `applicant_id` | VARCHAR(20) | ✅ | - | 申请人学号 |
| `applicant_name` | VARCHAR(50) | ❌ | NULL | 申请人姓名（冗余） |
| `stl_file_id` | BIGINT UNSIGNED | ❌ | NULL | STL 文件 ID（关联 file 表） |
| `stl_file_url` | VARCHAR(255) | ❌ | NULL | STL 文件 URL |
| `material_id` | BIGINT UNSIGNED | ✅ | - | 材料 ID |
| `material_type` | VARCHAR(20) | ❌ | NULL | 材料类型（冗余） |
| `color` | VARCHAR(20) | ❌ | NULL | 颜色 |
| `weight_grams` | DECIMAL(10,2) | ❌ | NULL | 预计重量（克） |
| `estimated_hours` | DECIMAL(5,2) | ❌ | NULL | 预计打印小时数 |
| `priority` | TINYINT | ✅ | 2 | 优先级（1=紧急，2=普通，3=低优） |
| `status` | TINYINT | ✅ | 1 | 状态（见下表） |
| `project_id` | BIGINT UNSIGNED | ❌ | NULL | 关联项目 ID |
| `printer_id` | BIGINT UNSIGNED | ❌ | NULL | 分配的打印机 ID |
| `approver_id` | VARCHAR(20) | ❌ | NULL | 审批人学号 |
| `approve_time` | DATETIME | ❌ | NULL | 审批时间 |
| `approve_remark` | VARCHAR(255) | ❌ | NULL | 审批备注 |
| `reject_reason` | VARCHAR(255) | ❌ | NULL | 驳回原因 |
| `start_time` | DATETIME | ❌ | NULL | 开始打印时间 |
| `finish_time` | DATETIME | ❌ | NULL | 完成时间 |
| `actual_hours` | DECIMAL(5,2) | ❌ | NULL | 实际打印小时数 |
| `pickup_time` | DATETIME | ❌ | NULL | 取件时间 |
| `pickup_signature` | VARCHAR(255) | ❌ | NULL | 取件签名图片 URL |

### 7.2 `status` 任务状态（核心枚举）

| 值 | 名称 | 英文 | 触发条件 | 下一个状态 |
|---|---|---|---|---|
| 1 | 待审批 | PENDING | 申请人提交 | APPROVED(2) / REJECTED(3) |
| 2 | 已通过 | APPROVED | 技术骨干审批通过 | QUEUED(4) |
| 3 | 已驳回 | REJECTED | 技术骨干驳回 | 终态 |
| 4 | 排队中 | QUEUED | 进入打印队列 | PRINTING(5) |
| 5 | 打印中 | PRINTING | 分配打印机并开始 | DONE(6) / FAILED |
| 6 | 已完成 | DONE | 打印完成 | 终态 |
| 7 | 已取消 | CANCELLED | 申请人取消 | 终态 |

**状态流转图**：

```
[1 待审批] ──approve──> [2 已通过] ──queue──> [4 排队中] ──assign──> [4 排队中(已分配)]
   │                        │                      │                       │
   │                        │                      └──cancel──> [7 已取消]   │
   │                        │                                              │
   │                        └──reject──> [3 已驳回]                        │
   │                                                                       │
   └──cancel──> [7 已取消]                                                ▼
                                                                  [5 打印中] ──finish──> [6 已完成]
                                                                                                │
                                                                                                └──pickup──> [6 已完成(已取件)]
```

---

## 8. `task_timeline`（任务时间线）

### 8.1 字段

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | 主键 |
| `task_id` | VARCHAR(32) | ✅ | 关联任务 |
| `action` | VARCHAR(50) | ✅ | 动作（apply/approve/reject/queue/assign/start/finish/cancel/pickup） |
| `operator_id` | VARCHAR(20) | ❌ | 操作人学号 |
| `operator_name` | VARCHAR(50) | ❌ | 操作人姓名 |
| `from_status` | TINYINT | ❌ | 操作前状态 |
| `to_status` | TINYINT | ❌ | 操作后状态 |
| `remark` | VARCHAR(255) | ❌ | 备注 |
| `operate_time` | DATETIME | ✅ | 操作时间 |

### 8.2 `action` 动作枚举

| 值 | 动作 | 触发人 |
|---|---|---|
| apply | 提交申请 | 申请人 |
| approve | 审批通过 | 技术骨干 |
| reject | 驳回 | 技术骨干 |
| queue | 进入队列 | 系统自动 |
| assign | 分配打印机 | 技术骨干 |
| start | 开始打印 | 技术骨干 |
| finish | 标记完成 | 技术骨干 |
| cancel | 取消任务 | 申请人 |
| pickup | 取件 | 申请人 |

---

## 9. `project`（项目）⭐核心表

### 9.1 字段

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | AUTO | 主键 |
| `project_name` | VARCHAR(100) | ✅ | - | 项目名 |
| `project_code` | VARCHAR(50) | ✅ | - | 项目编号（如 `PRJ-2025-001`，唯一） |
| `project_type` | TINYINT | ✅ | 1 | 类型（见下表） |
| `description` | TEXT | ❌ | NULL | 项目描述 |
| `leader_id` | VARCHAR(20) | ✅ | - | 项目负责人学号 |
| `leader_name` | VARCHAR(50) | ❌ | NULL | 负责人姓名（冗余） |
| `status` | TINYINT | ✅ | 0 | 状态（见下表） |
| `start_date` | DATE | ✅ | - | 计划开始日期 |
| `end_date` | DATE | ❌ | NULL | 计划结束日期 |
| `actual_end_date` | DATE | ❌ | NULL | 实际结束日期 |
| `budget` | DECIMAL(10,2) | ❌ | NULL | 预算（元） |
| `cover_image` | VARCHAR(255) | ❌ | NULL | 封面图 URL |
| `tags` | VARCHAR(255) | ❌ | NULL | 标签（逗号分隔） |

### 9.2 `project_type` 项目类型

| 值 | 名称 | 含义 |
|---|---|---|
| 1 | 竞赛 | 参加比赛的项目 |
| 2 | 科研 | 教师指导的科研项目 |
| 3 | 教学 | 课程配套项目 |
| 4 | 社团活动 | 社团日常活动 |
| 5 | 其他 | 其他 |

### 9.3 `status` 项目状态

| 值 | 名称 | 含义 | 可操作 |
|---|---|---|---|
| 0 | 筹备中 | 已立项但未开始 | 编辑、删除、启动 |
| 1 | 进行中 | 正常推进中 | 编辑、添加阶段/成员、完成、取消 |
| 2 | 已完成 | 正常结束 | 只读 |
| 3 | 已取消 | 主动取消 | 只读 |

---

## 10. `project_member`（项目成员）

> 多对多关联表：一个项目有多个成员，一个成员可以参与多个项目。

### 10.1 字段

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | AUTO | 主键 |
| `project_id` | BIGINT UNSIGNED | ✅ | - | 项目 ID |
| `member_id` | VARCHAR(20) | ✅ | - | 成员学号 |
| `member_name` | VARCHAR(50) | ❌ | NULL | 成员姓名（冗余） |
| `role_in_project` | TINYINT | ✅ | 3 | 项目内角色（见下表） |
| `contribution` | VARCHAR(255) | ❌ | NULL | 贡献描述 |
| `join_time` | DATETIME | ✅ | NOW() | 加入时间 |
| `status` | TINYINT | ✅ | 1 | 1=在项目中，0=已退出 |

### 10.2 `role_in_project` 项目内角色

| 值 | 名称 | 权限 |
|---|---|---|
| 1 | 项目负责人 | 所有项目操作 |
| 2 | 核心成员 | 编辑阶段、添加成员、关联任务 |
| 3 | 普通成员 | 查看、提交关联任务 |

### 10.3 索引

- `UNIQUE KEY uk_project_member (project_id, member_id)`

---

## 11. `project_progress`（项目阶段）

### 11.1 字段

| 字段 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | AUTO | 主键 |
| `project_id` | BIGINT UNSIGNED | ✅ | - | 项目 ID |
| `stage_name` | VARCHAR(100) | ✅ | - | 阶段名（如 "建模"、"打印"、"后处理"） |
| `stage_order` | INT | ✅ | - | 阶段顺序（从 1 开始） |
| `description` | TEXT | ❌ | NULL | 阶段描述 |
| `status` | TINYINT | ✅ | 0 | 状态（见下表） |
| `planned_start_date` | DATE | ❌ | NULL | 计划开始 |
| `planned_end_date` | DATE | ❌ | NULL | 计划结束 |
| `actual_start_date` | DATE | ❌ | NULL | 实际开始 |
| `actual_end_date` | DATE | ❌ | NULL | 实际结束 |
| `progress_percent` | TINYINT UNSIGNED | ✅ | 0 | 进度 0-100 |

### 11.2 `status` 阶段状态

| 值 | 名称 | 颜色 | 含义 |
|---|---|---|---|
| 0 | 未开始 | 灰色 | 还没到计划开始时间 |
| 1 | 进行中 | 蓝色 | 正在进行 |
| 2 | 已完成 | 绿色 | 完成 |
| 3 | 已延期 | 红色 | 超过计划结束时间未完成 |
| 4 | 已跳过 | 橙色 | 不再需要 |

---

## 12. `project_file`（项目文件）

### 12.1 字段

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | 主键 |
| `project_id` | BIGINT UNSIGNED | ✅ | 项目 ID |
| `file_name` | VARCHAR(255) | ✅ | 文件名 |
| `file_path` | VARCHAR(500) | ✅ | 存储路径 |
| `file_size` | BIGINT UNSIGNED | ✅ | 文件大小（字节） |
| `file_type` | VARCHAR(20) | ❌ | 类型（stl/3mf/obj/gcode/img/doc/other） |
| `uploader_id` | VARCHAR(20) | ✅ | 上传人学号 |
| `uploader_name` | VARCHAR(50) | ❌ | 上传人姓名（冗余） |
| `description` | VARCHAR(255) | ❌ | 文件描述 |
| `upload_time` | DATETIME | ✅ | 上传时间 |

---

## 13. `material_log`（材料消耗日志）

### 13.1 字段

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | BIGINT UNSIGNED | ✅ | 主键 |
| `material_id` | BIGINT UNSIGNED | ✅ | 材料 ID |
| `task_id` | VARCHAR(32) | ✅ | 关联任务 ID |
| `change_type` | TINYINT | ✅ | 变化类型（1=扣减，2=补充） |
| `change_amount` | DECIMAL(10,2) | ✅ | 变化数量 |
| `stock_before` | DECIMAL(10,2) | ✅ | 变化前库存 |
| `stock_after` | DECIMAL(10,2) | ✅ | 变化后库存 |
| `operator_id` | VARCHAR(20) | ✅ | 操作人学号 |
| `remark` | VARCHAR(255) | ❌ | 备注 |
| `operate_time` | DATETIME | ✅ | 操作时间 |

### 13.2 `change_type` 变化类型

| 值 | 含义 | 触发 |
|---|---|---|
| 1 | 扣减 | 任务打印完成自动扣减 |
| 2 | 补充 | 管理员手动补充库存 |

---

## 14. 关键关联关系图

```
                                    ┌────────────┐
                                    │   member   │
                                    └──────┬─────┘
                                           │ student_id
            ┌──────────────────────────────┼──────────────────────────┐
            │                              │                          │
            ▼                              ▼                          ▼
     ┌────────────┐              ┌─────────────────┐         ┌────────────────┐
     │ login_log  │              │   print_task    │         │ project_member │
     └────────────┘              │  (申请人/审批人) │         └────────┬───────┘
                                 └────────┬────────┘                  │
                                          │ task_id                   │ project_id
                                          │                           ▼
                                          │                  ┌─────────────────┐
                                          │                  │     project     │
                                          │                  └────────┬────────┘
                                          │                           │
                ┌─────────────────────────┼───────────────────────────┼────────────────────┐
                ▼                         ▼                           ▼                    ▼
        ┌──────────────┐         ┌──────────────────┐         ┌─────────────────┐  ┌─────────────────┐
        │task_timeline │         │   material_log   │         │project_progress │  │ project_file    │
        └──────────────┘         └──────────────────┘         └─────────────────┘  └─────────────────┘
                                          │
                                          │ material_id
                                          ▼
                                  ┌──────────────┐
                                  │   material   │
                                  └──────────────┘

        ┌──────────────────┐         ┌──────────────────┐
        │ operation_log    │         │     printer      │
        │ (审计任意操作)    │         │  (被 task 引用)  │
        └──────────────────┘         └──────────────────┘
```

---

## 15. 常用查询 SQL 示例

### 15.1 查询某个成员的待办任务

```sql
SELECT *
FROM print_task
WHERE applicant_id = '2021001'
  AND status IN (1, 4, 5)  -- 待审批 / 排队中 / 打印中
  AND deleted = 0
ORDER BY priority ASC, apply_time DESC;
```

### 15.2 查询某个项目的所有信息（4 张表 join）

```sql
SELECT
  p.*,
  pm.member_id, pm.role_in_project, pm.contribution,
  pp.stage_name, pp.stage_order, pp.status AS stage_status, pp.progress_percent,
  pf.file_name, pf.file_path, pf.file_size
FROM project p
LEFT JOIN project_member pm ON p.id = pm.project_id AND pm.status = 1
LEFT JOIN project_progress pp ON p.id = pp.project_id
LEFT JOIN project_file pf ON p.id = pf.project_id
WHERE p.id = 1 AND p.deleted = 0
ORDER BY pm.role_in_project ASC, pp.stage_order ASC;
```

### 15.3 查询材料库存预警

```sql
SELECT *
FROM material
WHERE total_stock < warning_threshold
  AND status = 1
  AND deleted = 0
ORDER BY (total_stock / warning_threshold) ASC;
```

### 15.4 查询某月任务统计

```sql
SELECT
  DATE_FORMAT(apply_time, '%Y-%m') AS month,
  COUNT(*) AS total,
  SUM(CASE WHEN status = 6 THEN 1 ELSE 0 END) AS done_count,
  SUM(CASE WHEN status = 7 THEN 1 ELSE 0 END) AS cancelled_count,
  AVG(actual_hours) AS avg_hours
FROM print_task
WHERE deleted = 0
  AND apply_time >= '2025-01-01'
GROUP BY DATE_FORMAT(apply_time, '%Y-%m')
ORDER BY month DESC;
```

---

## 16. 数据维护约定

### 16.1 软删除

所有业务表都用 `@TableLogic` 实现软删除：

```java
@TableLogic
@TableField(fill = FieldFill.INSERT)
private Integer deleted;  // 0=正常，1=已删
```

物理删除只在以下情况：

- 测试环境重置
- 数据合规要求（如 GDPR）
- 维护脚本里**显式**注明

### 16.2 时间字段

- 所有时间字段用 `DATETIME`（不是 `TIMESTAMP`，2038 年问题）
- 数据库时区统一 `+08:00`（北京时间）
- 前端展示用 `formatDate()` 工具函数

### 16.3 金额字段

- 用 `DECIMAL(10,2)`（不用 `DOUBLE`）
- 单位：元
- 前端展示：`¥1,234.56`

---

**最后更新**：项目启动时定稿，如有变动请同步更新本文件。**
