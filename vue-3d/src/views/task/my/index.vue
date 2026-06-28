<script setup lang="ts">
/**
 * 我的任务列表（**B** - 社员端）
 */
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useTaskStore } from '@/stores/task'
import { TaskStatus, TaskStatusText, type PrintTask } from '@/types/task'
import { formatRelativeTime } from '@/utils/format'

const router = useRouter()
const taskStore = useTaskStore()

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
      case TaskStatus.PENDING:
        groups['待审批']!.push(t); break
      case TaskStatus.APPROVED:
        groups['已通过（待分配）']!.push(t); break
      case TaskStatus.QUEUED:
      case TaskStatus.PRINTING:
        groups['排队/打印中']!.push(t); break
      case TaskStatus.DONE:
        groups['已完成（待取件）']!.push(t); break
      case TaskStatus.PICKED_UP:
        groups['已取件/已签收']!.push(t); break
      case TaskStatus.CANCELLED:
      case TaskStatus.REJECTED:
        groups['已驳回/已取消']!.push(t); break
    }
  })
  return Object.entries(groups).filter(([, items]) => (items ?? []).length > 0)
})

const fetchData = async () => {
  await taskStore.fetchMyTasks({ page: 1, size: 50, status: filterStatus.value })
}

onMounted(fetchData)
</script>

<template>
  <div class="task-my-page">
    <PageHeader title="我的任务">
      <el-button type="primary" @click="router.push('/task/apply')">
        <el-icon><Plus /></el-icon> 提交新任务
      </el-button>
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

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
                <StatusTag status-type="task" :status="row.status" />
              </template>
            </el-table-column>
            <el-table-column label="优先级" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.priority === 1" type="danger" size="small">紧急</el-tag>
                <el-tag v-else-if="row.priority === 3" size="small">低优</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="materialType" label="材料" width="80" />
            <el-table-column prop="color" label="颜色" width="80" />
            <el-table-column label="申请时间" width="160">
              <template #default="{ row }">
                {{ formatRelativeTime(row.applyTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" @click.stop="router.push(`/task/${row.taskId}`)">详情</el-button>
                <el-button
                  v-if="row.status === TaskStatus.DONE"
                  text
                  type="success"
                  @click.stop="router.push({ path: '/artwork/create', query: { taskId: row.taskId } })"
                >
                  登记作品
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Plus } from '@element-plus/icons-vue'
export default { name: 'TaskMyPage' }
</script>

<style lang="scss" scoped>
.task-my-page {
  padding: 0;
}
.task-group {
  margin-bottom: $spacing-large;
  &:last-child {
    margin-bottom: 0;
  }
}
.group-title {
  margin: 0 0 $spacing-small;
  padding: $spacing-small $spacing-base;
  background: var(--bg-base);
  border-left: 3px solid $brand-color;
  font-size: $font-size-base;
  color: var(--text-regular);
}
</style>