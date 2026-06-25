# task

打印任务模块（**E** 核心 — 你）

接口：`/api/task/*`

## 文件清单

- `controller/TaskController.java`
- `service/TaskService.java` + `TaskServiceImpl.java`
- `mapper/TaskMapper.java` + 自定义 SQL（队列排序等）
- `entity/PrintTask.java` — 对应 `print_task` 表
- `dto/TaskApplyDTO.java` — 提交申请
- `dto/ApproveDTO.java` — 审批
- `dto/RejectDTO.java` — 驳回
- `dto/AssignPrinterDTO.java` — 分配打印机
- `dto/FinishPrintDTO.java` — 完成打印（含实际耗材/耗时/质量分）
- `dto/PickupDTO.java` — 取件签到
- `dto/TaskQuery.java` — 列表查询参数

## 业务要点

- 状态流转合法性校验（用枚举类 + switch 校验）
- 任务完成时**事务内**扣减耗材（material_log）+ 累计 member.total_prints + 归档作品库
- 队列排序：priority 升序 → apply_time 升序
- 当前任务查询：实时查 `WHERE printer_id=? AND status=4`（不要存 current_task_id）
