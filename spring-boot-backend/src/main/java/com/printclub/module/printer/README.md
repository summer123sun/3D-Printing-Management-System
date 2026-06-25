# printer

打印机模块（**F**）

接口：`/api/printer/*`

## 业务要点

- 实时查当前任务：`SELECT * FROM print_task WHERE printer_id=? AND status=4 LIMIT 1`
- 累计时长：任务完成时累加（actual_time / 60 小时）
- 维护记录 4 种类型：保养/维修/换件/校准
