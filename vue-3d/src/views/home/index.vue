<script setup lang="ts">
/**
 * 首页 Dashboard（A 负责）
 * 根据角色显示不同内容
 */
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { Role } from '@/types/member'
import { Printer, Folder, Picture, Bell, Promotion } from '@element-plus/icons-vue'

const authStore = useAuthStore()

onMounted(() => {
  // 拉一次最新的用户信息（确保 token 没过期 + 数据最新）
  if (authStore.token && !authStore.user) {
    authStore.fetchUserInfo().catch(() => {})
  }
})

const isAdmin = computed(() => {
  const role = authStore.user?.role ?? Role.NEWBIE
  return role === Role.PRESIDENT || role === Role.TECH_LEAD
})

// 模拟的待办数据（实际开发时从后端拉）
const todoList = [
  { type: 'pickup', text: '你有 2 个待取件任务', icon: Bell },
  { type: 'progress', text: '机械键盘定制项目进入批量打印阶段', icon: Promotion },
]
</script>

<template>
  <div class="home-page">
    <el-card class="welcome-card">
      <h2 class="welcome-title">
        {{ getGreeting() }}，{{ authStore.user?.name || '同学' }} 👋
      </h2>
      <p class="welcome-subtitle">
        欢迎使用 3D 打印科创会管理系统
        <span v-if="isAdmin" class="admin-tag">管理员视图</span>
      </p>
    </el-card>

    <!-- 快捷入口（社员） -->
    <el-row :gutter="16" class="quick-actions">
      <el-col :xs="12" :sm="8" :md="6">
        <el-card class="action-card" @click="$router.push('/task/apply')">
          <el-icon :size="32" color="#1e88e5"><Printer /></el-icon>
          <p class="action-text">提交打印任务</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card class="action-card" @click="$router.push('/task/my')">
          <el-icon :size="32" color="#67c23a"><Bell /></el-icon>
          <p class="action-text">我的任务</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card class="action-card" @click="$router.push('/project/list')">
          <el-icon :size="32" color="#e6a23c"><Folder /></el-icon>
          <p class="action-text">浏览项目</p>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card class="action-card" @click="$router.push('/artwork/list')">
          <el-icon :size="32" color="#f56c6c"><Picture /></el-icon>
          <p class="action-text">作品库</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办 -->
    <el-card class="todo-card">
      <template #header>
        <span>📌 我的待办</span>
      </template>
      <el-empty v-if="todoList.length === 0" description="暂无待办" />
      <ul v-else class="todo-list">
        <li v-for="(todo, i) in todoList" :key="i" class="todo-item">
          <el-icon><component :is="todo.icon" /></el-icon>
          <span>{{ todo.text }}</span>
        </li>
      </ul>
    </el-card>

    <!-- 管理员视图入口 -->
    <el-card v-if="isAdmin" class="admin-card">
      <template #header>
        <span>⚙️ 管理后台</span>
      </template>
      <el-space wrap>
        <el-button type="primary" @click="$router.push('/admin/task/pending')">
          待审批任务
        </el-button>
        <el-button @click="$router.push('/admin/dashboard')">
          统计看板
        </el-button>
        <el-button @click="$router.push('/admin/material')">
          耗材库存
        </el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script lang="ts">
function getGreeting() {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
}
export default { name: 'HomePage' }
</script>

<style lang="scss" scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-medium;
}
.welcome-card {
  background: linear-gradient(135deg, $brand-color 0%, #42a5f5 100%);
  color: #fff;
  :deep(.el-card__body) {
    padding: $spacing-large;
  }
}
.welcome-title {
  margin: 0 0 $spacing-small;
  font-size: 24px;
  font-weight: 600;
}
.welcome-subtitle {
  margin: 0;
  font-size: $font-size-base;
  opacity: 0.9;
}
.admin-tag {
  display: inline-block;
  margin-left: $spacing-small;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: $border-radius-base;
  font-size: $font-size-small;
}
.quick-actions {
  margin: 0 !important;
}
.action-card {
  cursor: pointer;
  text-align: center;
  transition: all 0.2s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-light;
  }
  :deep(.el-card__body) {
    padding: $spacing-large $spacing-base;
  }
}
.action-text {
  margin: $spacing-small 0 0;
  font-size: $font-size-base;
  color: $text-primary;
}
.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.todo-item {
  display: flex;
  align-items: center;
  gap: $spacing-small;
  padding: $spacing-small 0;
  color: $text-regular;
}
</style>