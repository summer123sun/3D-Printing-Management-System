<script setup lang="ts">
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import AppMain from './AppMain.vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
</script>

<template>
  <el-container class="app-layout">
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
</style>