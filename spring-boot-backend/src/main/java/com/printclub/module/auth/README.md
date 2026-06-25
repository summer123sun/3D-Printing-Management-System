# auth

登录模块（D 负责）

接口：`/api/auth/login` `/api/auth/logout`

## 文件

- `controller/AuthController.java`
- `service/AuthService.java` + `AuthServiceImpl.java`
- `dto/LoginDTO.java` — 登录请求
- `dto/LoginVO.java` — 登录返回（含 JWT token + 用户信息）

## 关键逻辑

- 密码 BCrypt 校验
- 签发 JWT（含 studentId + role + 过期时间 7 天）
