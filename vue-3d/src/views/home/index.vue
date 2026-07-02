<script setup lang="ts">
/**
 * 首页 Dashboard（A 负责）
 * 根据角色显示不同内容
 * - 社长/技术骨干：后台管理风（数据看板 + 待审批）
 * - 普通社员/新成员：友好插图风（HeroBanner + 快捷入口 + 待办）
 */
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMemberStyle } from '@/composables/useMemberStyle'
import { Printer, Folder, Picture, Bell, Promotion, TrendCharts, Box, DataLine } from '@element-plus/icons-vue'
import EmptyState from '@/components/common/EmptyState.vue'
import HeroBanner from '@/components/member/HeroBanner.vue'
import MemberCard from '@/components/member/MemberCard.vue'

const authStore = useAuthStore()
const router = useRouter()
const { isAdmin, isMember, isNewbie } = useMemberStyle()

onMounted(() => {
  if (authStore.token && !authStore.user) {
    authStore.fetchUserInfo().catch(() => {})
  }
})

function getGreeting(): string {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
}

const userName = computed(() => authStore.user?.name || '同学')

const todoList = [
  { type: 'pickup', text: '你有 2 个待取件任务', icon: Bell },
  { type: 'progress', text: '机械键盘定制项目进入批量打印阶段', icon: Promotion },
]

// 成员端快捷入口
const memberActions = [
  { icon: Printer, text: '提交打印任务', desc: '上传 STL 文件，一键打印', color: '#00A88A', to: '/task/apply' },
  { icon: Bell, text: '我的任务', desc: '查看申请进度和状态', color: '#1E3A5F', to: '/task/my' },
  { icon: Folder, text: '项目中心', desc: '浏览和参与社团项目', color: '#F2A93B', to: '/project/list' },
  { icon: Picture, text: '作品库', desc: '看看大佬们的作品', color: '#DC2626', to: '/artwork/list' },
]
</script>

<template>
  <div class="home-page">
    <!-- ============= 成员端 HeroBanner ============= -->
    <template v-if="isMember">
      <HeroBanner
        :title="`${getGreeting()}，${userName} 👋`"
        subtitle="欢迎使用 3D 打印科创会管理系统 · 愿你今天的创意都能落地"
        illustration="hero-home"
        :is-newbie="isNewbie"
        newbie-tip="作为新成员，先去逛逛打印任务和作品库吧！有任何问题找社长/技术骨干。"
      />
    </template>

    <!-- ============= 后台端欢迎卡 ============= -->
    <template v-else>
      <el-card class="welcome-card">
        <h2 class="welcome-title">
          {{ getGreeting() }}，{{ userName }} 👋
        </h2>
        <p class="welcome-subtitle">
          欢迎使用 3D 打印科创会管理系统
          <span class="admin-tag">管理员视图</span>
        </p>
      </el-card>
    </template>

    <!-- ============= 成员端快捷入口 ============= -->
    <template v-if="isMember">
      <h3 class="section-title">快捷入口</h3>
      <el-row :gutter="20" class="member-actions">
        <el-col v-for="action in memberActions" :key="action.to" :xs="12" :sm="12" :md="6">
          <MemberCard hoverable padding="20px" @click="router.push(action.to)">
            <template #header>
              <div class="action-header">
                <div class="action-icon" :style="{ background: action.color + '15', color: action.color }">
                  <el-icon :size="24"><component :is="action.icon" /></el-icon>
                </div>
                <span class="action-title">{{ action.text }}</span>
              </div>
            </template>
            <p class="action-desc">{{ action.desc }}</p>
          </MemberCard>
        </el-col>
      </el-row>
    </template>

    <!-- ============= 后台端快捷入口 ============= -->
    <template v-else>
      <el-row :gutter="16" class="quick-actions">
        <el-col :xs="12" :sm="8" :md="6">
          <el-card class="action-card" @click="router.push('/task/apply')">
            <el-icon :size="32" color="#1e88e5"><Printer /></el-icon>
            <p class="action-text">提交打印任务</p>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-card class="action-card" @click="router.push('/task/my')">
            <el-icon :size="32" color="#67c23a"><Bell /></el-icon>
            <p class="action-text">我的任务</p>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-card class="action-card" @click="router.push('/project/list')">
            <el-icon :size="32" color="#e6a23c"><Folder /></el-icon>
            <p class="action-text">浏览项目</p>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-card class="action-card" @click="router.push('/artwork/list')">
            <el-icon :size="32" color="#f56c6c"><Picture /></el-icon>
            <p class="action-text">作品库</p>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ============= 待办（两版共用，成员端用 MemberCard）============= -->
    <template v-if="isMember">
      <h3 class="section-title">📌 我的待办</h3>
      <MemberCard v-if="todoList.length > 0" padding="20px">
        <ul class="todo-list">
          <li v-for="(todo, i) in todoList" :key="i" class="todo-item">
            <div class="todo-icon">
              <el-icon :size="18"><component :is="todo.icon" /></el-icon>
            </div>
            <span class="todo-text">{{ todo.text }}</span>
          </li>
        </ul>
      </MemberCard>
      <MemberCard v-else padding="20px">
        <EmptyState description="暂无待办" hint="所有任务都处理完了，可以去打印区转转~" />
      </MemberCard>
    </template>

    <template v-else>
      <el-card class="todo-card">
        <template #header>
          <span>📌 我的待办</span>
        </template>
        <EmptyState v-if="todoList.length === 0" description="暂无待办" hint="所有任务都处理完了，可以去打印区转转~" />
        <ul v-else class="todo-list">
          <li v-for="(todo, i) in todoList" :key="i" class="todo-item">
            <el-icon><component :is="todo.icon" /></el-icon>
            <span>{{ todo.text }}</span>
          </li>
        </ul>
      </el-card>
    </template>

    <!-- ============= 后台端：管理入口 ============= -->
    <el-card v-if="isAdmin" class="admin-card">
      <template #header>
        <span>⚙️ 管理后台</span>
      </template>
      <el-space wrap>
        <el-button type="primary" @click="router.push('/admin/task/pending')">
          <el-icon><Bell /></el-icon> 待审批任务
        </el-button>
        <el-button @click="router.push('/admin/dashboard')">
          <el-icon><DataLine /></el-icon> 统计看板
        </el-button>
        <el-button @click="router.push('/admin/material')">
          <el-icon><Box /></el-icon> 耗材库存
        </el-button>
        <el-button @click="router.push('/admin/printer')">
          <el-icon><Printer /></el-icon> 打印机管理
        </el-button>
        <el-button @click="router.push('/admin/log')">
          <el-icon><TrendCharts /></el-icon> 系统日志
        </el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script lang="ts">
export default { name: 'HomePage' }
</script>

<style lang="scss" scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-large;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

// ============ 成员端样式 ============
.member-actions {
  margin: 0 !important;
}
.action-header {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.action-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.action-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}
.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed var(--border-extra-light);
  &:last-child { border-bottom: none; }
}
.todo-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #E8EEF5 0%, #F0F4F8 100%);
  color: $primary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.todo-text {
  font-size: 14px;
  color: var(--text-regular);
}

// ============ 后台端样式（保留原样）============
.welcome-card {
  background: linear-gradient(135deg, $brand-color 0%, #42a5f5 100%) !important;
  color: #fff;
  :deep(.el-card__body) { padding: $spacing-large; }
  :deep(.el-card__header),
  :deep(.el-card__body) { color: #fff !important; }
}
.welcome-title {
  margin: 0 0 $spacing-small;
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}
.welcome-subtitle {
  margin: 0;
  font-size: $font-size-base;
  opacity: 0.9;
  color: #fff;
}
.admin-tag {
  display: inline-block;
  margin-left: $spacing-small;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: $border-radius-base;
  font-size: $font-size-small;
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.3);
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
  :deep(.el-card__body) { padding: $spacing-large $spacing-base; }
}
.action-text {
  margin: $spacing-small 0 0;
  font-size: $font-size-base;
  color: var(--text-primary);
  font-weight: 500;
}
.admin-card :deep(.el-card__body) {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-small;
}
</style>
