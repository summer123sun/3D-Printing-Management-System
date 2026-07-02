<script setup lang="ts">
/**
 * 首页 Dashboard
 * - 社长/技术骨干 (role 1/2): 后台管理风（原样保留）
 * - 普通社员/新成员 (role 3/4): 博客风中枢
 *   - 全屏背景图（layout 注入）
 *   - 居中 hero 文案（标题 + 副标题 + 新成员 tip）
 *   - 白色玻璃质感卡片：4 统计 + 6 快捷入口
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMemberStyle } from '@/composables/useMemberStyle'
import {
  Printer, Bell, Operation, Folder, Picture, User, ArrowRight, ArrowDown,
} from '@element-plus/icons-vue'
import EmptyState from '@/components/common/EmptyState.vue'

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
    icon: Printer, title: '申请打印任务', desc: '上传 STL · 一键打印',
    color: '#00A88A', to: '/task/apply', badge: '热门',
  },
  {
    icon: Bell, title: '我的任务', desc: '查看申请进度和状态',
    color: '#0A2540', to: '/task/my',
  },
  {
    icon: Operation, title: '打印队列', desc: '看看当前在打印什么',
    color: '#6366F1', to: '/task/queue',
  },
  {
    icon: Folder, title: '项目中心', desc: '浏览和参与社团项目',
    color: '#F2A93B', to: '/project/list',
  },
  {
    icon: Picture, title: '作品库', desc: '看看大佬们的得意之作',
    color: '#DC2626', to: '/artwork/list',
  },
  {
    icon: User, title: '我的作品', desc: '管理你登记过的所有作品',
    color: '#0EA5E9', to: '/artwork/my',
  },
]

// 今日速览（mock）
const stats = ref([
  { key: 'myTasks', label: '我的任务', value: 0, icon: Bell, color: '#0A2540' },
  { key: 'pending', label: '待处理', value: 0, icon: Printer, color: '#F2A93B' },
  { key: 'projects', label: '参与项目', value: 0, icon: Folder, color: '#00A88A' },
  { key: 'artworks', label: '我的作品', value: 0, icon: Picture, color: '#DC2626' },
])

// 滚动到内容区
const scrollToContent = () => {
  const el = document.querySelector('.home-content')
  el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
</script>

<template>
  <div class="home-page">
    <!-- ============= 成员端：博客风中枢 ============= -->
    <template v-if="isMember">
      <!-- 顶部居中 hero：标题 + 副标题 + 滚动提示 -->
      <section class="hero-section">
        <div class="hero-content">
          <div v-if="isNewbie" class="hero-badge">✨ 新成员专属</div>
          <h1 class="hero-title">{{ getGreeting() }}，{{ userName }} 👋</h1>
          <p class="hero-subtitle">欢迎使用 3D 打印科创会管理系统</p>
          <p v-if="isNewbie" class="hero-tip">
            💡 作为新成员，先点下面任意一个入口逛逛，打印任务和作品库最适合新手熟悉。
          </p>
          <button class="hero-scroll-btn" @click="scrollToContent">
            <span>开始使用</span>
            <el-icon :size="18"><ArrowDown /></el-icon>
          </button>
        </div>
      </section>

      <!-- 内容区：4 统计 + 6 快捷入口 -->
      <section class="home-content">
        <div class="stats-row">
          <div v-for="s in stats" :key="s.key" class="stat-card">
            <div class="stat-icon" :style="{ background: s.color + '18', color: s.color }">
              <el-icon :size="24"><component :is="s.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ s.value }}</div>
              <div class="stat-label">{{ s.label }}</div>
            </div>
          </div>
        </div>

        <div class="quick-entries-section">
          <h2 class="section-title">快捷入口</h2>
          <p class="section-subtitle">点一个开始用</p>
          <div class="quick-entries-grid">
            <div
              v-for="entry in quickEntries"
              :key="entry.to"
              class="entry-card clickable"
              @click="router.push(entry.to)"
            >
              <div v-if="entry.badge" class="entry-badge">{{ entry.badge }}</div>
              <div class="entry-icon" :style="{ background: entry.color + '18', color: entry.color }">
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
      </section>
    </template>

    <!-- ============= 后台端：保留原布局 ============= -->
    <template v-else>
      <el-card class="welcome-card">
        <h2 class="welcome-title">{{ getGreeting() }}，{{ userName }} 👋</h2>
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
        <template #header><span>📌 我的待办</span></template>
        <EmptyState description="暂无待办" hint="所有任务都处理完了，可以去打印区转转~" />
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
}

// ============ 成员端 ============
.hero-section {
  min-height: calc(100vh - #{$header-height} - 80px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px $spacing-large 40px;
  text-align: center;
  position: relative;
}
.hero-content {
  max-width: 720px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  animation: hero-fade-up 0.8s cubic-bezier(0.16, 1, 0.3, 1) both;
}
@keyframes hero-fade-up {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 16px;
  background: rgba(255, 215, 0, 0.22);
  border: 1px solid rgba(255, 215, 0, 0.45);
  color: #FFE89A;
  font-size: 13px;
  font-weight: 500;
  border-radius: 999px;
  backdrop-filter: blur(8px);
}
.hero-title {
  margin: 0;
  font-size: 56px;
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -1.5px;
  color: #FFFFFF;
  text-shadow: 0 4px 20px rgba(10, 37, 64, 0.4);
}
.hero-subtitle {
  margin: 0;
  font-size: 18px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.92);
  text-shadow: 0 2px 8px rgba(10, 37, 64, 0.35);
  font-weight: 300;
}
.hero-tip {
  margin: $spacing-small 0 0;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.28);
  backdrop-filter: blur(10px);
  color: #FFFFFF;
  font-size: 14px;
  border-radius: 12px;
  max-width: 600px;
  line-height: 1.6;
}
.hero-scroll-btn {
  margin-top: $spacing-base;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: rgba(255, 255, 255, 0.95);
  color: #0A2540;
  font-size: 15px;
  font-weight: 600;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(10, 37, 64, 0.3);
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  animation: hero-bounce 2s ease-in-out infinite;
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 32px rgba(10, 37, 64, 0.4);
    background: #FFFFFF;
  }
  .el-icon {
    animation: arrow-down 1.4s ease-in-out infinite;
  }
}
@keyframes hero-bounce {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-4px); }
}
@keyframes arrow-down {
  0%, 100% { transform: translateY(0); opacity: 0.7; }
  50%      { transform: translateY(3px); opacity: 1; }
}

// 内容区
.home-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 $spacing-large $spacing-large * 2;
  display: flex;
  flex-direction: column;
  gap: $spacing-large;
  width: 100%;

  @include mobile {
    padding: 0 $spacing-medium $spacing-large;
    gap: $spacing-medium;
  }
}

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
  padding: 22px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(10, 37, 64, 0.15);
  transition: transform 0.25s, box-shadow 0.25s;
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 14px 32px rgba(10, 37, 64, 0.22);
  }
}
.stat-icon {
  width: 52px;
  height: 52px;
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
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
  color: #0A2540;
  font-family: 'Consolas', monospace;
}
.stat-label {
  font-size: 13px;
  color: #5C6B82;
}

.quick-entries-section {
  display: flex;
  flex-direction: column;
  gap: $spacing-small;
}
.section-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: -0.3px;
  text-shadow: 0 2px 8px rgba(10, 37, 64, 0.3);
}
.section-subtitle {
  margin: 0 0 $spacing-base;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 1px 4px rgba(10, 37, 64, 0.3);
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
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  box-shadow: 0 8px 28px rgba(10, 37, 64, 0.15);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 18px 40px rgba(10, 37, 64, 0.28);
    background: #FFFFFF;

    .entry-arrow {
      transform: translateX(4px);
      color: #0A2540;
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
  box-shadow: 0 2px 8px rgba(242, 169, 59, 0.5);
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
  color: #0A2540;
  letter-spacing: -0.2px;
}
.entry-desc {
  margin: 0;
  font-size: 13px;
  color: #5C6B82;
  line-height: 1.5;
  flex: 1;
}
.entry-arrow {
  position: absolute;
  bottom: 20px;
  right: 20px;
  color: #94A3B8;
  transition: all 0.25s;
}

// 移动端 hero 适配
@include mobile {
  .hero-section { min-height: 70vh; padding: 40px $spacing-medium 24px; }
  .hero-title { font-size: 36px; letter-spacing: -1px; }
  .hero-subtitle { font-size: 15px; }
  .entry-card { min-height: 170px; padding: 22px 18px 18px; }
  .entry-icon { width: 48px; height: 48px; }
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
</style>
