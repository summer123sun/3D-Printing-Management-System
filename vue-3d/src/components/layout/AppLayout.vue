<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import AppMain from './AppMain.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

// 响应式：移动端用抽屉式 sidebar
const isMobile = ref(false)
const drawerVisible = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) drawerVisible.value = false
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<template>
  <!-- 桌面端：左侧固定 sidebar -->
  <el-container v-if="!isMobile" class="app-layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="app-aside">
      <AppSidebar />
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <AppHeader />
      </el-header>
      <el-main class="app-main">
        <AppMain />
      </el-main>
    </el-container>
  </el-container>

  <!-- 移动端：顶部 + 抽屉式 sidebar -->
  <el-container v-else class="app-layout app-layout-mobile">
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
</template>

<style lang="scss" scoped>
.app-layout {
  height: 100vh;
}
.app-aside {
  background: $bg-card;
  border-right: 1px solid $border-extra-light;
  transition: width 0.25s ease;
  overflow: hidden;
}
.app-header {
  background: $bg-card;
  border-bottom: 1px solid $border-extra-light;
  height: $header-height;
  padding: 0;
}
.app-main {
  background: $bg-page;
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
.app-layout-mobile .app-main {
  @include mobile {
    padding: $spacing-small;
  }
}

// 移动端弹窗占满屏幕
:deep(.el-message-box) {
  @include mobile {
    width: 92vw !important;
    max-width: 92vw !important;
  }
}

// 移动端抽屉里的 sidebar 占满高度
:deep(.mobile-drawer) {
  .el-drawer__body {
    padding: 0;
    height: 100%;
  }
}
</style>