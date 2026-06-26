<script setup lang="ts">
/**
 * 任务统计（**B** - 管理端看板）
 *
 * 简化版：4 张数据卡片 + 各状态分布
 */
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { taskStats } from '@/api/task'
import { TaskStatus, TaskStatusText, TaskStatusTagType } from '@/types/task'

const stats = ref<Record<string, number> | null>(null)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    stats.value = await taskStats()
  } finally {
    loading.value = false
  }
}
onMounted(fetchData)

const total = (key: number) => stats.value?.[`status_${key}`] ?? 0

const statusCards = [
  { status: TaskStatus.PENDING, label: '待审批' },
  { status: TaskStatus.QUEUED, label: '排队中' },
  { status: TaskStatus.PRINTING, label: '打印中' },
  { status: TaskStatus.DONE, label: '已完成' },
]
</script>

<template>
  <div class="admin-task-stats-page">
    <PageHeader title="任务统计">
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <div v-loading="loading">
      <!-- 数据卡 -->
      <el-row :gutter="16">
        <el-col v-for="c in statusCards" :key="c.status" :xs="12" :sm="6">
          <el-card class="stat-card" shadow="hover">
            <p class="stat-label">{{ c.label }}</p>
            <p class="stat-num">{{ total(c.status) }}</p>
          </el-card>
        </el-col>
      </el-row>

      <!-- 详细分布 -->
      <el-card style="margin-top: 16px">
        <template #header><span>各状态任务数</span></template>
        <el-table :data="Object.entries(TaskStatusText).map(([k, v]) => ({
          status: Number(k), label: v, count: stats?.[`status_${k}`] ?? 0
        }))" stripe>
          <el-table-column label="状态">
            <template #default="{ row }">
              <el-tag :type="TaskStatusTagType[row.status as keyof typeof TaskStatusTagType]" size="small" effect="light">
                {{ row.label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="数量" width="120" />
          <el-table-column label="占比">
            <template #default="{ row }">
              <el-progress
                :percentage="stats ? Math.round((row.count / Object.values(stats).reduce((a, b) => a + b, 0)) * 100) : 0"
                :stroke-width="14"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.admin-task-stats-page {
  padding: 0;
}
.stat-card {
  text-align: center;
  :deep(.el-card__body) {
    padding: $spacing-large $spacing-base;
  }
}
.stat-label {
  margin: 0;
  font-size: $font-size-base;
  color: $text-secondary;
}
.stat-num {
  margin: $spacing-small 0 0;
  font-size: 36px;
  font-weight: 600;
  color: $brand-color;
}
</style>