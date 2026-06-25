# material

耗材模块（**F**）

接口：`/api/material/*`

## 业务要点

- 余额连续：每次操作 `balance` 都写真实余额（不要盘点！）
- 消耗：负数 `weight_change`，必填 `related_task_id`
- 库存预警：`balance < 200g` 触发
