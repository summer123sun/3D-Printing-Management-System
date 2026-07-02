<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Fold, Expand, Moon, Sunny, SwitchButton, User, WarningFilled, Menu as MenuIcon, House } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { RoleText, Role } from '@/types/member'

const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()

// 成员端：role >= 3，隐藏 sidebar 折叠按钮
const isMemberLayout = computed(() => {
  const role = authStore.user?.role ?? Role.NEWBIE
  return role >= Role.MEMBER
})

defineEmits<{
  (e: 'toggle-sidebar'): void
}>()

const logoutVisible = ref(false)

const confirmLogout = () => {
  logoutVisible.value = false
  authStore.logout()
  router.push('/login')
}

const cancelLogout = () => {
  logoutVisible.value = false
}

const goHome = () => {
  router.push('/home')
}
</script>

<template>
  <div class="header-container">
    <div class="header-left">
      <!-- 成员端：直接显示"首页"图标按钮（无 sidebar） -->
      <el-tooltip v-if="isMemberLayout" content="返回首页">
        <el-button text class="member-home-btn" @click="goHome">
          <el-icon :size="20"><House /></el-icon>
        </el-button>
      </el-tooltip>
      <!-- 桌面端：折叠/展开 sidebar（仅后台端） -->
      <el-button v-else text class="desktop-only" @click="appStore.toggleSidebar()">
        <el-icon :size="20">
          <component :is="appStore.sidebarCollapsed ? Expand : Fold" />
        </el-icon>
      </el-button>
      <!-- 移动端：汉堡菜单（emit 给父组件 AppLayout 控制抽屉） -->
      <el-button v-if="!isMemberLayout" text class="mobile-only" @click="$emit('toggle-sidebar')">
        <el-icon :size="20"><MenuIcon /></el-icon>
      </el-button>
      <span class="header-title">{{ $route.meta?.title || '' }}</span>
    </div>

    <div class="header-right">
      <el-tooltip :content="appStore.isDark ? '切换到亮色' : '切换到暗色'">
        <el-button text class="theme-toggle-btn" @click="appStore.toggleDark()">
          <el-icon :size="18">
            <component :is="appStore.isDark ? Sunny : Moon" />
          </el-icon>
        </el-button>
      </el-tooltip>

      <el-dropdown trigger="click">
        <div class="user-info">
          <el-avatar :size="32" :src="authStore.user?.avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="user-name">{{ authStore.user?.name }}</span>
          <span v-if="authStore.user" class="user-role" :class="{
            'role-staff': authStore.user.role === Role.TECH_LEAD,
            'role-member': authStore.user.role === Role.MEMBER,
            'role-newbie': authStore.user.role >= Role.NEWBIE,
          }">
            {{ RoleText[authStore.user.role] }}
          </span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/profile')">
              <el-icon><User /></el-icon> 个人中心
            </el-dropdown-item>
            <el-dropdown-item divided @click="logoutVisible = true">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 退出登录确认对话框（不磨砂，干净半透明遮罩） -->
    <el-dialog
      v-model="logoutVisible"
      :close-on-click-modal="false"
      :show-close="false"
      width="400px"
      modal-class="dark-modal"
      custom-class="logout-dialog"
      append-to-body
    >
      <div class="logout-content">
        <div class="logout-icon">
          <el-icon :size="48"><WarningFilled /></el-icon>
        </div>
        <h3 class="logout-title">退出登录</h3>
        <p class="logout-desc">确定要退出登录吗？</p>
        <div class="logout-actions">
          <el-button size="large" class="btn-cancel" @click="cancelLogout">取消</el-button>
          <el-button size="large" type="danger" class="btn-confirm" @click="confirmLogout">退出</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 $spacing-medium;
}
.header-left {
  display: flex;
  align-items: center;
  gap: $spacing-small;
}
.header-title {
  font-size: $font-size-title;
  font-weight: 500;
  margin-left: $spacing-small;
}
.header-right {
  display: flex;
  align-items: center;
  gap: $spacing-base;
}
.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-small;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: $border-radius-base;
  &:hover {
    background: var(--bg-base);
  }
}
.user-name {
  font-size: $font-size-base;
}
.user-role {
  font-size: $font-size-small;
  padding: 3px 10px;
  background: linear-gradient(135deg, $gold-color 0%, $gold-color-light 100%);
  color: $primary-color;
  border-radius: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 6px rgba(255, 215, 0, 0.35);
  border: 1px solid color-mix(in srgb, $gold-color 60%, transparent);
  transition: all 0.2s ease;
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 10px rgba(255, 215, 0, 0.5);
  }
  // 普通成员用薄荷青
  &.role-staff {
    background: linear-gradient(135deg, $accent-color 0%, $accent-color-light 100%);
    color: $primary-color;
    box-shadow: 0 2px 6px rgba(0, 212, 170, 0.3);
  }
  &.role-member {
    background: color-mix(in srgb, $primary-color 10%, transparent);
    color: $primary-color;
    box-shadow: none;
    border-color: color-mix(in srgb, $primary-color 20%, transparent);
  }
  &.role-newbie {
    background: var(--bg-base);
    color: var(--text-secondary);
    box-shadow: none;
    border-color: var(--border-light);
  }
}

// 响应式：按平台显示不同按钮
.desktop-only {
  @include mobile { display: none; }
}
.mobile-only {
  @include desktop { display: none; }
}

// 移动端隐藏用户角色标签（节省空间）
.user-role {
  @include mobile { display: none; }
}

/* ========== 退出登录对话框 - scoped 部分需穿透，核心样式放全局 ========== */
</style>
<!-- 全局非 scoped，确保 ElDialog 子组件生效 -->
<style lang="scss">
/* 退出登录遮罩（干净深色，无磨砂） */
.dark-modal {
  background: rgba(10, 37, 64, 0.55) !important;
}

/* 退出登录对话框卡片（不磨砂） */
.logout-dialog {
  background: #ffffff !important;
  border-radius: 16px !important;
  border: none !important;
  box-shadow: 0 20px 60px rgba(10, 37, 64, 0.25), 0 4px 12px rgba(10, 37, 64, 0.12) !important;
  padding: 0 !important;

  html.dark & {
    background: #15233D !important;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5), 0 4px 12px rgba(0, 0, 0, 0.25) !important;
  }

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }
}

.logout-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 32px 32px;
}

.logout-icon {
  color: #e6a23c;
  margin-bottom: 16px;
  animation: logoutPulse 2s ease-in-out infinite;
}

@keyframes logoutPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.08); opacity: 0.85; }
}

.logout-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;

  html.dark & {
    color: #e5e7eb;
  }
}

.logout-desc {
  margin: 0 0 28px;
  font-size: 15px;
  color: var(--text-secondary);
}

.logout-actions {
  display: flex;
  gap: 16px;
  width: 100%;
  justify-content: center;

  .btn-cancel {
    width: 120px;
    border-radius: 10px;
    background: var(--bg-base);
    border: 1px solid var(--border-light);

    html.dark & {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.12);
      color: #c7c7cc;
    }

    &:hover {
      background: rgba(0, 0, 0, 0.04);

      html.dark & {
        background: rgba(255, 255, 255, 0.12);
      }
    }
  }

  .btn-confirm {
    width: 120px;
    border-radius: 10px;
    font-weight: 500;
  }
}
</style>