<script setup lang="ts">
/**
 * 首页 Dashboard（A 负责）
 * - 社长/技术骨干：保留原表格 + 管理入口
 * - 普通社员/新成员：无 sidebar 友好中枢
 *   - 顶部 HeroBanner 欢迎
 *   - 中部"今日速览"统计
 *   - 下方 6 大快捷入口卡片（替代 sidebar 导航）
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMemberStyle } from '@/composables/useMemberStyle'
import {
  Printer, Bell, Operation, Folder, Picture, User, Document,
  Plus, ArrowRight, Box, DataLine, TrendCharts,
} from '@element-plus/icons-vue'
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

// 成员端 6 大快捷入口
const quickEntries = [
  {
    icon: Printer, title: '申请打印任务', desc: '上传 STL，一键打印',
    color: '#00A88A', gradient: 'linear-gradient(135deg, #00A88A 0%, #4FE5C7 100%)',
    to: '/task/apply', badge: '热门',
  },
  {
    icon: Bell, title: '我的任务', desc: '查看申请进度和状态',
    color: '#0A2540', gradient: 'linear-gradient(135deg, #0A2540 0%, #1E3A5F 100%)',
    to: '/task/my', badge: '',
  },
  {
    icon: Operation, title: '打印队列', desc: '看看当前在打印什么',
    color: '#6366F1', gradient: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
    to: '/task/queue', badge: '',
  },
  {
    icon: Folder, title: '项目中心', desc: '浏览和参与社团项目',
    color: '#F2A93B', gradient: 'linear-gradient(135deg, #F2A93B 0%, #FFA500 100%)',
    to: '/project/list', badge: '',
  },
  {
    icon: Picture, title: '作品库', desc: '看看大佬们的得意之作',
    color: '#DC2626', gradient: 'linear-gradient(135deg, #DC2626 0%, #F472B6 100%)',
    to: '/artwork/list', badge: '',
  },
  {
    icon: User, title: '我的作品', desc: '管理你登记过的所有作品',
    color: '#0EA5E9', gradient: 'linear-gradient(135deg, #0EA5E9 0%, #38BDF8 100%)',
    to: '/artwork/my', badge: '',
  },
]

// 今日速览（mock 数据，后期接 API）
const stats = ref([
  { key: 'myTasks', label: '我的任务', value: 0, icon: Bell, color: '#0A2540' },
  { key: 'pending', label: '待处理', value: 0, icon: Printer, color: '#F2A93B' },
  { key: 'projects', label: '参与项目', value: 0, icon: Folder, color: '#00A88A' },
  { key: 'artworks', label: '我的作品', value: 0, icon: Picture, color: '#DC2626' },
])
</script>

<template>
  <div class="home-page">
    <!-- ============= 成员端：中枢布局 ============= -->
    <template v-if="isMember">
      <!-- Hero 欢迎区 -->
      <HeroBanner
        :title="`${getGreeting()}，${userName} 👋`"
        subtitle="欢迎使用 3D 打印科创会管理系统 · 愿你今天的创意都能落地"
        illustration="hero-home"
        :is-newbie="isNewbie"
        newbie-tip="作为新成员，先点下面任意一个入口逛逛，打印任务和作品库最适合新手熟悉。"
      />

      <!-- 今日速览（4 个统计小卡）-->
      <div class="stats-row">
        <div v-for="s in stats" :key="s.key" class="stat-card">
          <div class="stat-icon" :style="{ background: s.color + '20', color: s.color }">
            <el-icon :size="22"><component :is="s.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ s.value }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </div>

      <!-- 6 大快捷入口 -->
      <div class="quick-entries-section">
        <h2 class="section-title">快捷入口</h2>
        <p class="section-subtitle">点一个开始用</p>
        <div class="quick-entries-grid">
          <div
            v-for="entry in quickEntries"
            :key="entry.to"
            class="entry-card"
            :style="{ '--card-gradient': entry.gradient }"
            @click="router.push(entry.to)"
          >
            <div v-if="entry.badge" class="entry-badge">{{ entry.badge }}</div>
            <div class="entry-icon" :style="{ background: entry.color + '20', color: entry.color }">
              <el-icon :size="32"><component :is="entry.icon" /></el-icon>
            </div>
            <h3 class="entry-title">{{ entry.title }}</h3>
            <p class="entry-desc">{{ entry.desc }}</p>
            <div class="entry-arrow">
              <el-icon :size="20"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部小提示 -->
      <div class="footer-tip">
        <el-icon :size="14"><Document /></el-icon>
        <span>需要帮助？联系社长或技术骨干。点击右上角头像可进入个人中心。</span>
      </div>
    </template>

    <!-- ============= 后台端：保留原布局 ============= -->
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

      <el-card class="todo-card">
        <template #header>
          <span>📌 我的待办</span>
        </template>
        <EmptyState description="暂无待办" hint="所有任务都处理完了，可以去打印区转转~" />
      </el-card>

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
    </template>
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
  padding: $spacing-large;
  max-width: 1200px;
  margin: 0 auto;

  @include mobile {
    padding: $spacing-medium;
    gap: $spacing-medium;
  }
}

// ============ 成员端 ============
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-medium;

  @include mobile {
    grid-template-columns: repeat(2, 1fr);
  }
}
.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-medium;
  padding: 18px 22px;
  background: var(--bg-card);
  border: 1px solid var(--border-extra-light);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(10, 37, 64, 0.04);
  transition: transform 0.25s, box-shadow 0.25s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(10, 37, 64, 0.1);
  }
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: var(--text-primary);
  font-family: 'Consolas', monospace;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.quick-entries-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-small;
}
.section-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}
.section-subtitle {
  margin: 0 0 $spacing-base;
  font-size: 14px;
  color: var(--text-secondary);
}

.quick-entries-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-medium;

  @include mobile {
    grid-template-columns: repeat(2, 1fr);
  }
}
.entry-card {
  position: relative;
  padding: 28px 24px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-extra-light);
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(10, 37, 64, 0.05);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;

  // 顶部装饰条
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: var(--card-gradient);
    opacity: 0.85;
  }

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 16px 32px rgba(10, 37, 64, 0.15);
    border-color: transparent;

    .entry-arrow {
      transform: translateX(4px);
      color: var(--text-primary);
    }
  }
}
.entry-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 3px 10px;
  background: linear-gradient(135deg, #F2A93B 0%, #CCB000 100%);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(242, 169, 59, 0.4);
  z-index: 1;
}
.entry-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: $spacing-mini;
}
.entry-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.2px;
}
.entry-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  flex: 1;
}
.entry-arrow {
  position: absolute;
  bottom: 20px;
  right: 20px;
  color: var(--text-placeholder);
  transition: all 0.25s;
}

.footer-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  background: var(--bg-card);
  border: 1px solid var(--border-extra-light);
  border-radius: 12px;
  font-size: 13px;
  color: var(--text-secondary);
  text-align: center;
  justify-content: center;

  .el-icon { color: var(--accent-color); }
}

// ============ 后台端（保留原样）============
.welcome-card {
  background: linear-gradient(135deg, $brand-color 0%, #42a5f5 100%) !important;
  color: #fff;
  :deep(.el-card__body) { padding: $spacing-large; }
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
}
.quick-actions { margin: 0 !important; }
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
