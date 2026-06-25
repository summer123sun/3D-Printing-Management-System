<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Fold, Expand, Moon, Sunny, SwitchButton, User } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { RoleText } from '@/types/member'

const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
    authStore.logout()
    ElMessageBox.close()
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<template>
  <div class="header-container">
    <div class="header-left">
      <el-button text @click="appStore.toggleSidebar()">
        <el-icon :size="20">
          <component :is="appStore.sidebarCollapsed ? Expand : Fold" />
        </el-icon>
      </el-button>
      <span class="header-title">{{ $route.meta?.title || '' }}</span>
    </div>

    <div class="header-right">
      <el-tooltip content="切换主题">
        <el-button text @click="appStore.toggleDark()">
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
          <span v-if="authStore.user" class="user-role">
            {{ RoleText[authStore.user.role] }}
          </span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/profile')">
              <el-icon><User /></el-icon> 个人中心
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleLogout">
              <el-icon><SwitchButton /></el-icon> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
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
    background: $bg-base;
  }
}
.user-name {
  font-size: $font-size-base;
}
.user-role {
  font-size: $font-size-small;
  color: $text-secondary;
  padding: 2px 6px;
  background: $brand-color-light;
  color: $brand-color;
  border-radius: $border-radius-small;
}
</style>