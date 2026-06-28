<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { Role } from '@/types/member'

const appStore = useAppStore()
const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

interface MenuItem {
  path: string
  title: string
  icon: string
  children?: MenuItem[]
}

/** 根据路由表生成菜单（按角色过滤） */
const menus = computed<MenuItem[]>(() => {
  // 从路由的子路由中筛出菜单项
  const rootRoutes = router.options.routes.filter((r) => r.path !== '/login' && !r.path.startsWith('/:'))
  const result: MenuItem[] = []

  for (const r of rootRoutes) {
    // 顶层 path 是 / 的不算菜单（首页直接显示）
    if (r.path === '/') continue

    // 角色过滤
    const requiredRoles = (r.meta?.roles as number[] | undefined) ?? []
    if (requiredRoles.length > 0) {
      const userRole = authStore.user?.role ?? Role.NEWBIE
      if (!requiredRoles.includes(userRole)) continue
    }

    // 跳过 hidden 的
    if (r.meta?.hidden) continue

    // 取第一个子路由的 path 作为跳转目标
    const children = (r.children ?? [])
      .filter((c) => !c.meta?.hidden)
      .map((c) => ({
        path: `/${r.path.replace(/^\//, '')}/${c.path.replace(/^\//, '')}`,
        title: (c.meta?.title as string) || c.name?.toString() || '',
        icon: (c.meta?.icon as string) || '',
      }))

    result.push({
      path: `/${r.path.replace(/^\//, '')}`,
      title: (r.meta?.title as string) || r.name?.toString() || '',
      icon: (r.meta?.icon as string) || '',
      children: children.length > 0 ? children : undefined,
    })
  }

  return result
})

const activeMenu = computed(() => route.path)
</script>

<template>
  <div class="sidebar-container">
    <!-- Logo -->
    <div class="sidebar-logo">
      <span v-if="!appStore.sidebarCollapsed" class="logo-text">3D 科创会</span>
      <span v-else class="logo-mini">3D</span>
    </div>

    <!-- 菜单 -->
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebarCollapsed"
      :collapse-transition="false"
      background-color="transparent"
      text-color="#303133"
      active-text-color="#1e88e5"
      router
    >
      <template v-for="menu in menus" :key="menu.path">
        <!-- 无子菜单：直接渲染菜单项 -->
        <el-menu-item v-if="!menu.children || menu.children.length === 0" :index="menu.path">
          <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
          <template #title>{{ menu.title }}</template>
        </el-menu-item>

        <!-- 有子菜单：渲染子菜单 -->
        <el-sub-menu v-else :index="menu.path">
          <template #title>
            <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
            <span>{{ menu.title }}</span>
          </template>
          <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
            <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
            <template #title>{{ child.title }}</template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </div>
</template>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;  // 容器不滚动，只让菜单区滚
}
.sidebar-logo {
  flex-shrink: 0;  // logo 不被压缩
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border-extra-light);
  color: $brand-color;
  font-weight: 600;
}
.logo-text {
  font-size: $font-size-title;
}
.logo-mini {
  font-size: $font-size-large;
  font-weight: 700;
}

:deep(.el-menu) {
  border-right: none;
  flex: 1;
  // 菜单区独立滚动（菜单多时滚动显示）
  overflow-y: auto !important;
  overflow-x: hidden !important;
  // 滚动条美化（继承全局深海蓝配色）
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: color-mix(in srgb, $primary-color 20%, transparent);
    border-radius: 3px;
    &:hover { background: color-mix(in srgb, $primary-color 40%, transparent); }
  }
}
:deep(.el-menu-item.is-active) {
  background: $brand-color-lighter !important;
  border-right: 3px solid $accent-color !important;
  color: $primary-color !important;
  font-weight: 600;
}
:deep(.el-sub-menu .el-menu-item.is-active) {
  border-right: none !important;
  background: color-mix(in srgb, $accent-color 10%, transparent) !important;
  color: $accent-color-dark !important;
}
</style>