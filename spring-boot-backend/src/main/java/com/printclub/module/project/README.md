# project

项目模块（**E** 核心 — 你）

接口：`/api/project/*`

## 文件清单

- `controller/ProjectController.java`
- `service/ProjectService.java`
- `mapper/ProjectMapper.java`
- `entity/Project.java` + `ProjectMember.java` + `ProjectProgress.java` + `ProjectFile.java`
- `dto/ProjectCreateDTO.java` `StageDTO.java` `AddMemberDTO.java` 等

## 业务要点

- 阶段管理：动态增删改排序（前端传数组）
- 关联任务：项目详情页要能展示所有 `print_task WHERE project_id=?`
- 权限：仅项目负责人可改项目信息/成员/阶段
