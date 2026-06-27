/**
 * 全局 UI 状态
 */
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

const DARK_KEY = 'app-dark-mode'

// 初始化：从 localStorage 读取（防止刷新丢失）
function readInitialDark(): boolean {
  if (typeof window === 'undefined') return false
  try {
    return localStorage.getItem(DARK_KEY) === '1'
  } catch {
    return false
  }
}

export const useAppStore = defineStore('app', () => {
  // 侧栏折叠
  const sidebarCollapsed = ref<boolean>(false)

  // 暗色模式（持久化到 localStorage）
  const isDark = ref<boolean>(readInitialDark())

  // 全局 loading
  const globalLoading = ref<boolean>(false)

  // 面包屑
  const breadcrumbs = ref<Array<{ title: string; path?: string }>>([])

  // 初始化时同步到 html
  if (typeof document !== 'undefined') {
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  // 监听变化 → 同步到 DOM + localStorage
  watch(isDark, (v) => {
    if (typeof document !== 'undefined') {
      document.documentElement.classList.toggle('dark', v)
    }
    try { localStorage.setItem(DARK_KEY, v ? '1' : '0') } catch { /* noop */ }
  })

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const toggleDark = () => {
    isDark.value = !isDark.value
  }

  const setGlobalLoading = (loading: boolean) => {
    globalLoading.value = loading
  }

  const setBreadcrumbs = (crumbs: Array<{ title: string; path?: string }>) => {
    breadcrumbs.value = crumbs
  }

  return {
    sidebarCollapsed,
    isDark,
    globalLoading,
    breadcrumbs,
    toggleSidebar,
    toggleDark,
    setGlobalLoading,
    setBreadcrumbs,
  }
})