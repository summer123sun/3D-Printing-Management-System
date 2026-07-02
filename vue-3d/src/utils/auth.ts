/**
 * Token 存取工具
 */

// ✅ v2.12 修复（审查发现）：之前 TOKEN_KEY = 'print_club_token'，
//    stores/auth.ts:70 监听的是 'auth-token' —— 两个 key 对不上导致多 Tab 同步从未真生效过
//    统一改用 'auth-token'（与 stores/auth.ts 注释和监听器一致）
const TOKEN_KEY = 'auth-token'
const USER_KEY = 'auth-user'

export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

export const getUserInfo = <T = unknown>(): T | null => {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export const setUserInfo = <T>(user: T): void => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const removeUserInfo = (): void => {
  localStorage.removeItem(USER_KEY)
}

export const clearAuth = (): void => {
  removeToken()
  removeUserInfo()
}