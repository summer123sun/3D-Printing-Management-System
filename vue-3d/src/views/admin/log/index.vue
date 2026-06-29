<script setup lang="ts">
/**
 * 系统日志（M6 - 审计：仅 ADMIN）
 */
import { onMounted, ref } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useLogStore } from '@/stores/log'
import { OperationText, TargetTypeText } from '@/types/log'
import { formatDateTime } from '@/utils/format'

const store = useLogStore()

const filter = ref({
  page: 1,
  size: 20,
  userId: '',
  operation: '',
  targetType: '',
  startTime: '',
  endTime: '',
})

const fetchData = async () => {
  await store.fetchList({
    page: filter.value.page,
    size: filter.value.size,
    userId: filter.value.userId || undefined,
    operation: filter.value.operation || undefined,
    targetType: filter.value.targetType || undefined,
    startTime: filter.value.startTime || undefined,
    endTime: filter.value.endTime || undefined,
  })
}

onMounted(fetchData)

const onSearch = () => {
  filter.value.page = 1
  fetchData()
}

const onPageChange = (page: number) => {
  filter.value.page = page
  fetchData()
}

const onReset = () => {
  filter.value = { page: 1, size: 20, userId: '', operation: '', targetType: '', startTime: '', endTime: '' }
  fetchData()
}

const handleClean = async () => {
  try {
    await ElMessageBox.confirm(
      '确定清理 90 天前的日志吗？此操作不可撤销。',
      '清理确认',
      {
        type: 'warning',
        center: true,
        confirmButtonText: '清理',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
      }
    )
  } catch {
    return
  }
  const n = await store.clean(90)
  ElNotification.success(`已清理 ${n} 条过期日志`)
  fetchData()
}

const getOperationName = (op: string) => {
  return OperationText[op] || op
}

const getTargetTypeName = (t: string | undefined) => {
  if (!t) return '-'
  return TargetTypeText[t] || t
}

const getOperationTagType = (op: string) => {
  if (op.includes('delete') || op.includes('cancel') || op.includes('reject')) return 'danger'
  if (op.includes('create') || op.includes('add') || op.includes('inbound')) return 'success'
  if (op.includes('update') || op.includes('edit') || op.includes('modify')) return 'warning'
  if (op.includes('login') || op.includes('logout')) return 'info'
  return 'primary'
}
</script>

<template>
  <div class="admin-log-page">
    <PageHeader title="系统日志">
      <el-input v-model="filter.userId" placeholder="操作人学号" clearable style="width: 140px" @keyup.enter="onSearch" @clear="onSearch" />
      <el-input v-model="filter.operation" placeholder="操作类型（如 task.approve）" clearable style="width: 200px" @keyup.enter="onSearch" @clear="onSearch" />
      <el-input v-model="filter.targetType" placeholder="对象类型（task/project/...）" clearable style="width: 160px" @keyup.enter="onSearch" @clear="onSearch" />
      <el-button type="primary" @click="onSearch">搜索</el-button>
      <el-button @click="onReset">重置</el-button>
      <el-button type="danger" plain @click="handleClean">
        <el-icon><Delete /></el-icon> 清理过期
      </el-button>
    </PageHeader>

    <el-card v-loading="store.loading">
      <EmptyState
        v-if="!store.list || store.list.list.length === 0"
        description="还没有日志记录"
        hint="用户的关键操作会自动记录到这里"
      />

      <el-table v-else :data="store.list.list" stripe>
        <el-table-column prop="logId" label="ID" width="80" />
        <el-table-column label="操作人" width="140">
          <template #default="{ row }">
            <div>{{ row.username || row.userId || '-' }}</div>
            <div style="font-size: 11px; color: var(--text-secondary); font-family: monospace">{{ row.userId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-tag :type="getOperationTagType(row.operation)" effect="dark">
              {{ getOperationName(row.operation) }}
            </el-tag>
            <div style="font-size: 11px; color: var(--text-secondary); font-family: monospace; margin-top: 2px">{{ row.operation }}</div>
          </template>
        </el-table-column>
        <el-table-column label="对象" width="120">
          <template #default="{ row }">
            <span>{{ getTargetTypeName(row.targetType) }}</span>
            <div v-if="row.targetId" style="font-size: 11px; color: var(--text-secondary); font-family: monospace">{{ row.targetId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP" width="140" />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="store.list && store.list.total > filter.size" class="pagination-wrap">
        <el-pagination
          :current-page="filter.page"
          :page-size="filter.size"
          :total="store.list.total"
          layout="prev, pager, next, total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.admin-log-page { padding: 0; }
.pagination-wrap {
  display: flex; justify-content: center; margin-top: $spacing-large;
}
</style>
