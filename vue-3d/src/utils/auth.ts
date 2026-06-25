/**
 * Token 存取工具
 */

const TOKEN_KEY = 'print_club_token'
const USER_KEY = 'print_club_user'

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