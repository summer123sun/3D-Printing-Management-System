/**
 * 全局 UI 状态
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧栏折叠
  const sidebarCollapsed = ref<boolean>(false)

  // 暗色模式（保留 @vueuse/core 的 useDark 接口）
  const isDark = ref<boolean>(false)

  // 全局 loading
  const globalLoading = ref<boolean>(false)

  // 面包屑（页面级别自己控制，这里只提供存储）
  const breadcrumbs = ref<Array<{ title: string; path?: string }>>([])

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const toggleDark = () => {
    isDark.value = !isDark.value
    document.documentElement.classList.toggle('dark', isDark.value)
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