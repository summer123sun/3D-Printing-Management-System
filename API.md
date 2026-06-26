# 接口文档（API.md）

> 所有后端 REST 接口的完整定义：路径 / 方法 / 参数 / 返回值 / curl 示例。
> 配合 `PERMISSION_MATRIX.md` 一起看。

---

## 0. 通用约定

### 0.1 基础信息

| 项 | 值 |
|---|---|
| Base URL（开发） | `http://localhost:8080/api` |
| Base URL（生产） | `https://print.example.com/api` |
| 数据格式 | JSON（UTF-8） |
| 认证方式 | JWT Bearer Token（除登录外所有接口都要） |
| 文档 | `http://localhost:8080/api/doc.html`（Knife4j） |

### 0.2 统一返回格式

**成功**：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1702000000000
}
```

**失败**：

```json
{
  "code": 400,
  "message": "参数错误：title 不能为空",
  "data": null,
  "timestamp": 1702000000000
}
```

**分页**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 100,
    "pages": 10,
    "current": 1,
    "size": 10
  }
}
```

### 0.3 错误码

| 范围 | 含义 | 例子 |
|---|---|---|
| 200 | 成功 | |
| 400 | 参数错误 | `"title 不能为空"` |
| 401 | 未登录/Token 过期 | `"请先登录"` |
| 403 | 无权限 | `"仅管理员可操作"` |
| 404 | 资源不存在 | `"任务 T20251208ABCD 不存在"` |
| 409 | 冲突 | `"该项目已存在"` |
| 500 | 服务器内部错误 | `"系统异常，请联系管理员"` |

### 0.4 认证 Header

```http
GET /api/task/my HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxx.xxx
Content-Type: application/json; charset=utf-8
```

---

## 1. 认证模块（`/api/auth/*`）

### 1.1 登录

```http
POST /api/auth/login
Content-Type: application/json
```

**请求体**：

```json
{
  "studentId": "2021001",
  "password": "123456"
}
```

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdHVkZW50SWQiOiIyMDIxMDAxIiwicm9sZSI6MSwiaWF0IjoxNzAyMDAwMDAwLCJleHAiOjE3MDIwODY0MDB9.xxxxx",
    "user": {
      "id": 1,
      "studentId": "2021001",
      "name": "张三",
      "role": 1,
      "avatar": null,
      "major": "机械工程",
      "grade": "2021"
    }
  }
}
```

**curl**：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"studentId":"2021001","password":"123456"}'
```

### 1.2 登出

```http
POST /api/auth/logout
Authorization: Bearer {token}
```

**响应**：`{"code":200,"message":"success","data":null}`

### 1.3 获取当前用户信息

```http
GET /api/auth/info
Authorization: Bearer {token}
```

**响应**：`data` 字段同登录的 `user`。

### 1.4 修改密码

```http
PUT /api/auth/password
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

---

## 2. 用户模块（`/api/user/*`）

### 2.1 获取自己的信息

```http
GET /api/user/info
```

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "studentId": "2021001",
    "name": "张三",
    "role": 1,
    "roleName": "普通社员",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "avatar": null,
    "major": "机械工程",
    "grade": "2021",
    "skills": ["建模", "切片"],
    "status": 1,
    "lastLoginTime": "2025-12-08T10:30:00",
    "lastLoginIp": "127.0.0.1"
  }
}
```

### 2.2 修改自己的信息

```http
POST /api/user/info
Content-Type: application/json
```

**请求体**：

```json
{
  "name": "张三",
  "email": "zhangsan_new@example.com",
  "phone": "13800138000",
  "major": "机械工程",
  "grade": "2021",
  "skills": "建模,切片,后处理"
}
```

### 2.3 修改密码

```http
PUT /api/user/password
```

同 `1.4` `/auth/password`。

### 2.4 成员列表（分页）

```http
GET /api/user/list?page=1&size=10&role=1&keyword=张
```

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | int | ❌ | 默认 1 |
| `size` | int | ❌ | 默认 20 |
| `role` | int | ❌ | 按角色筛选（1=社员，2=技术骨干，3=负责人，9=管理员） |
| `keyword` | string | ❌ | 按学号/姓名模糊搜索 |

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "studentId": "2021001",
        "name": "张三",
        "role": 1,
        "major": "机械工程",
        "grade": "2021"
      }
    ],
    "total": 50,
    "pages": 5,
    "current": 1,
    "size": 10
  }
}
```

### 2.5 修改成员角色（仅 ADMIN）

```http
PUT /api/user/role
```

**请求体**：

```json
{
  "studentId": "2021002",
  "role": 2
}
```

### 2.6 修改技能标签

```http
PUT /api/user/skill
```

**请求体**：`{"skills": "建模,切片,后处理,三维设计"}`

### 2.7 统计信息

```http
GET /api/user/stats?studentId={自己/他人}
```

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "studentId": "2021001",
    "submittedTasks": 12,
    "approvedTasks": 10,
    "completedTasks": 8,
    "cancelledTasks": 2,
    "totalPrintHours": 32.5,
    "totalWeightUsed": 450.0,
    "involvedProjects": 3
  }
}
```

---

## 3. 打印任务模块（`/api/task/*`）

### 3.1 提交任务

```http
POST /api/task
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：

```json
{
  "title": "机器人底座",
  "description": "实验室机器人底座，需要 M5 螺丝孔位",
  "stlFileId": 12,
  "materialId": 3,
  "color": "黑色",
  "weightGrams": 150.50,
  "estimatedHours": 6.5,
  "priority": 2,
  "projectId": null
}
```

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": "T20251208A1B2",
    "title": "机器人底座",
    "status": 1,
    "statusName": "待审批",
    "applyTime": "2025-12-08T10:30:00"
  }
}
```

**curl**：

```bash
curl -X POST http://localhost:8080/api/task \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d '{"title":"机器人底座","materialId":3,"priority":2,"weightGrams":150.5}'
```

### 3.2 任务详情

```http
GET /api/task/{taskId}
```

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": "T20251208A1B2",
    "title": "机器人底座",
    "description": "...",
    "applicantId": "2021001",
    "applicantName": "张三",
    "status": 1,
    "statusName": "待审批",
    "priority": 2,
    "priorityName": "普通",
    "materialId": 3,
    "materialType": "PLA",
    "color": "黑色",
    "weightGrams": 150.50,
    "estimatedHours": 6.5,
    "stlFileUrl": "/uploads/2025/12/08/xxx.stl",
    "projectId": null,
    "printerId": null,
    "applyTime": "2025-12-08T10:30:00",
    "approverId": null,
    "approveTime": null,
    "approveRemark": null,
    "rejectReason": null,
    "startTime": null,
    "finishTime": null,
    "actualHours": null,
    "pickupTime": null,
    "timeline": [
      {
        "action": "apply",
        "actionName": "提交申请",
        "operatorName": "张三",
        "fromStatus": null,
        "toStatus": 1,
        "operateTime": "2025-12-08T10:30:00"
      }
    ]
  }
}
```

### 3.3 我的任务

```http
GET /api/task/my?page=1&size=10&status=1
```

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | int | ❌ | 默认 1 |
| `size` | int | ❌ | 默认 20 |
| `status` | int | ❌ | 按状态筛选（可多选：`?status=1,2,4`） |

### 3.4 打印队列

```http
GET /api/task/queue?page=1&size=50
```

返回状态为 `已通过(2)`、`排队中(4)`、`打印中(5)` 的任务，按 `priority ASC, apply_time ASC` 排序。

### 3.5 待审批任务（STAFF+）

```http
GET /api/task/pending?page=1&size=20
```

返回状态为 `待审批(1)` 的任务。

### 3.6 任务统计

```http
GET /api/task/stats?studentId={可选}
```

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "pending": 5,
    "approved": 10,
    "queued": 8,
    "printing": 3,
    "done": 70,
    "cancelled": 4,
    "totalHours": 250.5,
    "totalWeight": 5000.0
  }
}
```

### 3.7 审批通过

```http
POST /api/task/{taskId}/approve
```

**请求体**（可选）：

```json
{
  "remark": "材料够用，可以排"
}
```

### 3.8 驳回

```http
POST /api/task/{taskId}/reject
```

**请求体**：

```json
{
  "reason": "文件格式不对，请转 STL"
}
```

### 3.9 取消任务（申请人）

```http
POST /api/task/{taskId}/cancel
```

**请求体**（可选）：`{"reason": "不需要了"}`

### 3.10 取件签到

```http
POST /api/task/{taskId}/pickup
```

**请求体**（可选）：

```json
{
  "signature": "data:image/png;base64,iVBORw0KGgo..."
}
```

### 3.11 分配打印机（STAFF+）

```http
POST /api/task/{taskId}/assign
```

**请求体**：

```json
{
  "printerId": 1
}
```

### 3.12 开始打印（STAFF+）

```http
POST /api/task/{taskId}/start
```

### 3.13 标记完成（STAFF+）

```http
POST /api/task/{taskId}/finish
```

**请求体**：

```json
{
  "actualHours": 5.8,
  "actualWeightGrams": 145.0,
  "remark": "打印顺利"
}
```

---

## 4. 项目模块（`/api/project/*`）

### 4.1 创建项目

```http
POST /api/project
Authorization: Bearer {token}
```

**请求体**：

```json
{
  "projectName": "校园导航机器人",
  "projectCode": "PRJ-2025-001",
  "projectType": 1,
  "description": "参加 RoboMaster 比赛的项目",
  "startDate": "2025-12-01",
  "endDate": "2026-03-01",
  "budget": 5000.00,
  "tags": "RoboMaster,机器人,竞赛",
  "members": [
    {"memberId": "2021001", "roleInProject": 1, "contribution": "项目负责人"},
    {"memberId": "2021002", "roleInProject": 2, "contribution": "建模"},
    {"memberId": "2021003", "roleInProject": 3, "contribution": "测试"}
  ],
  "stages": [
    {"stageName": "建模", "stageOrder": 1, "plannedStartDate": "2025-12-01", "plannedEndDate": "2025-12-15"},
    {"stageName": "打印", "stageOrder": 2, "plannedStartDate": "2025-12-16", "plannedEndDate": "2026-01-15"},
    {"stageName": "装配", "stageOrder": 3, "plannedStartDate": "2026-01-16", "plannedEndDate": "2026-02-15"}
  ]
}
```

**响应**：`data: { "projectId": 1, "projectCode": "PRJ-2025-001" }`

### 4.2 项目详情

```http
GET /api/project/{id}
```

**响应**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "project": {
      "id": 1,
      "projectName": "校园导航机器人",
      "projectCode": "PRJ-2025-001",
      "projectType": 1,
      "projectTypeName": "竞赛",
      "status": 1,
      "statusName": "进行中",
      "leaderId": "2021001",
      "leaderName": "张三",
      "startDate": "2025-12-01",
      "endDate": "2026-03-01",
      "actualEndDate": null,
      "budget": 5000.00,
      "tags": "RoboMaster,机器人,竞赛"
    },
    "members": [
      {
        "pmId": 1,
        "memberId": "2021001",
        "memberName": "张三",
        "roleInProject": 1,
        "roleInProjectName": "项目负责人",
        "contribution": "项目负责人",
        "joinTime": "2025-12-01T09:00:00"
      }
    ],
    "stages": [
      {
        "id": 1,
        "stageName": "建模",
        "stageOrder": 1,
        "status": 2,
        "statusName": "已完成",
        "plannedStartDate": "2025-12-01",
        "plannedEndDate": "2025-12-15",
        "actualStartDate": "2025-12-01",
        "actualEndDate": "2025-12-14",
        "progressPercent": 100
      }
    ],
    "files": [
      {
        "id": 1,
        "fileName": "机器人.stl",
        "fileSize": 2048000,
        "fileType": "stl",
        "uploaderName": "李四",
        "uploadTime": "2025-12-02T10:00:00"
      }
    ],
    "relatedTasks": [
      {
        "taskId": "T20251208A1B2",
        "title": "机器人底座",
        "status": 6,
        "statusName": "已完成",
        "applyTime": "2025-12-08T10:30:00"
      }
    ]
  }
}
```

### 4.3 编辑项目

```http
PUT /api/project/{id}
```

**请求体**：同创建（可选字段）

### 4.4 删除项目

```http
DELETE /api/project/{id}
```

**说明**：软删除，关联数据保留。

### 4.5 标记完成

```http
POST /api/project/{id}/complete
```

### 4.6 取消项目

```http
POST /api/project/{id}/cancel
```

**请求体**：`{"reason": "经费不足"}`

### 4.7 项目列表

```http
GET /api/project/list?page=1&size=10&status=1&scope=mine&keyword=机器
```

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `page` | int | ❌ | 默认 1 |
| `size` | int | ❌ | 默认 20 |
| `status` | int | ❌ | 状态筛选 |
| `projectType` | int | ❌ | 类型筛选 |
| `scope` | string | ❌ | `mine`（我参与的）/ `all`（全部） |
| `keyword` | string | ❌ | 按名称搜索 |

### 4.8 添加成员

```http
POST /api/project/{id}/member
```

**请求体**：

```json
{
  "memberId": "2021004",
  "roleInProject": 3,
  "contribution": "建模"
}
```

### 4.9 移除成员

```http
DELETE /api/project/{id}/member/{memberId}
```

### 4.10 添加阶段

```http
POST /api/project/{id}/stage
```

**请求体**：

```json
{
  "stageName": "测试",
  "stageOrder": 4,
  "plannedStartDate": "2026-02-16",
  "plannedEndDate": "2026-03-01"
}
```

### 4.11 编辑阶段

```http
PUT /api/project/{id}/stage/{stageId}
```

### 4.12 删除阶段

```http
DELETE /api/project/{id}/stage/{stageId}
```

### 4.13 上传文件

```http
POST /api/project/{id}/file/upload
Content-Type: multipart/form-data
```

**表单字段**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `file` | File | ✅ | 文件（最大 50MB） |
| `description` | string | ❌ | 文件描述 |

### 4.14 删除文件

```http
DELETE /api/project/{id}/file/{fileId}
```

---

## 5. 打印机模块（`/api/printer/*`）

### 5.1 打印机列表

```http
GET /api/printer/list
```

**响应**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "printerCode": "PR-001",
      "printerName": "创想三维 K1",
      "model": "K1",
      "brand": "创想三维",
      "buildVolume": "220x220x250mm",
      "supportedMaterials": "PLA,ABS,PETG,TPU",
      "status": 1,
      "statusName": "空闲",
      "location": "打印室A-3号机位",
      "totalPrintHours": 120.5,
      "totalPrintCount": 50,
      "lastUsedTime": "2025-12-08T08:00:00"
    }
  ]
}
```

### 5.2 打印机详情

```http
GET /api/printer/{id}
```

### 5.3 可用打印机

```http
GET /api/printer/{id}/available?materialType=PLA
```

**查询参数**：`materialType`（按材料类型筛选）

返回状态 = 空闲(1) 且支持该材料类型的打印机。

### 5.4 新增打印机（ADMIN）

```http
POST /api/printer
```

**请求体**：

```json
{
  "printerCode": "PR-005",
  "printerName": "Bambu Lab X1",
  "model": "X1 Carbon",
  "brand": "拓竹",
  "buildVolume": "256x256x256mm",
  "supportedMaterials": "PLA,ABS,PETG,PA,PC",
  "location": "打印室B-1号机位"
}
```

### 5.5 修改打印机（ADMIN）

```http
PUT /api/printer/{id}
```

### 5.6 设置维护状态（ADMIN）

```http
POST /api/printer/{id}/maintain
```

**请求体**：`{"status": 3, "remark": "更换喷嘴"}`

---

## 6. 材料模块（`/api/material/*`）

### 6.1 材料列表

```http
GET /api/material/list?materialType=PLA
```

### 6.2 材料详情

```http
GET /api/material/{id}
```

**响应**：

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "materialCode": "PLA-RED-1KG",
    "materialName": "PLA 红色 1.75mm",
    "materialType": "PLA",
    "color": "红色",
    "spec": "1.75mm",
    "unit": "g",
    "totalStock": 5000.0,
    "warningThreshold": 500.0,
    "unitPrice": 80.00,
    "supplier": "eSUN",
    "status": 1
  }
}
```

### 6.3 库存预警

```http
GET /api/material/warning
```

返回 `total_stock < warning_threshold` 的材料。

### 6.4 新增材料（ADMIN）

```http
POST /api/material
```

### 6.5 修改材料（ADMIN）

```http
PUT /api/material/{id}
```

### 6.6 补充库存（ADMIN）

```http
POST /api/material/{id}/restock
```

**请求体**：

```json
{
  "amount": 1000.0,
  "remark": "新购入库"
}
```

### 6.7 消耗日志

```http
GET /api/material/log?page=1&size=20&materialId=1
```

---

## 7. 文件模块（`/api/file/*`）

### 7.1 上传文件（通用）

```http
POST /api/file/upload
Content-Type: multipart/form-data
```

**表单字段**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `file` | File | ✅ | 文件 |
| `bizType` | string | ✅ | 业务类型（task/project/avatar） |
| `bizId` | long | ❌ | 业务 ID |

**支持格式**：`stl`, `3mf`, `obj`, `gcode`, `png`, `jpg`, `pdf`, `doc`, `docx`, `xls`, `xlsx`, `zip` 等。

**限制**：最大 50MB。

### 7.2 下载文件

```http
GET /api/file/download/{fileId}
```

**说明**：直接返回文件流，前端用 `<a :href="url" download>` 或 `window.open(url)` 下载。

**响应头**：

```
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="机器人.stl"
```

### 7.3 删除文件

```http
DELETE /api/file/{fileId}
```

---

## 8. 日志模块（`/api/log/*`，仅 ADMIN）

### 8.1 登录日志

```http
GET /api/log/login?page=1&size=20&studentId=&status=&startTime=&endTime=
```

### 8.2 操作日志

```http
GET /api/log/operation?page=1&size=20&memberId=&module=&action=&startTime=&endTime=
```

---

## 9. 测试用例（端到端）

### 9.1 完整流程：提交 → 审批 → 打印 → 完成

```bash
# 1. 登录拿 token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"studentId":"2021001","password":"123456"}' \
  | jq -r '.data.token')

# 2. 社员提交任务
TASK_ID=$(curl -s -X POST http://localhost:8080/api/task \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"测试件","materialId":1,"weightGrams":100,"estimatedHours":3}' \
  | jq -r '.data.taskId')

# 3. 切换到技术骨干
STAFF_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"studentId":"2021002","password":"123456"}' \
  | jq -r '.data.token')

# 4. 审批通过
curl -X POST "http://localhost:8080/api/task/$TASK_ID/approve" \
  -H "Authorization: Bearer $STAFF_TOKEN"

# 5. 分配打印机
curl -X POST "http://localhost:8080/api/task/$TASK_ID/assign" \
  -H "Authorization: Bearer $STAFF_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"printerId":1}'

# 6. 开始打印
curl -X POST "http://localhost:8080/api/task/$TASK_ID/start" \
  -H "Authorization: Bearer $STAFF_TOKEN"

# 7. 标记完成
curl -X POST "http://localhost:8080/api/task/$TASK_ID/finish" \
  -H "Authorization: Bearer $STAFF_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"actualHours":2.8,"actualWeightGrams":95}'

# 8. 社员取件
curl -X POST "http://localhost:8080/api/task/$TASK_ID/pickup" \
  -H "Authorization: Bearer $TOKEN"

# 9. 查最终状态
curl "http://localhost:8080/api/task/$TASK_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '.data.statusName'
# 输出："已完成"
```

---

## 10. 错误码速查

| HTTP | 业务码 | 含义 | 排查 |
|---|---|---|---|
| 200 | 200 | 成功 | - |
| 400 | 40001 | 参数校验失败 | 看 message |
| 400 | 40002 | 业务校验失败 | 看 message |
| 401 | 40101 | Token 缺失 | 加 Authorization 头 |
| 401 | 40102 | Token 无效 | 重新登录 |
| 401 | 40103 | Token 过期 | 刷新 token 或重新登录 |
| 403 | 40301 | 角色不足 | 看 PERMISSION_MATRIX.md |
| 403 | 40302 | 数据归属不符 | 不能操作别人的数据 |
| 403 | 40303 | 状态不符 | 当前状态不允许此操作 |
| 404 | 40401 | 资源不存在 | 检查 ID |
| 409 | 40901 | 数据冲突 | 重复提交/唯一索引冲突 |
| 500 | 50001 | 系统异常 | 看后端日志 |

---

## 11. 调试技巧

### 11.1 Swagger / Knife4j

打开 `http://localhost:8080/api/doc.html`，所有接口可视化测试。

### 11.2 查看请求日志

```bash
# 后端启动加参数
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 看到 SQL 日志
# Hibernate: select ... from print_task where ...
```

### 11.3 抓包

F12 → Network → 看请求/响应体。

### 11.4 PowerShell 测试

用 `scripts/test-all-endpoints.ps1`，一键测 11 个核心接口。

---

## 12. 接口变更日志

| 日期 | 版本 | 变更 | 作者 |
|---|---|---|---|
| 2025-12-08 | v1.0 | 初版，26 个接口 | 组长 |
| | | | |

---

**附录**：

- 字段含义看 [DATA_DICTIONARY.md](./DATA_DICTIONARY.md)
- 权限矩阵看 [PERMISSION_MATRIX.md](./PERMISSION_MATRIX.md)
- 故障排查看 [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
