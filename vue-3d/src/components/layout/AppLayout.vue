<script setup lang="ts">
/**
 * 顶层布局
 * - 社长 / 技术骨干 (role 1/2): 完整布局（顶部 header + 左侧 sidebar + 主区）
 * - 普通社员 / 新成员 (role 3/4): 简化布局（仅顶部 header，主区铺满，无 sidebar）
 *   - 全屏背景图 beijing.png（v2.7）
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import AppMain from './AppMain.vue'
import CursorEffect from '@/components/common/CursorEffect.vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { Role } from '@/types/member'

const appStore = useAppStore()
const authStore = useAuthStore()

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

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<template>
  <!-- 成员端：仅 header + 主区（无 sidebar），全屏背景图 -->
  <template v-if="isMemberLayout()">
    <div class="member-bg">
      <div class="member-bg-image" />
      <div class="member-bg-overlay" />
    </div>
    <el-container class="app-layout app-layout-member">
      <el-header class="app-header app-header-transparent">
        <AppHeader @toggle-sidebar="drawerVisible = !drawerVisible" />
      </el-header>
      <el-main class="app-main app-main-member">
        <AppMain />
      </el-main>
    </el-container>
    <!-- 自定义鼠标 + 点击涟漪（仅成员端） -->
    <CursorEffect />
  </template>

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

// 成员端：背景层（fixed 全屏，盖在所有内容下层）
.member-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}
.member-bg-image {
  position: absolute;
  inset: 0;
  background-image: url('@/assets/member/beijing.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  // 轻微缩放 + 缓慢漂移，让背景"活"起来
  animation: bg-drift 30s ease-in-out infinite alternate;
}
.member-bg-overlay {
  // 半透明深色蒙版，确保前景文字可读
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(10, 37, 64, 0.35) 0%,
    rgba(10, 37, 64, 0.55) 100%
  );
}
@keyframes bg-drift {
  0%   { transform: scale(1.05) translate(0, 0); }
  100% { transform: scale(1.12) translate(-2%, -1%); }
}

.app-layout-member {
  position: relative;
  z-index: 1;
  height: 100vh;
  background: transparent;  // 背景层在 .member-bg，主区透明
  .app-main-member {
    padding: 0;
    background: transparent;
  }
}
.app-header-transparent {
  background: rgba(255, 255, 255, 0.78) !important;
  backdrop-filter: blur(12px) saturate(160%) !important;
  -webkit-backdrop-filter: blur(12px) saturate(160%) !important;
  border-bottom: 1px solid rgba(10, 37, 64, 0.08) !important;
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