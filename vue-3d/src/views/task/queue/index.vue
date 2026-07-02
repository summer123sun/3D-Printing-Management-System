<script setup lang="ts">
/**
 * 打印队列（**B** - 社员端，所有人都能看）
 * v2.3 重构：表格 -> 卡片化时间线
 */
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Clock, Promotion } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import HeroBanner from '@/components/member/HeroBanner.vue'
import MemberCard from '@/components/member/MemberCard.vue'
import { useTaskStore } from '@/stores/task'
import { useMemberStyle } from '@/composables/useMemberStyle'
import { Priority, TaskStatus, type PrintTask } from '@/types/task'
import { formatRelativeTime } from '@/utils/format'

const router = useRouter()
const taskStore = useTaskStore()
const { isMember } = useMemberStyle()

const queueList = computed<PrintTask[]>(() => taskStore.queue?.list ?? [])
const printingCount = computed(() => queueList.value.filter((t) => t.status === TaskStatus.PRINTING).length)
const queuedCount = computed(() => queueList.value.filter((t) => t.status === TaskStatus.QUEUED).length)

onMounted(async () => {
  await taskStore.fetchQueue({ page: 1, size: 50 })
})
</script>

<template>
  <div class="task-queue-page">
    <!-- 成员端 HeroBanner -->
    <HeroBanner
      v-if="isMember"
      title="打印队列"
      :subtitle="`排队中 ${queuedCount} 个 · 打印中 ${printingCount} 个`"
      illustration="hero-home"
    >
      <template #actions>
        <el-button type="primary" size="large" round @click="router.push('/task/apply')">
          <el-icon><Plus /></el-icon> 提交新任务
        </el-button>
      </template>
    </HeroBanner>

    <!-- 后台端 PageHeader -->
    <PageHeader v-else title="打印队列" />

    <MemberCard v-if="isMember" v-loading="taskStore.loading" padding="20px">
      <template v-if="!taskStore.queue || queueList.length === 0">
        <EmptyState
          illustration="empty-queue"
          description="队列里没有任务"
          hint="所有任务都已打印完成或等待中。提交一个任务试试？"
        >
          <el-button type="primary" round size="large" @click="router.push('/task/apply')">
            <el-icon><Plus /></el-icon> 提交打印任务
          </el-button>
        </EmptyState>
      </template>

      <el-row v-else :gutter="16">
        <el-col v-for="(task, idx) in queueList" :key="task.taskId" :xs="24" :sm="12" :md="8" :lg="6">
          <MemberCard hoverable padding="18px" @click="router.push(`/task/${task.taskId}`)">
            <template #header>
              <div class="queue-header">
                <div class="queue-position">#{{ idx + 1 }}</div>
                <StatusTag status-type="task" :status="task.status" />
              </div>
            </template>
            <h4 class="queue-title">{{ task.title }}</h4>
            <div class="queue-meta">
              <div class="meta-row">
                <span class="meta-label">申请人:</span>
                <el-tooltip :content="`学号: ${task.applicantId}`" placement="top">
                  <span class="meta-value">{{ task.applicantName || task.applicantId }}</span>
                </el-tooltip>
              </div>
              <div v-if="task.printerId" class="meta-row">
                <span class="meta-label">打印机:</span>
                <span class="meta-value">{{ task.printerModel || task.printerId }}</span>
              </div>
              <div v-if="task.priority === Priority.URGENT" class="meta-row">
                <el-tag type="danger" size="small" effect="dark">紧急</el-tag>
              </div>
            </div>
            <template #footer>
              <div class="queue-footer">
                <span class="queue-time">
                  <el-icon :size="12"><Clock /></el-icon>
                  {{ formatRelativeTime(task.applyTime) }}
                </span>
                <el-button text type="primary" size="small">
                  详情 <el-icon><Promotion /></el-icon>
                </el-button>
              </div>
            </template>
          </MemberCard>
        </el-col>
      </el-row>
    </MemberCard>

    <el-card v-else v-loading="taskStore.loading">
      <template v-if="!taskStore.queue || taskStore.queue.list.length === 0">
        <EmptyState
          description="队列里没有任务"
          hint="所有任务都已打印完成或等待中。提交一个任务试试？"
        >
          <el-button type="primary" @click="router.push('/task/apply')">
            <el-icon><Plus /></el-icon> 提交打印任务
          </el-button>
        </EmptyState>
      </template>
      <template v-else>
        <el-table
          :data="taskStore.queue.list"
          stripe
          @row-click="(row: any) => router.push(`/task/${row.taskId}`)"
        >
          <el-table-column type="index" label="#" width="60" />
          <el-table-column prop="taskId" label="任务编号" width="180" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <StatusTag status-type="task" :status="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="优先级" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.priority === Priority.URGENT" type="danger" size="small" effect="dark">紧急</el-tag>
              <el-tag v-else-if="row.priority === Priority.LOW" size="small" effect="dark">低优</el-tag>
              <el-tag v-else size="small" effect="dark">普通</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="110">
            <template #default="{ row }">
              <el-tooltip :content="`学号：${row.applicantId}`" placement="top">
                <span>{{ row.applicantName || row.applicantId }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="materialType" label="材料" width="80" />
          <el-table-column prop="color" label="颜色" width="80" />
          <el-table-column label="打印机" width="140">
            <template #default="{ row }">
              <el-tooltip v-if="row.printerId" :content="`编号：${row.printerId}`" placement="top">
                <el-tag size="small" effect="dark">{{ row.printerModel || row.printerId }}</el-tag>
              </el-tooltip>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="排队时间" width="160">
            <template #default="{ row }">
              {{ formatRelativeTime(row.applyTime) }}
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.task-queue-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-large;
}

.queue-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.queue-position {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 24px;
  padding: 0 8px;
  background: linear-gradient(135deg, #0A2540 0%, #00A88A 100%);
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  font-family: 'Consolas', monospace;
}
.queue-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.queue-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
  font-size: 13px;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.meta-label { color: var(--text-secondary); }
.meta-value { color: var(--text-regular); font-weight: 500; }
.queue-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.queue-time {
  font-size: 12px;
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
