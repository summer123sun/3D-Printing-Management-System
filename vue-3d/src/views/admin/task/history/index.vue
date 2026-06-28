<script setup lang="ts">
/**
 * 历史任务（**B** - 管理端）
 *
 * 显示状态 ≥ 5（已完成/已取消/已驳回）的任务
 * 用全部任务 + 状态筛选
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { get } from '@/utils/request'
import { TaskStatus, TaskStatusText } from '@/types/task'
import { formatDate, formatDuration } from '@/utils/format'
import type { PageResult } from '@/types/api'
import type { PrintTask } from '@/types/task'

const router = useRouter()

const list = ref<PageResult<PrintTask> | null>(null)
const loading = ref(false)
// v2 修复：默认查所有"已结束"任务（DONE + PICKED_UP + CANCELLED），
// 而不是只查 DONE。否则已取件的任务（DONE 之后还会走到 PICKED_UP）在历史页看不到
const ALL_FINISHED = '5,6,8'
const filter = ref({
  status: ALL_FINISHED as number | string,
  page: 1,
  size: 20,
})

const fetchData = async () => {
  loading.value = true
  try {
    // 用我的任务接口（管理端没有专门的"全部历史"接口，先简化）
    // 实际开发时可以加 /api/task/history 后端接口
    const res = await get<PageResult<PrintTask>>('/task/my', {
      page: filter.value.page,
      size: filter.value.size,
      status: filter.value.status,
    })
    list.value = res
  } finally {
    loading.value = false
  }
}
onMounted(fetchData)

// v2 修复：状态选项支持"全部已结束"（多状态逗号分隔字符串）
// 之前的 Object.entries 生成的是 number 单选，但 TaskServiceImpl.applyCommonFilters
// 现在支持"5,6,8" 这种字符串，所以下拉要混用
const statusOptions = [
  { value: ALL_FINISHED, label: '全部（已结束）' },
  ...Object.entries(TaskStatusText)
    .filter(([k]) => ['5', '6', '8'].includes(k))  // 只显示已结束的 3 个
    .map(([k, v]) => ({ value: Number(k), label: v })),
]

const resetFilter = () => {
  filter.value.status = ALL_FINISHED
  filter.value.page = 1
  fetchData()
}
</script>

<template>
  <div class="admin-task-history-page">
    <PageHeader title="历史任务">
      <el-select v-model="filter.status" placeholder="筛选状态" style="width: 140px" @change="fetchData">
        <el-option
          v-for="o in statusOptions"
          :key="o.value"
          :label="o.label"
          :value="o.value"
        />
      </el-select>
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <el-card v-loading="loading">
      <template v-if="!list || list.list.length === 0">
        <EmptyState
          description="没有符合条件的历史任务"
          hint="尝试调整筛选条件，或换个时间段看看。"
        >
          <el-button @click="resetFilter">重置筛选</el-button>
        </EmptyState>
      </template>

      <template v-else>
        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
          共 <b>{{ list.total }}</b> 条
        </el-alert>

        <el-table :data="list.list" stripe>
          <el-table-column prop="taskId" label="任务编号" width="180" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <StatusTag status-type="task" :status="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="110">
            <template #default="{ row }">
              <el-tooltip :content="`学号：${row.applicantId}`" placement="top">
                <span>{{ row.applicantName || row.applicantId }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="耗材" width="120">
            <template #default="{ row }">
              <span v-if="row.actualWeight">{{ row.actualWeight }}g</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="耗时" width="120">
            <template #default="{ row }">
              <span v-if="row.actualTime">{{ formatDuration(row.actualTime) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="qualityScore" label="评分" width="80">
            <template #default="{ row }">
              <el-rate v-model="row.qualityScore" disabled show-score :max="5" />
            </template>
          </el-table-column>
          <el-table-column label="完成时间" width="160">
            <template #default="{ row }">
              {{ formatDate(row.finishTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="router.push(`/task/${row.taskId}`)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="filter.page"
          v-model:page-size="filter.size"
          :total="list.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 16px; justify-content: flex-end"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </template>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.admin-task-history-page {
  padding: 0;
}
</style>