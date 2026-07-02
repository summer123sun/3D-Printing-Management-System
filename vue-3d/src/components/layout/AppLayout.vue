<script setup lang="ts">
/**
 * 顶层布局
 * - 社长 / 技术骨干 (role 1/2): 完整布局（顶部 header + 左侧 sidebar + 主区）
 * - 普通社员 / 新成员 (role 3/4): 简化布局（仅顶部 header，主区铺满，无 sidebar）
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import AppMain from './AppMain.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { Role } from '@/types/member'

const appStore = useAppStore()
const authStore = useAuthStore()
const route = useRoute()

const isMobile = ref(false)
const drawerVisible = ref(false)

// 是否为成员端（role >= 3）
const isMemberLayout = () => {
  const role = authStore.user?.role ?? Role.NEWBIE
  return role >= Role.MEMBER
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) drawerVisible.value = false
}

// 路由变化时关闭移动端抽屉
const stopRouteWatch = () => {
  // noop，router.afterEach 已替代
}
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
  stopRouteWatch()
})
</script>

<template>
  <!-- 成员端：仅 header + 主区（无 sidebar） -->
  <el-container v-if="isMemberLayout()" class="app-layout app-layout-member">
    <el-header class="app-header">
      <AppHeader @toggle-sidebar="drawerVisible = !drawerVisible" />
    </el-header>
    <el-main class="app-main app-main-member">
      <AppMain />
    </el-main>
  </el-container>

  <!-- 后台端：完整布局 -->
  <el-container v-else class="app-layout">
    <el-container v-if="!isMobile">
      <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="app-aside">
        <AppSidebar />
      </el-aside>
      <el-container>
        <el-header class="app-header">
          <AppHeader @toggle-sidebar="drawerVisible = !drawerVisible" />
        </el-header>
        <el-main class="app-main">
          <AppMain />
        </el-main>
      </el-container>
    </el-container>
    <el-container v-else>
      <el-header class="app-header">
        <AppHeader @toggle-sidebar="drawerVisible = !drawerVisible" />
      </el-header>
      <el-main class="app-main">
        <AppMain />
      </el-main>
      <el-drawer
        v-model="drawerVisible"
        direction="ltr"
        :with-header="false"
        size="260px"
        class="mobile-drawer"
      >
        <AppSidebar @navigate="drawerVisible = false" />
      </el-drawer>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.app-layout {
  height: 100vh;
}
.app-layout-member {
  height: 100vh;
  background: var(--bg-page);
  // 成员端主区完全铺满
  .app-main-member {
    padding: 0;
    background: var(--bg-page);
  }
}
.app-aside {
  background: var(--bg-card);
  border-right: 1px solid var(--border-extra-light);
  transition: width 0.25s ease;
  overflow: hidden;
}
.app-header {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-extra-light);
  height: $header-height;
  padding: 0;
}
.app-main {
  background: var(--bg-page);
  padding: $spacing-medium;
  height: calc(100vh - #{$header-height});
  overflow-y: auto;
}

// 移动端表格处理：让 el-table 可以横向滚动（不让它撑爆页面）
:deep(.el-table) {
  @include mobile {
    overflow-x: auto;
    display: block;
  }
}
// 移动端卡片样式：让统计数字大一点
:deep(.el-card) {
  @include mobile {
    margin-bottom: $spacing-small;
  }
}
// 移动端主区域去掉 padding，节省屏幕空间
.app-layout-member .app-main-member {
  @include mobile {
    padding: 0;
  }
}
// 移动端弹窗占满屏幕
:deep(.el-message-box) {
  @include mobile {
    width: 92vw !important;
    max-width: 92vw !important;
  }
}
// 移动端抽屉里的 sidebar 填满高度
:deep(.mobile-drawer) {
  .el-drawer__body {
    padding: 0;
    height: 100%;
  }
}
</style>