<script setup lang="ts">
/**
 * 打印队列（**B** - 社员端，所有人都能看）
 *
 * 按 priority → apply_time 排序展示
 */
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useTaskStore } from '@/stores/task'
import { formatRelativeTime } from '@/utils/format'

const router = useRouter()
const taskStore = useTaskStore()

onMounted(async () => {
  await taskStore.fetchQueue({ page: 1, size: 50 })
})
</script>

<template>
  <div class="task-queue-page">
    <PageHeader title="打印队列" />

    <el-card v-loading="taskStore.loading">
      <template v-if="!taskStore.queue || taskStore.queue.list.length === 0">
        <EmptyState description="队列里没有任务" />
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
              <el-tag v-if="row.priority === 1" type="danger" size="small">紧急</el-tag>
              <el-tag v-else-if="row.priority === 3" size="small">低优</el-tag>
              <el-tag v-else size="small">普通</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="applicantName" label="申请人" width="100" />
          <el-table-column prop="materialType" label="材料" width="80" />
          <el-table-column prop="color" label="颜色" width="80" />
          <el-table-column prop="printerId" label="打印机" width="100" />
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
  padding: 0;
}
</style>