# 故障排查手册（TROUBLESHOOTING.md）

> 把这一年踩过的坑全部写下来，下次遇到直接 Ctrl+F。
> 每个问题都有：**症状 → 原因 → 解决**。

---

## 目录

1. [后端启动问题](#1-后端启动问题)
2. [依赖与构建问题](#2-依赖与构建问题)
3. [数据库问题](#3-数据库问题)
4. [前端开发问题](#4-前端开发问题)
5. [前端构建问题](#5-前端构建问题)
6. [JWT 与登录问题](#6-jwt-与登录问题)
7. [中文乱码问题](#7-中文乱码问题)
8. [Git 问题](#8-git-问题)
9. [PowerShell 测试脚本问题](#9-powershell-测试脚本问题)
10. [IDEA 启动坑](#10-idea-启动坑)
11. [数据库连接密码与登录密码搞混](#11-数据库连接密码与登录密码搞混)
12. [代码被覆盖 / 误删](#12-代码被覆盖--误删)
13. [生产环境部署问题](#13-生产环境部署问题)
14. [v2 新增踩坑（审计 / @Async / el-message-box / 编码）](#14-v2-新增踩坑2026-06-期间)
15. [v2.1 部署/上线踩坑（CORS / PowerShell / Cloudflare / 内存压榨）](#15-v21-部署上线踩坑2026-06-29)

---

## 1. 后端启动问题

### 1.1 `mvn` 不是内部或外部命令

**症状**：
```
'mvn' 不是内部或外部命令，也不是可运行的程序或批处理文件。
```

**原因**：IDEA 自带的 Maven 没加到 PATH 里。

**解决**：

```powershell
# Windows PowerShell（当前会话）
$env:PATH += ";D:\Users\summer\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin"

# 或者用我们的一键脚本
.\scripts\start-backend.ps1
```

永久方案：把上面那行加到 `$PROFILE`：

```powershell
# 编辑 profile
notepad $PROFILE
# 加上：
$env:PATH += ";D:\Users\summer\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin"
```

---

### 1.2 Lombok 编译失败：`variable log not initialized`

**症状**：
```
error: variable log might not have been initialized
```

**原因**：你在 `@Slf4j` 类里**自己定义了一个叫 `log` 的字段**，和 Lombok 生成的 `log` 冲突。

**解决**：

```java
@Slf4j
@Service
public class MaterialLogService {
    // ❌ 错
    private final MaterialLog log;

    // ✅ 对
    private final MaterialMaterialLog logEntry;
    // 或
    private final MaterialLog materialLog;
}
```

---

### 1.3 Lombok 版本不存在：`1.18.36` 找不到

**症状**：
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.13.0:compile
  ... cannot access lombok. lombok 1.18.36 does not exist
```

**原因**：有人写错了版本号。Lombok 最新稳定版是 **1.18.34**（不是 .36）。

**解决**：

```xml
<!-- pom.xml -->
<properties>
    <lombok.version>1.18.34</lombok.version>  <!-- 不要写 1.18.36！ -->
</properties>
```

---

### 1.4 MySQL Driver 重复加载：`class com.mysql.cj.jdbc.Driver loaded twice`

**症状**：
```
Caused by: java.lang.ClassCastException: class com.mysql.cj.jdbc.Driver cannot be cast to class java.sql.Driver
```

**原因**：本地 Maven 仓库有 **两个版本的 `mysql-connector-j`**（8.0.33 和 8.3.0），都进了 classpath。

**解决**：

```xml
<!-- pom.xml 锁版本 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>  <!-- 只用一个版本！ -->
</dependency>
```

清理本地缓存：

```bash
# 删除冲突版本
rm -rf ~/.m2/repository/com/mysql/mysql-connector-j/8.0.33/
# 强制重新解析依赖
mvn dependency:purge-local-repository -DreResolve=false
mvn clean install
```

---

### 1.5 Maven 中央仓库超时：Connection timed out

**症状**：
```
Could not resolve dependencies for project ...: Could not transfer artifact ...
  ...: Connection timed out
```

**原因**：在国内连 Maven Central 超时是常态。

**解决**：配置阿里云镜像（在 `~/.m2/settings.xml`）：

```xml
<!-- ~/.m2/settings.xml -->
<settings>
  <mirrors>
    <mirror>
      <id>aliyun-public</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Public</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
    <mirror>
      <id>aliyun-spring</id>
      <mirrorOf>spring-milestones,spring-snapshots</mirrorOf>
      <name>Aliyun Spring</name>
      <url>https://maven.aliyun.com/repository/spring</url>
    </mirror>
  </mirrors>
</settings>
```

---

### 1.6 启动报错：`Communications link failure`

**症状**：
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago.
```

**原因**：MySQL 没启动，或者 `application-dev.yml` 里的密码不对。

**解决**：

```bash
# 1. 检查 MySQL 是否启动（Windows 服务）
net start mysql80

# 2. 测一下能不能连
mysql -uroot -p
# 输入密码试试（我们的是 Xy@20070831）

# 3. 确认 yml 里的密码
# application-dev.yml
spring:
  datasource:
    password: ${DB_PASSWORD:root}  # ← 这个值从环境变量 DB_PASSWORD 取
```

启动后端时设密码：

```powershell
$env:DB_PASSWORD = "Xy@20070831"
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

### 1.7 启动报错：`Unknown database 'print_club'`

**症状**：
```
java.sql.SQLSyntaxErrorException: Unknown database 'print_club'
```

**原因**：MySQL 里没建这个库。

**解决**：

```bash
# 1. 登录 MySQL
mysql -uroot -p

# 2. 建库
CREATE DATABASE print_club DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 或者直接跑我们的建库脚本
mysql -uroot -p < docs/db_design.sql
```

---

### 1.8 Spring Boot 启动后立刻退出，无错误

**症状**：进程跑 3 秒就退，控制台没 error。

**原因**：某个 `@PostConstruct` 抛异常被吞了。

**解决**：

```yaml
# application.yml 加日志
logging:
  level:
    root: DEBUG
    com.printclub: DEBUG
```

或启动时强制保留堆栈：

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-XX:+ExitOnOutOfMemoryError"
```

---

## 2. 依赖与构建问题

### 2.1 `npm install` 慢

**症状**：等 10 分钟还在转圈。

**解决**：用淘宝镜像

```bash
npm config set registry https://registry.npmmirror.com

# 或者临时用
npm install --registry=https://registry.npmmirror.com
```

---

### 2.2 `npm install` 报 `ERESOLVE` 错误

**症状**：
```
npm ERR! ERESOLVE unable to resolve dependency tree
```

**原因**：依赖版本冲突。

**解决**：

```bash
# 删了重装
rm -rf node_modules package-lock.json
npm install

# 还是不行就用 legacy peer deps
npm install --legacy-peer-deps
```

---

### 2.3 `vue-tsc` 类型检查报错

**症状**：`npm run build` 时一堆 TS error。

**原因**：组件 import 路径错了 / 类型定义漏了。

**解决**：

```bash
# 看具体哪个文件有问题
npm run type-check

# 常见错：
# - Property 'X' does not exist → 类型不匹配，先看 store 的实际类型
# - Cannot find module '@/...' → 检查 vite.config.ts 的 alias 配置
# - Type 'X' is not assignable to type 'Y' → 用类型断言 any 临时绕过，但要在 PR 注释里说明
```

---

## 3. 数据库问题

### 3.1 外键约束失败：`Cannot delete or update a parent row`

**症状**：
```
Cannot delete or update a parent row: a foreign key constraint fails
```

**原因**：子表还有数据，不能直接删父表。

**解决**（按顺序删）：

```sql
-- 删项目时按这个顺序
DELETE FROM project_file WHERE project_id BETWEEN 5 AND 8;
DELETE FROM project_progress WHERE project_id BETWEEN 5 AND 8;
DELETE FROM project_member WHERE project_id BETWEEN 5 AND 8;
DELETE FROM print_task WHERE project_id BETWEEN 5 AND 8;  -- 关联的打印任务
DELETE FROM project WHERE project_id BETWEEN 5 AND 8;
```

**更好的方案**：用事务 + 按依赖顺序删除（在 Service 里写）。

---

### 3.2 字符集问题：`Incorrect string value: '\xE4\xB8\xAD...'`

**症状**：
```
java.sql.SQLException: Incorrect string value: '\xE4\xB8\xAD\xE6\x96\x87' for column 'title'
```

**原因**：表用了 `latin1` 而不是 `utf8mb4`。

**解决**：

```sql
-- 改表的字符集
ALTER TABLE print_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 改数据库默认字符集
ALTER DATABASE print_club DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

### 3.3 MyBatis-Plus 分页返回 `records` 字段，前端期望 `list`

**症状**：前端拿不到数据，因为 `data.list` 是 undefined，实际是 `data.records`。

**原因**：MyBatis-Plus 的 `Page<T>` 序列化时是 `records`，不是 `list`。

**解决**：永远不要把 `Page<T>` 直接返回给前端，用 `PageResult<T>` 转换：

```java
// ✅ 对
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Long pages;
    private Long current;
    private Long size;
    // ...
}

// Service 里转
PageResult<PrintTaskVO> result = PageUtils.fromPage(page, PrintTaskVO.class);
```

---

### 3.4 BigDecimal 除法报错：`Non-terminating decimal expansion`

**症状**：
```
java.lang.ArithmeticException: Non-terminating decimal expansion
```

**原因**：`BigDecimal.divide()` 没指定舍入方式。

**解决**：

```java
// ❌ 错
BigDecimal result = a.divide(b);

// ✅ 对（用 HALF_UP 银行家舍入）
BigDecimal result = a.divide(b, 2, RoundingMode.HALF_UP);
```

---

## 4. 前端开发问题

### 4.1 Pinia store 改了 state，组件不更新

**症状**：`store.xxx = 'new'` 后视图没刷新。

**原因**：Pinia 不允许直接修改 state，必须通过 action。

**解决**：

```ts
// ❌ 错
projectStore.projectList = newData

// ✅ 对
projectStore.setProjectList(newData)  // 在 store 里定义 action
```

---

### 4.2 `ElMessageBox.confirm` 报错：`Uncaught (in promise) cancel`

**症状**：用户点取消时控制台红一片。

**原因**：`ElMessageBox.confirm` 抛 reject 时是字符串 `'cancel'`，没处理就成了 uncaught。

**解决**：

```ts
try {
  await ElMessageBox.confirm('确定删除？', '提示', { center: true })
  // 用户点确认
  await doDelete()
} catch (e) {
  if (e === 'cancel') return  // 用户取消，静默返回
  throw e  // 其他错误继续抛
}
```

---

### 4.3 路由跳转后页面不刷新

**症状**：从 `/task/apply` 跳到 `/task/my`，但是 list 还是旧的。

**原因**：Vue Router 4 默认会复用组件实例。

**解决**：

```ts
// 方法 1：加 key 强制重新创建
<RouterView :key="$route.fullPath" />

// 方法 2：在目标组件里 watch route
watch(
  () => route.params,
  () => fetchData(),
  { immediate: true }
)

// 方法 3：导航前手动清空 store
onBeforeRouteLeave(() => {
  taskStore.reset()
})
```

---

### 4.4 axios 请求没带 cookie

**症状**：登录接口通了，但 `/api/user/info` 返回 401。

**原因**：axios 默认 `withCredentials: false`，跨域时不会带 cookie。

**解决**：

```ts
// src/utils/request.ts
const instance = axios.create({
  baseURL: '/api',
  withCredentials: true,  // ← 加这个
})
```

后端也要开 CORS：

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);  // ← 加这个
    config.addAllowedOriginPattern("*");
    // ...
}
```

---

## 5. 前端构建问题

### 5.1 打包后白屏

**症状**：`npm run build` 成功，部署后打开页面是白屏。

**解决**：

```bash
# 1. 看 console 有没有报错（F12）
# 2. 常见错：
#    - Failed to load resource: 404 → 检查 base 路径
#    - ChunkLoadError → 改了文件但用户用了旧缓存，加 hash 解决
#    - Chunk size > 500KB → 用 dynamic import 拆包
```

```ts
// vite.config.ts
export default defineConfig({
  base: '/3d-print/',  // 部署到子路径时改这个
  build: {
    chunkSizeWarningLimit: 2000,  // 调高警告阈值
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'echarts': ['echarts'],
        },
      },
    },
  },
})
```

---

### 5.2 `vue-tsc` 报一堆 `any` 错误

**症状**：用了 `axios.get<any>()`，但传参时报错。

**解决**：

```ts
// ❌ 错（用了 any）
const res = await axios.get<any>('/api/task/my', { params: { page: 1 } })
console.log(res.data.data.list[0].title)  // any.任何东西 都不报错

// ✅ 对（定义类型）
interface ApiResponse<T> { code: number; message: string; data: T }
interface PageResult<T> { list: T[]; total: number }

const res = await axios.get<ApiResponse<PageResult<PrintTask>>>('/api/task/my', { params: { page: 1 } })
console.log(res.data.data.list[0].title)  // 类型安全！
```

---

## 6. JWT 与登录问题

### 6.1 登录后立刻掉线

**症状**：登录成功跳转 dashboard，但 1 秒后又跳回登录页。

**原因**：JWT 没存到 localStorage / 存储的 key 不对。

**解决**：

```ts
// 1. 看 localStorage 存没存
console.log(localStorage.getItem('token'))
console.log(localStorage.getItem('print-club-auth'))

// 2. 确认 store 初始化时读了
const authStore = useAuthStore()
// Pinia 默认会从 localStorage hydrate（如果用了 pinia-plugin-persistedstate）

// 3. 检查 axios interceptor 有没有正确加 token
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})
```

---

### 6.2 JWT 过期：`401 Unauthorized`

**症状**：操作一段时间后所有接口都 401。

**原因**：JWT 默认 2 小时过期。

**解决**：

```ts
// 方案 1：后端配置长一点（开发环境）
// application-dev.yml
jwt:
  expiration: 86400000  # 24 小时

// 方案 2：前端自动刷新 token
// 在 response interceptor 里加：
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config._retry) {
      error.config._retry = true
      const newToken = await refreshToken()
      error.config.headers.Authorization = `Bearer ${newToken}`
      return axios(error.config)
    }
    return Promise.reject(error)
  }
)
```

---

## 7. 中文乱码问题

### 7.1 数据库存进去的中文是问号 `???`

**原因**：编码链路断了。

**完整解决**（按顺序检查）：

```yaml
# 1. application.yml - Spring 强制 UTF-8
server:
  servlet:
    encoding:
      charset: UTF-8
      force: true   # ← 关键！
```

```java
// 2. JDBC URL 加编码参数
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/print_club?useUnicode=true&characterEncoding=utf8mb4
```

```sql
-- 3. 表的字符集
ALTER TABLE print_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

```bash
# 4. MySQL 客户端编码
mysql -uroot -p --default-character-set=utf8mb4
```

---

### 7.2 PowerShell 控制台输出乱码

**症状**：PowerShell 跑 `mvn spring-boot:run` 时日志中文是 `?`，但数据库里是对的。

**原因**：PowerShell 5.1 默认 GBK，控制台显示 UTF-8 字节时会乱码。

**解决**：

```powershell
# 不影响实际数据，只是显示问题。如果非要修：
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001
```

或者用 Windows Terminal / PowerShell 7+（默认 UTF-8）。

---

### 7.3 前端 axios 提交中文变 `ä¸­æ–‡`

**症状**：中文提交到后端变成乱码。

**解决**：

```ts
// request.ts
const instance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json; charset=utf-8',  // ← 加 charset
  },
})

// 或者用 form-urlencoded
import qs from 'qs'
instance.post(url, qs.stringify(data), {
  headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8' }
})
```

---

## 8. Git 问题

### 8.1 `git push` 被拒绝：`non-fast-forward`

**症状**：
```
! [rejected]        main -> main (non-fast-forward)
```

**原因**：远程 main 比你的本地新。

**解决**：

```bash
# 先 pull 再 push（如果没冲突会自动 merge）
git pull origin main
git push origin main

# 或者 rebase
git pull --rebase origin main
git push origin main
```

---

### 8.2 我 commit 了密码 / 密钥

**症状**：发现 `.env`、`application.yml` 里写了真密码，已经 push 到 GitHub。

**解决**（**立刻**做）：

```bash
# 1. 改密码（不是删文件，是改实际的密码！）
# 2. 删掉历史记录里的密码
# 装 git-filter-repo
pip install git-filter-repo

# 3. 把所有历史里的 password=xxx 替换成 password=${PASSWORD:}
git filter-repo --replace-text <(echo "Xy@20070831==>__REDACTED__")
git filter-repo --replace-text <(echo "your-secret-key==>__REDACTED__")

# 4. 强制 push（需要 GitHub 仓库 Settings 里临时允许 force push）
git push -f origin main --all
```

---

### 8.3 `.gitignore` 不生效

**症状**：把 `node_modules/` 加进 `.gitignore` 但 `git status` 还显示它。

**原因**：文件**已经被跟踪**了，`gitignore` 对已跟踪文件无效。

**解决**：

```bash
# 先从 Git 索引里移除（不删本地文件）
git rm -r --cached node_modules/
git commit -m "chore: 从 Git 移除 node_modules/"

# .gitignore 才会生效
```

---

## 9. PowerShell 测试脚本问题

### 9.1 `Invoke-RestMethod` 中文 body 乱码

**症状**：用 PowerShell 测 POST 接口，中文字段变成 `ä¸­æ–‡`。

**解决**：用 UTF-8 字节数组：

```powershell
# ❌ 错
$body = '{"title":"中文"}'
Invoke-RestMethod -Method POST -Uri $url -Body $body -ContentType "application/json"

# ✅ 对
$utf8 = [System.Text.Encoding]::UTF8
$body = $utf8.GetBytes('{"title":"中文"}')
Invoke-RestMethod -Method POST -Uri $url -Body $body -ContentType "application/json; charset=utf-8"
```

---

### 9.2 PowerShell 哈希表里 `$token` 不解析

**症状**：
```powershell
$loginResp = Invoke-RestMethod -Method POST -Uri "$baseUrl/auth/login" -Body $body
# $loginResp.data.token 有值
$headers = @{ Authorization = "Bearer $loginResp.data.token" }  # ← 这里！
Invoke-RestMethod -Method GET -Uri "$baseUrl/user/info" -Headers $headers
# 报错：Authorization header 没了 token
```

**原因**：PowerShell 的字符串不解析 `$.x.y` 链式访问，只认简单变量。

**解决**：

```powershell
# ✅ 先提取
$token = $loginResp.data.token
$headers = @{ Authorization = "Bearer $token" }
```

---

## 10. IDEA 启动坑

### 10.1 用 IDEA Run 按钮启动后端报错，但 PowerShell `mvn spring-boot:run` 正常

**症状**：IDEA 启动报一堆错（找不到 driver、profile 没激活等），但命令行启动正常。

**原因**：IDEA 的 `.idea/` 缓存了过时的配置。

**解决**：

```bash
# 1. 删 .idea/workspace.xml
rm -rf .idea/workspace.xml

# 2. IDEA 里右键 pom.xml → Maven → Reload project

# 3. Run Configurations 里手动指定：
#    - Active profiles: dev
#    - Environment variables: DB_PASSWORD=Xy@20070831

# 4. 如果还不行，用 PowerShell 启动
.\scripts\start-backend.ps1
```

**建议**：**永远用 PowerShell 启动后端**，最稳。

---

### 10.2 IDEA Maven Projects 面板里项目是灰色的

**症状**：Maven 面板里项目变灰，不能点 lifecycle。

**解决**：

```bash
# 1. 关闭 IDEA
# 2. 删掉
rm -rf .idea/

# 3. 重新打开 IDEA
# 4. 右键 pom.xml → Add as Maven Project
```

---

## 11. 数据库连接密码与登录密码搞混

**症状**：改了 `application.yml` 里的密码，然后登录系统失败。

**澄清**：

| 用途 | 配置文件 | 默认值 | 怎么改 |
|---|---|---|---|
| MySQL 数据库连接 | `application-dev.yml` → `spring.datasource.password` | `Xy@20070831` | 环境变量 `DB_PASSWORD` |
| 登录系统（4 个测试账号） | 数据库 `member.password` 字段 | bcrypt 加密后的 `123456` | 数据库里改，或重跑 `init-bcrypt.sql` |

**两个密码是独立的！**

```sql
-- 改登录密码（bcrypt 加密后的 123456）
UPDATE member SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' WHERE student_id = '2021001';
-- 这个 hash 对应明文密码 "123456"

-- 或者直接重跑建库脚本
mysql -uroot -p < spring-boot-backend/docs/SQL/init-bcrypt.sql
```

---

## 12. 代码被覆盖 / 误删

### 12.1 我的代码被别人的 commit 覆盖了

**症状**：`git pull` 后我的代码没了。

**解决**：

```bash
# 1. 先看 reflog 找你的 commit
git reflog
# 找到你的 commit hash，比如 a1b2c3d

# 2. 恢复
git checkout a1b2c3d
# 或者把那个 commit cherry-pick 回来
git cherry-pick a1b2c3d
```

**预防**：

- 永远在 `feature/` 分支上开发，不要在 `main` 上
- push 前先 pull rebase

---

### 12.2 我想撤回某次 commit

```bash
# 撤回但保留改动
git revert <commit-hash>

# 完全删掉（危险！只用于还没 push 的本地 commit）
git reset --hard <commit-hash>^
```

---

## 13. 生产环境部署问题

### 13.1 部署后接口 404

**症状**：本地能跑，部署后所有接口 404。

**原因**：Nginx 没配反向代理。

**解决**：

```nginx
# /etc/nginx/conf.d/print-club.conf
server {
    listen 80;
    server_name print.example.com;

    # 前端静态文件
    location / {
        root /var/www/print-club/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

### 13.2 部署后中文乱码

**Nginx 配置**：

```nginx
http {
    charset utf-8;
    server {
        charset utf-8;
        # ...
    }
}
```

---

## 0. 求救流程

如果上面的都没解决：

1. **截图**（错误信息 + 文件路径 + 行号）
2. **描述复现步骤**（我做了什么 → 出现什么）
3. **在群里 @组长**，附上截图
4. 不要自己瞎试各种"网上搜到的"方案 → 可能把代码搞炸

---

**写于第 N 次踩坑之后，痛定思痛。**


## 14. v2 新增踩坑（2026-06 期间）

### 14.1 审计日志完全断裂 — `system_log` 表一直为 0

**症状**：
- `system_log` 表无任何记录
- `/admin/log` 页面一直空
- 业务操作（申请任务 / 入库 / 改角色）都查不到审计

**原因**：`LogServiceImpl.record()` 虽然有 `@Async` 注解，但**没有任何一个 controller/service 调用过它**——审计链路从代码层就完全断裂。

**解决**：
1. 在 6 个 service（Task / Project / User / Material / Auth / Artwork）的 25+ 关键方法末尾加 `logService.recordCurrent("xxx.xxx", "type", "id", "desc")`
2. **`@Async` 必须配 `@EnableAsync`**（放在主类上），否则异步不生效
3. `recordCurrent` 内部调 `record` 必须用 **self-injection**（`@Lazy @Autowired private LogService self;`），否则 self-invocation 绕过 Spring AOP 代理，`@Async` 还是失效
4. 详见 `spring-boot-backend/src/main/java/com/printclub/module/log/service/LogService.java` 的 javadoc（含完整 AOP 化 TODO 注释）

---

### 14.2 `@Async` 不生效，方法还是同步执行

**症状**：方法加了 `@Async`，调用方还是阻塞等待；SQL 写入慢，主流程卡住。

**原因**：
1. **缺 `@EnableAsync`**：Spring Boot 不会自动开启异步
2. **self-invocation 陷阱**：同类内 `this.asyncMethod()` 不走代理，`@Async` 失效

**解决**：
```java
@SpringBootApplication
@EnableAsync  // 必须加！
public class PrintClubApplication { }

@Service
public class LogServiceImpl {
    @Autowired
    @Lazy  // 解决循环依赖
    private LogService self;  // 通过代理调用

    public void recordCurrent(...) {
        self.record(...);  // 走代理，@Async 才生效
    }

    @Async
    public void record(...) { ... }
}
```

---

### 14.3 Spring 静态资源 `addResourceHandler` 跨协议 fallback 不靠谱

**症状**：
- `WebMvcConfig` 配了 `classpath:/static/` 和 `file:/uploads/` 两个 location
- 但 `classpath:/static/uploads/placeholder.png` 找不到
- 只 `file:/uploads/xxx` 能访问

**原因**：Spring 6+ 改了行为，跨协议 fallback 不可靠（classpath: 协议优先级被忽略）。

**解决**：**不要依赖多 location fallback**。直接复制资源到 `file:` 协议目录：
- 静态占位图 → 复制到 `D:/project/.../uploads/placeholder/xxx.png`（磁盘）
- WebMvcConfig 只需要 `file:磁盘路径` 一个 location
- 重启不需要重新部署，磁盘文件改了直接生效

---

### 14.4 MyBatis-Plus `Page` 序列化字段不一致

**症状**：前端 `store.list.list` 拿不到数据，调试发现后端返回的 JSON 字段是 `records` 而前端代码写的是 `list`。

**原因**：MyBatis-Plus `Page` 内部字段叫 `records`，但前端希望叫 `list`。

**解决**：写一个 `PageUtils.toResult(Page<T>)` 统一转换（`records → list`，`current → page`），所有分页接口都走这个工具，**不能直接返回 `Page<T>`**。

---

### 14.5 ElMessageBox 关闭按钮跑到左上角

**症状**：Element Plus 弹窗右上角的 × 关闭按钮位置错乱，跑到了**左上角**（紧贴弹窗边）。

**原因**：CSS 拦截时给 `.el-message-box__header` 设了 `height: 0`（为了加自定义 6px 顶部装饰条），但 `.el-message-box__headerbtn` 默认是 `position: absolute; right: 24px; top: 18px;`（**相对 header**）。header height=0 后，× 错位。

**解决**：把关闭按钮**绝对定位到弹窗容器本身**（不靠 header）：
```scss
.el-message-box__headerbtn {
  position: absolute !important;
  top: 12px !important;
  right: 12px !important;
  z-index: 10;
  border-radius: 50%;
  width: 32px; height: 32px;
  background: rgba(255, 255, 255, 0.92);
  &:hover {
    background: #FF4757; color: white;
    transform: rotate(90deg) scale(1.1);
  }
}
```

---

### 14.6 AppDialog 整页磨砂玻璃

**症状**：登录后整个页面被磨砂玻璃糊住，所有文字模糊不清。

**原因**：在 AppHeader 的 logout 弹窗用了 `.glass-modal` 类（`backdrop-filter: blur(12px)`）作为 modal-class prop，EP 把这个类应用到 `.el-overlay`（遮罩层），导致遮罩层半透明白色 + 磨砂，整页都被糊。

**解决**：
1. 删掉 AppHeader.vue 的 `backdrop-filter`（`.glass-modal` / `.logout-dialog` / `.btn-cancel` 全部删除）
2. 全局加防御 reset（`src/styles/index.scss`）：

```scss
.el-overlay, .el-message-box, .el-dialog, .glass-modal,
.logout-dialog, .app-dialog, .app-dialog-icon {
  backdrop-filter: none !important;
  -webkit-backdrop-filter: none !important;
}
```

---

### 14.7 Spring Boot `MultipartFile.transferTo` 报 FileNotFoundException

**症状**：文件上传后端 `transferTo(dest)` 抛 `FileNotFoundException: ...\uploads\xxx.stl`。

**原因**：
1. `file.upload-dir: ./uploads/` 是相对路径 → Spring Boot 实际工作目录是 Tomcat 临时目录
2. `transferTo` 内部用 `Files.move` 把 multipart temp 文件 move 到目标 — 但 temp 文件可能已被清理
3. 父目录不存在

**解决**（三件套，缺一不可）：
```yaml
# application.yml — 绝对路径
file:
  upload-dir: D:/project/spring-boot-backend/uploads/
```

```java
// FileUploadUtil.save() — 用 Files.copy 替代 transferTo
Path target = Paths.get(uploadDir + relativePath);
FileUtil.mkParentDirs(target.toFile());  // 兜底创建父目录
try (InputStream in = file.getInputStream()) {
    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);  // 不用 transferTo
}
```

---

### 14.8 MySQL Connector/J 8.x 中文乱码

**症状**：存进数据库的中文是 `Ã©Ã¨Ã Â ??` 之类的乱码。

**原因**：JDBC URL 的 `characterEncoding=utf8` 在新驱动里行为不稳定（用小写 `utf8`）。

**解决**：URL 用**大写 UTF-8 + connectionCollation**（三件套）：
```yaml
url: jdbc:mysql://localhost:3306/db?useUnicode=true
    &characterEncoding=UTF-8
    &connectionCollation=utf8mb4_unicode_ci
    &useSSL=false
    &serverTimezone=Asia/Shanghai
    &allowPublicKeyRetrieval=true
```

**同时** application.yml 加：
```yaml
server:
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
```

**清理历史脏数据**：
```sql
DELETE FROM print_task WHERE title REGEXP '(Ã©|Ã¨|Ã |Â|??)';
-- 验证：SELECT HEX(LEFT(title,20)) -- UTF-8 中文应该是 E6B58BE8AF95... 形态
```

---

### 14.9 Lombok `@Slf4j` 字段名遮蔽陷阱

**症状**：`@Slf4j` 类里加字段 `private String log;` 后编译报 `variable log might not have been initialized`，或运行时 NullPointerException。

**原因**：`@Slf4j` 自动生成 `private static final Logger log = LoggerFactory.getLogger(...)`，**字段名就是 `log`**。自己再声明 `log` 字段会遮蔽 Lombok 生成的 logger。

**解决**：**永远不要在 `@Slf4j` 类里命名 `log` 字段**，改成 `changeLog` / `operationLog` / `materialLog` 等。

---

### 14.10 Hutool `StrUtil.blankToDefault` 只接 2 参数

**症状**：`StrUtil.blankToDefault(value, defaultValue, "extra")` 编译报 "方法不存在"。

**原因**：Hutool `blankToDefault` 签名是 `(str, defaultStr)` —— 只有 2 个参数。

**解决**：删掉第 3 个参数或用三目运算：`StrUtil.blankToDefault(a, "b") + (c != null ? c : "")`。

---

### 14.11 `RequestContextHolder` 在非 Web 线程拿不到 IP

**症状**：日志写 IP 时用 `RequestContextHolder.getRequestAttributes()` 报 NullPointerException。

**原因**：定时任务 / 启动加载 / @Async 异步线程里没有 HTTP 请求上下文。

**解决**：**用 try-catch 包住**：
```java
String ip = "internal";
try {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs != null) {
        // 正常取 IP
    }
} catch (Exception ignore) {
    // 非 Web 线程，定时任务 / 异步 / 启动加载 — 用 "internal" 占位
}
```

**永远不要假设** `RequestContextHolder` 一定有值。

---

### 14.12 提交到 GitHub 时 push 失败 `Failed to connect to github.com port 443`

**症状**：
```
fatal: unable to access 'https://github.com/...':
Failed to connect to github.com port 443 after 21044 ms: Could not connect to server
```

**原因**：网络层问题（VPN/代理未开 / 防火墙 / DNS 污染）。

**解决**：
```powershell
# 1. 测试网络
Test-NetConnection github.com -Port 443
ping github.com

# 2. 如果 ping 不通 — 换 SSH 协议
git remote set-url origin git@github.com:用户名/仓库.git
git push origin main
# 需要先配 SSH key：ssh-keygen → 把 .pub 加到 GitHub Settings → SSH keys

# 3. 如果 ping 通但 push 失败 — 配 GitHub Personal Access Token
# GitHub → Settings → Developer settings → Personal access tokens → 选 repo 权限
# 然后 push 时用 token 当密码
```

---

## 15. v2.1 部署/上线踩坑（2026-06-29）

> 本节专治"本地跑得好好的，部署到生产就跪"的所有问题。
> 涵盖阿里云 ECS + Cloudflare Pages + Cloudflare 反代 全链路。

### 15.1 Spring Boot 3.x CORS `allowedOriginPatterns` token-level 通配符陷阱

**症状**：本地 `mvn spring-boot:run` + 浏览器 fetch 调用完全 OK，但部署到 ECS + 经过 Cloudflare 反代后，**所有 POST/PUT 接口返回 403 Forbidden**。

```
错误响应：
HTTP/1.1 403 Forbidden
Vary: Origin
Access-Control-Allow-Origin: https://3d-printing-management-system.pages.dev
Access-Control-Allow-Credentials: true
```

**原因**：

之前的 CorsConfig 用的是 WebMvcConfigurer 风格的写法：
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns("https://*.pages.dev")  // ❌ 这个写法从一开始就是错的
        .allowedMethods("GET", "POST", ...)
        .allowCredentials(true);
}
```

**Spring 6.x `CorsConfiguration.matchOriginPattern` 是 token-level 匹配**：用 `.` 拆分 origin 后**每个 token 必须完全相等或等于 `*`**。

- `https://*.pages.dev` 拆成 `["https://*", "pages", "dev"]`
- 实际请求 origin `https://3d-printing-management-system.pages.dev` 拆成 `["https://3d-...", "pages", "dev"]`
- 第一段 `https://*` vs `https://3d-...` 不相等 → **不匹配 → 403**

**为什么本地没暴露**：本地 curl 不带 Origin 头，绕过 CorsFilter；只有浏览器 fetch 才带。

**解决**：

**方案 A（最简）**：直接接受所有 origin（适合 JWT Authorization 认证，不用 cookie）：
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(false);  // ← 关键：用 JWT Authorization 头，不用 cookie
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**方案 B（精确）**：列出每个具体域名（适合多域名生产）：
```java
config.setAllowedOriginPatterns(List.of(
    "https://3d-printing-management-system.pages.dev",
    "https://3dprint.ccwu.cc",
    "http://localhost:5173"  // dev
));
```

**nginx 同步处理 OPTIONS 预检**（关键，不进 Spring Boot）：
```nginx
location / {
    if ($request_method = 'OPTIONS') {
        add_header Access-Control-Allow-Origin "$http_origin" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
        add_header Access-Control-Max-Age 3600;
        add_header Content-Type 'text/plain; charset=utf-8';
        add_header Content-Length 0;
        return 204;  # 预检直接返
    }
    # 普通请求反代 + 所有响应加 CORS 头
    proxy_pass http://127.0.0.1:8080;
    add_header Access-Control-Allow-Origin "$http_origin" always;  # ← always，覆盖错误响应
    add_header Access-Control-Allow-Credentials "true" always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
}
```

**`always` 标志的作用**：默认 `add_header` 只在 2xx/3xx 响应加；`always` 让 4xx/5xx 错误响应也带头，否则浏览器看到的错误响应没有 CORS 头 → 报错更迷糊。

---

### 15.2 PowerShell 5.1 HTTPS POST 到 Cloudflare 时 body 字符破坏

**症状**：用 PowerShell 测 Cloudflare 反代 POST 接口，body JSON 字符串 `"studentId":"2023010001"` 传到后端变成 `"studentId":"2023010001\x1B[200~..."` 或 `Unexpected character 's'` 解析错误。

```
curl.exe -X POST "https://api.3dprint.ccwu.cc/api/auth/login" -d '{"studentId":"2023010001","password":"123456"}'
# 后端日志：Unexpected character 's' at position 1
```

**原因**：
1. PowerShell 5.1 默认 `curl` 是 `Invoke-WebRequest` 别名（不是真正的 curl），它会**自动 wrap body**
2. Cloudflare 的边缘节点对某些 HTTPS POST body 做了字符集转换（极少但会遇到）
3. OpenSSH 粘贴密码时插入了 `^[[200~` ESC sequence

**解决**：
- **PowerShell 用真正的 curl.exe**：`curl.exe -X POST ...`（不是 `curl`）
- **body 用单引号包裹**（避免 PowerShell 变量展开）：`curl.exe -d '{"key":"value"}'`
- **HTTPS body 用 `--data-raw` 而不是 `-d`**：`--data-raw '{"key":"value"}'`（防止 curl 改 `@` 符号）
- **如果怀疑有 ESC sequence，用 `[byte]` 数组发送**：
  ```powershell
  $body = [System.Text.Encoding]::UTF8.GetBytes('{"studentId":"2023010001","password":"123456"}')
  Invoke-RestMethod -Uri $url -Method POST -Body $body -ContentType "application/json; charset=utf-8"
  ```

**关键经验**：**PowerShell 5.1 测试 Cloudflare HTTPS POST 不可靠**。最稳的是用前端 axios（自动处理 body 编码）或 Postman。

---

### 15.3 Element Plus `<el-tag>` 在浅色主题对比度差（必须 `effect="dark"`）

**症状**：浅色主题下，状态标签 `<el-tag type="success">已发布</el-tag>` **背景是浅绿、文字是浅绿**，对比度只有 ~1.5:1，肉眼几乎看不清（WCAG AA 要求 4.5:1）。

**原因**：Element Plus 2.14 默认 `effect="light"`（浅色背景 + 浅色文字），**只在暗色主题或显式 `effect="dark"` 时才有足够对比度**。项目全局默认 light，所以全站都糊。

**解决**：

**手工修 3 处最高频**：
```vue
<!-- ❌ 错 -->
<el-tag type="success">已发布</el-tag>

<!-- ✅ 对 -->
<el-tag type="success" effect="dark">已发布</el-tag>
```

**Python 脚本批量改**（覆盖 13 个文件 30+ 处）：
```python
import re
import glob

# 用 (?:[^>]*?) 让空标签也能匹配
pattern = re.compile(r'<el-tag (?![^>]*effect=)((?:[^>]*?))(/?)>')
replacement = r'<el-tag \1 effect="dark"\2>'

for filepath in glob.glob(r"D:\summer\3D-Printing-Management-System\vue-3d\src\views\**\*.vue", recursive=True):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    matches = pattern.findall(content)
    if not matches:
        continue
    new_content = pattern.sub(replacement, content)
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"[OK] {filepath}: {len(matches)} 处")
```

**脚本踩坑**：第一版正则用 `[^>]+?`（`+` = 至少 1 字符），漏掉了无属性的 `<el-tag>`（如 `<el-tag>{{ name }}</el-tag>`），改用 `[^>]*?`（`*` = 0+ 字符）才正确。

**全局默认值**：在 `StatusTag.vue` 组件默认 `effect="dark"`，新写的标签自动有对比度。

---

### 15.4 中国大陆访问不到 `*.workers.dev` 和 `api.trycloudflare.com`

**症状**：在阿里云 ECS 测试 Cloudflare Worker 反代成功（`curl https://xxx.workers.dev/api/auth/login` 返回 token），但用户在浏览器打开 `xxx.workers.dev` **永远转圈**，DevTools 显示 `net::ERR_CONNECTION_TIMED_OUT`。

**原因**：
- `*.workers.dev` 域名被 GFW（Great Firewall）屏蔽
- `api.trycloudflare.com`（临时隧道域名）也被屏蔽
- **所有 `*.workers.dev` 域名在中国大陆都不可达**，无论 Worker 内容是什么

**解决**：

**不用 Worker / Tunnel，用 Cloudflare 反代（NS + A 记录 Proxied）**：
1. 买一个免费子域名（`ccwu.cc` / `eu.org` / `js.org` 等支持改 NS 的）
2. 在 Cloudflare Dashboard → Add Site → 填域名 → Cloudflare 分配两个 NS（`sue.ns.cloudflare.com` / `tim.ns.cloudflare.com`）
3. 在域名注册商改 NS 指向 Cloudflare 的两个 NS（**不是 A 记录**）
4. 等 NS 生效（5-30 分钟）后，Cloudflare Dashboard 显示"Active"
5. 加 A 记录 `api.3dprint.ccwu.cc` → ECS IP `8.137.80.194` → **橙色云朵 Proxied**（不是灰色云朵 DNS only）
6. SSL/TLS 模式选 **Flexible**（Cloudflare → ECS 用 HTTP，不需要在 ECS 配 HTTPS 证书）
7. 前端 `.env.production` 改成 `VITE_API_BASE_URL=https://api.3dprint.ccwu.cc/api`

**橙色云朵 vs 灰色云朵**：
- 🟠 Proxied：流量走 Cloudflare 反代，隐藏真实 IP，能用 Cloudflare CDN/安全功能
- ⚪ DNS only：只做 DNS 解析，直接暴露真实 IP（用户能直接访问 ECS IP，不走 Cloudflare）

---

### 15.5 ssh 密码粘贴插入 ESC sequence `^[[200~`

**症状**：用 PowerShell 复制 ECS root 密码到 OpenSSH 终端粘贴，密码前面多了 `^[[200~`，导致 `Permission denied (publickey,password)`。

**原因**：PowerShell 的剪贴板在粘贴时会插入 `bracketed paste mode` 的开始/结束标记（`\x1B[200~` / `\x1B[201~`），OpenSSH 终端把它当作字面字符处理。

**解决**：
- **方案 A（推荐）**：手输密码（OpenSSH 密码提示不支持粘贴）
- **方案 B**：用 PowerShell 命令行 `sshpass`（Windows 没原生 sshpass，要装 MSYS2 / WSL）
- **方案 C**：配 SSH 公私钥免密登录（最稳）：
  ```bash
  # 本地
  ssh-keygen -t ed25519
  type $env:USERPROFILE\.ssh\id_ed25519.pub | ssh root@8.137.80.194 "cat >> ~/.ssh/authorized_keys"
  # 之后 ssh root@8.137.80.194 直接进（不输密码）
  ```

---

### 15.6 阿里云 ECS 1.6 GiB RAM 内存压榨（MySQL + Spring Boot + Nginx）

**症状**：阿里云 ECS 1.6 GiB RAM，跑 Spring Boot + MySQL + Nginx 时 OOM（Out of Memory Killer）杀掉 MySQL。

**内存预算**：
- MySQL 8 默认 `innodb_buffer_pool_size` ≈ 800M（占一半 RAM）
- Spring Boot 默认 JVM heap ≈ 512M（`-Xmx`）
- OS + 文件系统缓存 ≈ 300M
- Nginx ≈ 5M
- **合计 ≈ 1.6 GB** → 没有 swap 时 OOM

**解决**（三件套）：
```bash
# 1. 加 4 GiB swap（兜底）
fallocate -l 4G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
# 持久化
echo '/swapfile none swap sw 0 0' >> /etc/fstab
free -h  # 验证

# 2. MySQL 限制内存
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
# 加：
[mysqld]
innodb_buffer_pool_size = 128M
innodb_log_file_size = 64M
max_connections = 50
sudo systemctl restart mysql

# 3. Spring Boot 限制 JVM heap
# /etc/systemd/system/printclub.service
ExecStart=/usr/bin/java -Xmx512m -jar /opt/printclub/app/print-club-backend.jar --spring.profiles.active=prod
sudo systemctl daemon-reload
sudo systemctl restart printclub
```

**验证**：
```bash
free -h
ps aux --sort=-%mem | head -10
journalctl -u printclub -n 30 | grep -i "heap\|memory"
```

---

### 15.7 Login DTO 字段是 `studentId` 不是 `username`

**症状**：curl 测试登录接口一直 400 "学号不能为空"，即使 body 有 `"username":"2023010001"`。

**原因**：项目 LoginDTO 字段命名是学号（`studentId`），不是通用 `username`：
```java
@Data
public class LoginDTO {
    @NotBlank(message = "学号不能为空")
    @Size(min = 8, max = 20)
    private String studentId;  // ← 不是 username
    private String password;
}
```

**正确 body**：
```bash
curl.exe -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"studentId":"2023010001","password":"123456"}'

# 返 {"code":200,"data":{"token":"eyJ..."}}
```

---

### 15.8 `vue-tsc --build` 退出码 0 但 `npm run build` 跳过类型检查

**症状**：CI 上 `vue-tsc --build` 通过，但生产环境运行时浏览器 console 报 `Cannot read property 'X' of undefined` —— 严格说 `vue-tsc` **没报错**，但 Vite 构建时跳过了类型检查。

**原因**：
- `vue-tsc --build` 只检查 .ts 文件，不深度检查 .vue `<script setup>` 里的类型
- `npm run build` 默认调用 `vue-tsc --noEmit && vite build`，但很多项目把 `vue-tsc` 拿掉只跑 `vite build`（为了构建快）

**解决**：
- **CI 强制跑 `vue-tsc --noEmit`**：
  ```bash
  # package.json
  "scripts": {
    "build": "vue-tsc --noEmit && vite build",
    "type-check": "vue-tsc --noEmit"
  }
  ```
- **`--noEmit` 不输出 .js，只做类型检查**——比 `--build` 更准确
- **如果还想更深检查**：用 `vue-tsc --noEmit --skipLibCheck` + `tsc --noEmit` 双重跑

---

### 15.9 Cloudflare Pages Functions 与前端 `vite build` 打架

**症状**：Cloudflare Pages 部署时日志报 `Could not resolve './functions/...'`，前端构建产物不完整。

**原因**：`vue-3d/functions/` 目录是 Cloudflare Pages Functions 的入口，会被 Cloudflare 自动识别为 worker 函数。如果 `vite build` 也扫到 `functions/` 里的代码，会试图把它打进前端 bundle，结果冲突。

**解决**：
- **删除 `vue-3d/functions/` 目录**（如果用 Cloudflare 反代代替 Pages Functions）
- 或者**改用 Pages Functions 方案**（用 `functions/api/[[path]].js` 写 worker 反代）：
  ```js
  // functions/api/[[path]].js
  export async function onRequest(context) {
    const url = new URL(context.request.url)
    url.hostname = '8.137.80.194'  // ECS IP
    return fetch(url, context.request)
  }
  ```

---

## 0. 求救流程

如果上面的都没解决：

1. **截图**（错误信息 + 文件路径 + 行号）
2. **描述复现步骤**（我做了什么 → 出现什么）
3. **在群里 @组长**，附上截图
4. 不要自己瞎试各种"网上搜到的"方案 → 可能把代码搞炸

---

**写于第 N 次踩坑之后，痛定思痛。**

