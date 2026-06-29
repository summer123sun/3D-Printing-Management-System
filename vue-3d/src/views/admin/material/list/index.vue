<script setup lang="ts">
/**
 * 耗材库存列表（M5）
 *
 * 按材料类型 + 颜色聚合的当前库存
 * 含库存预警卡片
 */
import { computed, onMounted, ref } from 'vue'
import { ElNotification } from 'element-plus'
import { Warning, Upload } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useMaterialStore } from '@/stores/material'
import { MaterialTypes, MaterialTypeText } from '@/types/material'
import { formatDateTime } from '@/utils/format'

const store = useMaterialStore()
const router = useRouter()

const filterType = ref<string | undefined>(undefined)
const threshold = ref(500)

const fetchData = async () => {
  await store.fetchStocks(filterType.value)
  await store.fetchWarnings(threshold.value)
  await store.fetchSummary()
}

onMounted(fetchData)

const onFilterChange = () => fetchData()

const goInbound = () => {
  router.push('/admin/material/inbound')
}

const summary = computed(() => store.summary || {})

const stockByType = computed(() => {
  const map: Record<string, number> = {}
  for (const s of store.stocks) {
    map[s.materialType] = (map[s.materialType] || 0) + (s.currentStock || 0)
  }
  return map
})

// ✅ v2.2 修复：库存表点击列头排序（前端排序，因为后端 stockList 不支持排序参数）
const sortKey = ref<keyof typeof store.stocks[0] | ''>('')
const sortOrder = ref<'ascending' | 'descending' | ''>('')

const sortedStocks = computed(() => {
  if (!sortKey.value || !sortOrder.value) {
    return [...store.stocks]  // 保持原顺序（按 materialType+color）
  }
  const k = sortKey.value as keyof typeof store.stocks[0]
  const dir = sortOrder.value === 'ascending' ? 1 : -1
  return [...store.stocks].sort((a, b) => {
    const av = a[k]
    const bv = b[k]
    if (av == null && bv == null) return 0
    if (av == null) return 1
    if (bv == null) return -1
    if (typeof av === 'number' && typeof bv === 'number') return (av - bv) * dir
    return String(av).localeCompare(String(bv)) * dir
  })
})

const onSortChange = ({ prop, order }: { prop: string | null; order: 'ascending' | 'descending' | null }) => {
  sortKey.value = (prop as keyof typeof store.stocks[0]) || ''
  sortOrder.value = order || ''
}
</script>

<template>
  <div class="admin-material-list-page">
    <PageHeader title="耗材库存">
      <el-select v-model="filterType" placeholder="按材料类型筛选" clearable style="width: 180px" @change="onFilterChange">
        <el-option v-for="t in MaterialTypes" :key="t" :label="MaterialTypeText[t]" :value="t" />
      </el-select>
      <el-button @click="fetchData">刷新</el-button>
      <el-button type="primary" @click="goInbound">
        <el-icon><Upload /></el-icon> 入库
      </el-button>
    </PageHeader>

    <!-- 总览卡片 -->
    <el-row :gutter="16" class="summary-row">
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="summary-value">{{ Math.round(summary.totalStock || 0) }}</div>
          <div class="summary-label">总库存（克）</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card">
          <div class="summary-value">{{ summary.typeCount || 0 }}</div>
          <div class="summary-label">材料种类</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card warning">
          <div class="summary-value">
            <el-icon><Warning /></el-icon>
            {{ summary.warningCount || 0 }}
          </div>
          <div class="summary-label">预警数（&lt; {{ threshold }}g）</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card info">
          <div class="summary-value">{{ Object.keys(stockByType).length }}</div>
          <div class="summary-label">覆盖材料类型</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 库存预警 -->
    <el-card v-if="store.warnings.length > 0" class="warning-card">
      <template #header>
        <span class="warning-header">
          <el-icon color="#e6a23c"><Warning /></el-icon>
          库存预警（{{ store.warnings.length }} 项需补货）
        </span>
      </template>
      <el-table :data="store.warnings" stripe size="small">
        <el-table-column prop="materialType" label="材料" width="120">
          <template #default="{ row }">{{ MaterialTypeText[row.materialType as keyof typeof MaterialTypeText] || row.materialType }}</template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="120" />
        <el-table-column label="当前库存" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">{{ row.currentStock }} g</span>
          </template>
        </el-table-column>
        <el-table-column label="预警线" width="100">
          <template #default="{ row }">
            <span>{{ threshold }} g</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goInbound">立即补货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 完整库存表 -->
    <el-card v-loading="store.loading">
      <template #header>
        <span>📦 全部库存（按材料+颜色）</span>
      </template>
      <EmptyState
        v-if="store.stocks.length === 0"
        description="还没有库存记录"
        hint="点击右上角【入库】添加第一笔耗材"
      >
        <el-button type="primary" @click="goInbound">
          <el-icon><Upload /></el-icon> 入库
        </el-button>
      </EmptyState>

      <el-table v-else :data="sortedStocks" stripe @sort-change="onSortChange">
        <el-table-column prop="materialType" label="材料类型" width="140" sortable="custom">
          <template #default="{ row }">{{ MaterialTypeText[row.materialType as keyof typeof MaterialTypeText] || row.materialType }}</template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="120" sortable="custom" />
        <el-table-column prop="currentStock" label="当前库存（克）" min-width="120" sortable="custom">
          <template #default="{ row }">
            <b :style="{ color: row.currentStock < threshold ? '#f56c6c' : '#67c23a' }">
              {{ row.currentStock }} g
            </b>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.currentStock < threshold" type="danger" effect="dark">需补货</el-tag>
            <el-tag v-else-if="row.currentStock < threshold * 2" type="warning" effect="dark">偏低</el-tag>
            <el-tag v-else type="success" effect="dark">充足</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近更新" width="180" sortable="custom" prop="lastUpdateTime">
          <template #default="{ row }">{{ formatDateTime(row.lastUpdateTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default>
            <el-button size="small" type="primary" @click="goInbound">入库</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.admin-material-list-page { padding: 0; }
.summary-row { margin-bottom: $spacing-medium; }
.summary-card {
  text-align: center;
  .summary-value {
    font-size: 28px;
    font-weight: 600;
    color: $brand-color;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
  .summary-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 4px;
  }
  &.warning .summary-value { color: #e6a23c; }
  &.info .summary-value { color: var(--text-secondary); }
}
.warning-card { margin-bottom: $spacing-medium; }
.warning-header {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e6a23c;
  font-weight: 500;
}
</style>
