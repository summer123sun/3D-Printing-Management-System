# artwork

作品库模块（**F**）

接口：`/api/artwork/*`

## 业务要点

- 任务完成时自动归档（E 的 taskService 调用）
- 打印参数**不存** artwork 表（关联查 print_task）
- 无点赞/评论功能（v2 已砍）
