<script setup lang="ts">
/**
 * 耗材流水（M5）
 *
 * 全部入库/消耗记录，可按材料/颜色/操作类型筛选
 */
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useMaterialStore } from '@/stores/material'
import {
  MaterialTypes,
  MaterialTypeText,
  OperationType,
  OperationTypeText,
  OperationTypeTagType,
} from '@/types/material'
import { formatDateTime } from '@/utils/format'

const store = useMaterialStore()

const filter = ref({
  page: 1,
  size: 20,
  materialType: undefined as string | undefined,
  color: '',
  operationType: undefined as number | undefined,
})

const fetchData = async () => {
  await store.fetchLogs({
    page: filter.value.page,
    size: filter.value.size,
    materialType: filter.value.materialType,
    color: filter.value.color || undefined,
    operationType: filter.value.operationType,
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
</script>

<template>
  <div class="admin-material-log-page">
    <PageHeader title="耗材流水">
      <el-select v-model="filter.materialType" placeholder="材料类型" clearable style="width: 140px" @change="onSearch">
        <el-option v-for="t in MaterialTypes" :key="t" :label="t" :value="t" />
      </el-select>
      <el-input v-model="filter.color" placeholder="颜色" clearable style="width: 120px" @keyup.enter="onSearch" @clear="onSearch" />
      <el-select v-model="filter.operationType" placeholder="操作类型" clearable style="width: 120px" @change="onSearch">
        <el-option v-for="(name, value) in OperationTypeText" :key="value" :label="name" :value="Number(value)" />
      </el-select>
      <el-button type="primary" @click="onSearch">搜索</el-button>
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <el-card v-loading="store.loading">
      <EmptyState
        v-if="!store.logList || store.logList.list.length === 0"
        description="还没有耗材流水"
        hint="入库或打印完成后会自动生成流水记录"
      />

      <el-table v-else :data="store.logList.list" stripe>
        <el-table-column prop="logId" label="ID" width="80" />
        <el-table-column label="材料/颜色" width="180">
          <template #default="{ row }">
            <span>{{ MaterialTypeText[row.materialType as keyof typeof MaterialTypeText] || row.materialType }} / {{ row.color }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-tag :type="OperationTypeTagType[row.operationType as keyof typeof OperationTypeTagType]">
              {{ OperationTypeText[row.operationType as keyof typeof OperationTypeText] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变化（克）" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.weightChange > 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
              {{ row.weightChange > 0 ? '+' : '' }}{{ row.weightChange }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额（克）" width="120" />
        <el-table-column prop="relatedTaskId" label="关联任务" width="160">
          <template #default="{ row }">
            <code v-if="row.relatedTaskId" style="padding: 2px 6px; background: #f5f7fa; border-radius: 4px">
              {{ row.relatedTaskId }}
            </code>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorId" label="操作人" width="100" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="store.logList && store.logList.total > filter.size" class="pagination-wrap">
        <el-pagination
          :current-page="filter.page"
          :page-size="filter.size"
          :total="store.logList.total"
          layout="prev, pager, next, total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.admin-material-log-page { padding: 0; }
.pagination-wrap {
  display: flex; justify-content: center; margin-top: $spacing-large;
}
</style>
