# 贡献指南（CONTRIBUTING.md）

> 写给队友看的开发手册：怎么拉代码、怎么改代码、怎么合代码。
> 看这一篇就够了，够你从 0 到能 push。

---

## 0. TL;DR（30 秒看完）

```bash
# 1. 克隆（首次）
git clone https://github.com/summer/3D-Printing-Management-System.git
cd 3D-Printing-Management-System

# 2. 切自己的分支（不要在 main/develop 上直接改！）
git checkout -b feature/<你的模块>-<你名字缩写>

# 3. 改代码…（按下面的规范）

# 4. 提交前先跑检查（防密码泄露、防 TS 编译失败）
bash scripts/pre-commit-check.sh        # Linux/Mac
.\scripts\pre-commit-check.ps1          # Windows PowerShell

# 5. 提交（commit message 严格按规范）
git add .
git commit -m "feat(task): 新增打印任务提交接口"

# 6. 推送 + 开 PR
git push origin feature/task-lsy
# 然后去 GitHub 网页点 "Compare & pull request"
```

---

## 1. 分支策略

我们用 **Git Flow 简化版**，只留 4 类分支：

| 分支 | 谁在改 | 作用 | 谁能 push | 谁能 merge |
|---|---|---|---|---|
| `main` | ❌ 谁都不直接改 | 生产代码，每晚自动打包 | 仅组长（leader） | 仅组长 |
| `develop` | 集成时用 | 集成所有功能分支的临时归宿 | 仅组长 | 仅组长 |
| `feature/<模块>-<名字>` | 个人开发 | 你的功能/页面/接口 | 任何人 | PR 通过后由组长 merge |
| `hotfix/<bug>` | 紧急修复 | 线上 bug 修复 | 任何人 | PR 通过后由组长 merge |

### 1.1 命名规范

**feature 分支**（你开发一个东西就开一个分支）：

```bash
feature/<模块代号>-<一句话功能>-<你的名字缩写>

# 模块代号（按 docs/team_outline_v1.md）：
#   auth       - 登录/认证
#   user       - 用户/成员
#   task       - 打印任务
#   project    - 项目
#   file       - 文件上传下载
#   material   - 材料库存
#   printer    - 打印机管理
#   dashboard  - 数据大屏
#   layout     - 公共布局
#   common     - 公共组件
#   doc        - 文档

# 例子：
feature/task-apply-form-lsy          # 李四写任务申请表单
feature/project-stage-editor-zsm     # 张三写项目阶段编辑器
feature/material-stock-page-wh      # 王五写材料库存页
```

**hotfix 分支**（修线上 bug）：

```bash
hotfix/<一句话描述>

# 例子：
hotfix/login-jwt-expired
hotfix/task-duplicate-submit
```

### 1.2 分支生命周期

```
本地创建 feature 分支 → 写代码 → 提交 → push → 开 PR → 组长 review → 合并到 develop → 删除 feature 分支
                                                                              ↓
                                                                       （集成测试通过后）
                                                                              ↓
                                                                       组长 merge 到 main
```

---

## 2. Commit Message 规范

**强制使用 Conventional Commits**，否则 CI 会拒绝合并。

### 2.1 格式

```
<类型>(<作用域>): <一句话描述，不超过 50 字>

<正文，72 字换行，解释"为什么"而不是"做了什么">

<脚注，关联 Issue / 任务编号>
```

### 2.2 类型（type）

| 类型 | 说明 | 例子 |
|---|---|---|
| `feat` | 新功能 | `feat(task): 新增打印任务提交接口` |
| `fix` | 修 bug | `fix(login): 修复 JWT 过期后无法刷新问题` |
| `refactor` | 重构（不改功能） | `refactor(task): 把 TaskService 拆成审批 + 执行两个服务` |
| `style` | 纯格式（空格/分号/缩进） | `style(project): 统一用 2 空格缩进` |
| `docs` | 文档 | `docs(readme): 补充启动步骤` |
| `test` | 测试 | `test(task): 给 TaskService 加单元测试` |
| `chore` | 杂事（依赖/脚本） | `chore(deps): 升级 lombok 到 1.18.34` |
| `perf` | 性能优化 | `perf(queue): 用 Redis 缓存打印队列` |

### 2.3 作用域（scope）

用模块代号（同分支命名）：

- `auth` / `user` / `task` / `project` / `file` / `material` / `printer` / `dashboard`
- `layout` / `common` / `doc`

### 2.4 完整例子

```bash
# 好例子（推荐这么写）
git commit -m "feat(task): 新增打印任务提交接口

- 新增 PrintTask entity + TaskMapper
- 新增 TaskService.submitTask()，事务内扣库存+累加次数
- 新增 TaskController.submit POST /api/task
- 13 个单元测试覆盖正常/异常路径"

# 坏例子（不要这么写）
git commit -m "update"
git commit -m "fix bug"
git commit -m "新增任务"
```

---

## 3. 代码规范

### 3.1 后端（Java + Spring Boot 3）

#### 必须遵守的硬规则

1. **包名**：`com.printclub.module.<模块>.<controller|service|mapper|entity|dto>`
2. **类名**：`XxxController` / `XxxService` + `Impl` / `XxxMapper` / `XxxEntity` / `XxxDTO`
3. **Lombok**：`@Data` / `@Builder` / `@Slf4j` 都可以用
4. **⚠️ 禁用变量名 `log`**：在 `@Slf4j` 类里 `log` 已被 Lombok 占用，自己再定义会编译失败！
   ```java
   @Slf4j
   @Service
   public class MaterialLogService {       // ✅ 类名可以用
       private final MaterialLog logEntry;  // ✅ 字段名不能用 log，用 logEntry
       public void addLog() {
           log.info("xxx");                // ✅ 用 Lombok 的 log
       }
   }
   ```
5. **事务**：`@Transactional(rollbackFor = Exception.class)` 显式指定
6. **DTO 不要泄露 entity**：返回给前端用 VO/DTO，不要直接返 entity
7. **密码、token、密钥**：必须从环境变量读，禁止硬编码
   ```java
   // ❌ 错
   @Value("${jwt.secret}")
   private String secret;  // 别人能 git log 看到

   // ✅ 对
   @Value("${jwt.secret:${JWT_SECRET:default-dev-secret}}")
   private String secret;  // 占位符 + 环境变量覆盖
   ```
8. **接口路径**：`/api/<模块>/<动作>`，例如 `/api/task/submit`
9. **统一返回格式**：用 `Result<T>` 包装，`code`/`message`/`data` 三段
10. **日志**：关键操作必打日志（INFO 级别 + MDC traceId）
    ```java
    log.info("[{}] 提交任务 title={} applicantId={}", traceId, title, applicantId);
    ```

#### 数据库操作规范

1. **MyBatis-Plus**：用 `BaseMapper` + `LambdaQueryWrapper`
2. **分页**：返回 `PageResult<T>`（不是 `Page<T>`），避免前端 `records` vs `list` 字段不一致
3. **删除**：业务表全部软删除（`@TableLogic` + `deleted` 字段）
4. **索引**：所有外键 + 状态字段必须有索引
5. **金额/重量**：用 `BigDecimal`，禁止 `double`

### 3.2 前端（Vue 3 + TS + Vite）

#### 必须遵守的硬规则

1. **Composition API** 优先（不用 Options API）
2. **`<script setup lang="ts">`**：所有组件必须 TS
3. **命名**：
   - 文件：`kebab-case.vue` 或 `kebab-case/index.vue`
   - 组件名：`PascalCase`（自动从文件名推）
   - 变量/函数：`camelCase`
   - 类型/接口：`PascalCase`，不加 `I` 前缀
4. **store**：用 Pinia，**禁止直接修改 state**（必须通过 action）
5. **API 请求**：用 `src/utils/request.ts` 统一封装，**禁止直接 axios**
6. **弹窗**：所有 `ElMessageBox.confirm` 加 `center: true` + try/catch 处理 cancel
7. **空状态**：用 `EmptyState` 组件，**禁止用 `<el-empty>`**
8. **图标**：从 `@element-plus/icons-vue` 导入，不要字符串
9. **样式**：SCSS + `<style scoped>`，全局样式放 `src/styles/`
10. **按钮防重复点击**：所有"提交/删除/审批"类按钮加 `v-debounce-click`
    ```vue
    <el-button type="primary" v-debounce-click @click="onSubmit">提交</el-button>
    ```

#### 目录结构约定

```
src/
├── api/              # 接口调用（按模块分文件）
├── assets/           # 图片/字体
├── components/       # 组件
│   ├── common/       # 跨模块通用（PageHeader/EmptyState/StatusTag）
│   ├── layout/       # 布局（AppLayout/AppHeader/AppSidebar）
│   └── <模块>/       # 模块专属组件
├── directives/       # 全局指令（debounce-click 等）
├── router/           # 路由
├── stores/           # Pinia store
├── styles/           # 全局样式
│   ├── variables.scss   # 颜色/间距变量
│   ├── common.scss      # 通用类
│   └── element-plus.scss # Element Plus 主题覆盖
├── types/            # TS 类型定义（按模块分文件）
├── utils/            # 工具函数
│   ├── request.ts    # axios 封装
│   ├── format.ts     # 日期/数字格式化
│   └── auth.ts       # token 处理
└── views/            # 页面
    ├── <模块>/
    │   ├── <子页面>/index.vue
    │   └── ...
```

### 3.3 通用规则

1. **不要 hardcode 任何账号、密码、token、密钥**
2. **不要 commit 任何 `node_modules/`、`target/`、`dist/`、`*.log`、`*.iml`、`.idea/`**（已在 `.gitignore` 里）
3. **不要把 IDEA 的 `.idea/` 和 VSCode 的 `.vscode/` 提交**（已在 `.gitignore` 里）
4. **不要修改 `package-lock.json` 和 `pom.xml` 的 lock 部分**（除非你清楚自己在干嘛）
5. **遇到老代码看不懂** → 先在群里问，**别瞎改**

---

## 4. 提交流程（端到端）

### 4.1 你要提交一个新功能

```bash
# 1. 确保本地 main 是最新的
git checkout main
git pull origin main

# 2. 基于 main 切新分支
git checkout -b feature/task-batch-apply-lsy

# 3. 写代码…

# 4. 跑本地检查
.\scripts\pre-commit-check.ps1         # Windows
bash scripts/pre-commit-check.sh       # Mac/Linux

# 5. 提交（按规范）
git add .
git commit -m "feat(task): 新增批量提交任务接口"

# 6. 推送
git push origin feature/task-batch-apply-lsy

# 7. 去 GitHub 开 PR
# - Base: main ← Compare: feature/task-batch-apply-lsy
# - 标题：和 commit 一样
# - 描述：用下面的 PR 模板
# - Reviewer: @组长
```

### 4.2 PR 模板（GitHub 提交时自动弹）

```markdown
## 改了什么
<!-- 一句话说清楚这次改了什么 -->

## 关联 Issue / 任务
<!-- 关联到哪个 Issue，比如 Closes #123 -->

## 测试
<!-- 你做了什么测试？怎么验证？ -->

## 截图（如果是 UI）
<!-- 截图贴这里 -->

## Checklist
- [ ] 我在自己的机器上跑通了
- [ ] 我加了/改了对应的文档
- [ ] 我没 commit 任何密码/密钥
- [ ] 我跑过了 `pre-commit-check`
```

### 4.3 Code Review 要点

组长会按下面顺序检查，被打回的话就改：

1. **能不能跑起来** → `npm run build` + `mvn spring-boot:run` 不报错
2. **符不符合规范** → commit message / 分支名 / 命名约定
3. **有没有写测试** → 至少核心 service 有单元测试
4. **有没有泄露密码** → grep `password|secret|token` 看一眼
5. **有没有改其他模块** → 不要在 task PR 里改 project 代码

---

## 5. 紧急情况

### 5.1 线上有 bug 要立刻修

```bash
# 1. 从 main 拉 hotfix 分支
git checkout main
git pull origin main
git checkout -b hotfix/login-jwt-expired

# 2. 改完直接 push 开 PR（不用先到 develop）
# 3. 组长审批后直接 merge 到 main
# 4. 别忘了 cherry-pick 回 develop
```

### 5.2 我 commit 错了

```bash
# 改最后一次 commit 的 message
git commit --amend -m "正确的 message"

# 撤回最后一次 commit（保留改动）
git reset --soft HEAD~1

# 撤回最后一次 commit（不保留改动）
git reset --hard HEAD~1

# 我已经 push 了（需要强制，会被 GitHub 拒绝时用 -f）
git push -f origin feature/xxx
```

> ⚠️ **不要 force push 到 main / develop**！会炸的。

### 5.3 我和别人冲突了

```bash
# 1. 先 rebase 一下 main
git fetch origin
git rebase origin/main

# 2. 有冲突 → 解决冲突（VSCode 里点 Accept Current / Accept Incoming / Accept Both）
# 3. add + rebase --continue
git add .
git rebase --continue

# 4. push
git push -f origin feature/xxx
```

---

## 6. 新人入职 Checklist

- [ ] 加入 GitHub 组织
- [ ] clone 仓库到本地
- [ ] 跑通 `npm run dev` + `mvn spring-boot:run`
- [ ] 通读 `README.md` + `docs/team_outline_v1.md`
- [ ] 跑通 `scripts/test-all-endpoints.ps1`，看到 11 个测试全过
- [ ] 在群里领一个 feature issue
- [ ] 按上面的流程开第一个 PR

---

## 7. 常见问题（FAQ）

**Q：我能在 main 分支直接 push 吗？**
A：不能。必须开 PR。除非你是组长修 hotfix。

**Q：我能不能一次性提交 500 行代码？**
A：不建议。拆成多个 commit，每个 commit 只做一件事。

**Q：我写的代码要不要写单元测试？**
A：要。至少 service 层必须有测试。组长会在 review 时打回没测试的 PR。

**Q：我能不能用 ChatGPT 生成的代码？**
A：可以，但你自己必须读懂每一行，不然 review 时会被打回。

**Q：commit message 写中文还是英文？**
A：都行，但我们推荐中文（团队都是中国人）。组长会统一风格。

**Q：我删了一个不该删的文件怎么办？**
A：`git checkout HEAD -- <文件名>` 救回来。如果已经 commit 了，`git revert <commit-hash>`。

---

## 8. 参考链接

- [Conventional Commits](https://www.conventionalcommits.org/)
- [Vue 3 风格指南](https://v3.cn.vuejs.org/style-guide/)
- [Spring Boot 代码规范](https://github.com/alibaba/p3c)
- [项目文档目录](./README.md)
