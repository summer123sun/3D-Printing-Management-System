# stats

统计看板模块（**F**）

接口：`/api/stats/dashboard`

## 业务要点

- 一次性返回所有看板数据（4 张图 + 4 张数据卡 + 库存预警列表）
- 用聚合 SQL（GROUP BY / COUNT / SUM）一次查完
