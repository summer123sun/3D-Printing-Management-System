# common

通用组件（D 负责）

跨模块复用，所有业务模块都会用到。

## 子目录

- `result/` — 统一返回格式 `{code, msg, data}`
- `exception/` — 全局异常处理
- `interceptor/` — 拦截器（JWT）
- `annotation/` — 自定义注解（@RequireAuth / @RequireRole）
- `util/` — 工具类（JwtUtil / FileUtil）
