<script setup lang="ts">
/**
 * 我的任务列表（**B** - 社员端）
 * v2.3 重构：表格 -> 卡片化时间线
 */
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Clock, Promotion, Check, CircleClose, Right } from '@element-plus/icons-vue'
import MemberCard from '@/components/member/MemberCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import HeroBanner from '@/components/member/HeroBanner.vue'
import { useTaskStore } from '@/stores/task'
import { useMemberStyle } from '@/composables/useMemberStyle'
import { TaskStatus, TaskStatusText, Priority, type PrintTask } from '@/types/task'
import { formatRelativeTime } from '@/utils/format'

const router = useRouter()
const taskStore = useTaskStore()
const { isMember, isNewbie } = useMemberStyle()

const filterStatus = ref<number | undefined>(undefined)

const taskList = computed<PrintTask[]>(() => taskStore.myTasks?.list ?? [])

const taskGroups = computed(() => {
  const groups: Record<string, PrintTask[]> = {
    '待审批': [],
    '已通过（待分配）': [],
    '排队/打印中': [],
    '已完成（待取件）': [],
    '已取件/已签收': [],
    '已驳回/已取消': [],
  }
  taskList.value.forEach((t) => {
    switch (t.status) {
      case TaskStatus.PENDING: groups['待审批']!.push(t); break
      case TaskStatus.APPROVED: groups['已通过（待分配）']!.push(t); break
      case TaskStatus.QUEUED:
      case TaskStatus.PRINTING: groups['排队/打印中']!.push(t); break
      case TaskStatus.DONE: groups['已完成（待取件）']!.push(t); break
      case TaskStatus.PICKED_UP: groups['已取件/已签收']!.push(t); break
      case TaskStatus.CANCELLED:
      case TaskStatus.REJECTED: groups['已驳回/已取消']!.push(t); break
    }
  })
  return Object.entries(groups).filter(([, items]) => (items ?? []).length > 0)
})

// 状态色配置
const statusMeta: Record<number, { color: string; bgColor: string; icon: any }> = {
  [TaskStatus.PENDING]: { color: '#909399', bgColor: '#F4F4F5', icon: Clock },
  [TaskStatus.APPROVED]: { color: '#00A88A', bgColor: '#E0FAF4', icon: Check },
  [TaskStatus.REJECTED]: { color: '#DC2626', bgColor: '#FEE2E2', icon: CircleClose },
  [TaskStatus.QUEUED]: { color: '#F2A93B', bgColor: '#FFF9D6', icon: Clock },
  [TaskStatus.PRINTING]: { color: '#1E3A5F', bgColor: '#E8EEF5', icon: Promotion },
  [TaskStatus.DONE]: { color: '#00A88A', bgColor: '#E0FAF4', icon: Check },
  [TaskStatus.CANCELLED]: { color: '#9AA8BC', bgColor: '#F6F9FC', icon: CircleClose },
  [TaskStatus.PICKED_UP]: { color: '#00A88A', bgColor: '#E0FAF4', icon: Check },
}

const statusTagType = (status: number) => {
  if (status === TaskStatus.APPROVED || status === TaskStatus.DONE || status === TaskStatus.PICKED_UP) return 'success' as const
  if (status === TaskStatus.REJECTED || status === TaskStatus.CANCELLED) return 'danger' as const
  if (status === TaskStatus.PRINTING) return 'primary' as const
  return 'info' as const
}

const pendingCount = computed(() =>
  taskList.value.filter((t) => t.status === TaskStatus.PENDING || t.status === TaskStatus.APPROVED).length
)
const printingCount = computed(() =>
  taskList.value.filter((t) => t.status === TaskStatus.QUEUED || t.status === TaskStatus.PRINTING).length
)

const fetchData = async () => {
  await taskStore.fetchMyTasks({ page: 1, size: 50, status: filterStatus.value })
}

onMounted(fetchData)
</script>

<template>
  <div class="task-my-page">
    <!-- 成员端 HeroBanner -->
    <HeroBanner
      v-if="isMember"
      title="我的任务"
      :subtitle="`待处理 ${pendingCount} 个 · 打印中 ${printingCount} 个`"
      :is-newbie="isNewbie"
      newbie-tip="提交任务后，在这里查看审批进度、打印队列、取件提醒。"
    >
      <template #actions>
        <el-button type="primary" size="large" round @click="router.push('/task/apply')">
          <el-icon><Plus /></el-icon> 提交新任务
        </el-button>
      </template>
    </HeroBanner>

    <!-- 后台端 PageHeader -->
    <div v-else class="admin-header">
      <h2 class="page-title">我的任务</h2>
      <div class="header-actions">
        <el-button type="primary" @click="router.push('/task/apply')">
          <el-icon><Plus /></el-icon> 提交新任务
        </el-button>
        <el-button @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 成员端：分组卡片 -->
    <template v-if="isMember">
      <div v-if="taskGroups.length === 0">
        <MemberCard padding="40px">
          <EmptyState
            description="还没有任务记录"
            hint="提交第一个打印任务，让你的创意变成实体"
          >
            <el-button type="primary" round size="large" @click="router.push('/task/apply')">
              立即提交任务
            </el-button>
          </EmptyState>
        </MemberCard>
      </div>

      <div v-for="[groupName, items] in taskGroups" :key="groupName" class="task-section">
        <h3 class="section-title">
          <span class="title-text">{{ groupName }}</span>
          <span class="title-count">{{ items.length }}</span>
        </h3>
        <el-row :gutter="16">
          <el-col v-for="task in items" :key="task.taskId" :xs="24" :sm="12" :md="8" :lg="8" :xl="6">
            <MemberCard hoverable padding="20px" @click="router.push(`/task/${task.taskId}`)">
              <template #header>
                <div class="task-card-header">
                  <div class="task-status-badge" :style="{ background: statusMeta[task.status]?.bgColor, color: statusMeta[task.status]?.color }">
                    <el-icon :size="14"><component :is="statusMeta[task.status]?.icon || Clock" /></el-icon>
                    <span>{{ TaskStatusText[task.status] }}</span>
                  </div>
                  <el-tag v-if="task.priority === Priority.URGENT" type="danger" size="small" effect="dark">紧急</el-tag>
                  <el-tag v-else-if="task.priority === Priority.LOW" size="small" effect="dark">低优</el-tag>
                </div>
              </template>
              <h4 class="task-title">{{ task.title }}</h4>
              <div class="task-meta">
                <span class="meta-item">
                  <span class="meta-label">编号:</span>
                  <span class="meta-value">{{ task.taskId.slice(-8) }}</span>
                </span>
                <span v-if="task.materialType" class="meta-item">
                  <span class="meta-label">材料:</span>
                  <span class="meta-value">{{ task.materialType }}</span>
                </span>
                <span v-if="task.color" class="meta-item">
                  <span class="meta-label">颜色:</span>
                  <span class="meta-value color-dot" :style="{ background: task.color }"></span>
                </span>
              </div>
              <template #footer>
                <div class="task-footer">
                  <span class="task-time">{{ formatRelativeTime(task.applyTime) }}</span>
                  <el-button text type="primary" size="small" @click.stop="router.push(`/task/${task.taskId}`)">
                    详情 <el-icon><Right /></el-icon>
                  </el-button>
                </div>
              </template>
            </MemberCard>
          </el-col>
        </el-row>
      </div>
    </template>

    <!-- 后台端：保留原表格 -->
    <template v-else>
      <el-card v-loading="taskStore.loading">
        <template v-if="taskGroups.length === 0">
          <EmptyState description="还没有任务，去提交第一个吧～">
            <el-button type="primary" @click="router.push('/task/apply')">提交打印任务</el-button>
          </EmptyState>
        </template>
        <template v-else>
          <div v-for="[groupName, items] in taskGroups" :key="groupName" class="task-group">
            <h4 class="group-title">{{ groupName }} ({{ items.length }})</h4>
            <el-table :data="items" stripe @row-click="(row: any) => router.push(`/task/${row.taskId}`)">
              <el-table-column prop="taskId" label="任务编号" width="180" />
              <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusTagType(row.status)">
                    {{ TaskStatusText[row.status as keyof typeof TaskStatusText] }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="materialType" label="材料" width="80" />
              <el-table-column prop="color" label="颜色" width="80" />
              <el-table-column label="申请时间" width="160">
                <template #default="{ row }">
                  {{ formatRelativeTime(row.applyTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-card>
    </template>
  </div>
</template>

<script lang="ts">
export default { name: 'TaskMyPage' }
</script>

<style lang="scss" scoped>
.task-my-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-large;
}

// ============ 后台端样式 ============
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-medium 0;
  border-bottom: 1px solid var(--border-light);
}
.page-title {
  margin: 0;
  font-size: 22px;
  color: var(--text-primary);
}
.task-group {
  margin-bottom: $spacing-large;
  &:last-child { margin-bottom: 0; }
}
.group-title {
  margin: 0 0 $spacing-small;
  padding: $spacing-small $spacing-base;
  background: var(--bg-base);
  border-left: 3px solid $brand-color;
  font-size: $font-size-base;
  color: var(--text-regular);
  // 暗色下换亮色（深海蓝在深色页面上看不见）
  html.dark & {
    border-left-color: $accent-color-light;
  }
}

// ============ 成员端样式 ============
.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  .title-count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 24px;
    height: 24px;
    padding: 0 8px;
    background: linear-gradient(135deg, #0A2540 0%, #00A88A 100%);
    color: #fff;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 700;
    // 暗色下用稍亮的深蓝起始
    html.dark & {
      background: linear-gradient(135deg, #1A3F5F 0%, #00A88A 100%);
    }
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
  }
}
.task-section {
  margin: 0 !important;
}

.task-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}
.task-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  :deep(.el-icon) { vertical-align: middle; }
}
.task-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
  font-size: 13px;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.meta-label { color: var(--text-secondary); }
.meta-value { color: var(--text-regular); font-weight: 500; }
.color-dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid var(--border-base);
  vertical-align: middle;
}
.task-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.task-time {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
