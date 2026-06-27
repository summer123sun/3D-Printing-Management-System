<script setup lang="ts">
/**
 * 数据驾驶舱（M6）
 *
 * 4 个核心数字卡片 + 7 天趋势图 + 多维度统计
 */
import { onMounted, ref } from 'vue'
import { Document, Goods, Operation, Printer, Star, Timer, TrendCharts, User } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { get } from '@/utils/request'
import type { DashboardData, TaskStats, MaterialStats, MemberRank, PrinterStats } from '@/types/stats'
import { formatDuration } from '@/utils/format'

const dashboard = ref<DashboardData | null>(null)
const taskStats = ref<TaskStats | null>(null)
const materialStats = ref<MaterialStats | null>(null)
const memberRanking = ref<MemberRank[]>([])
const printerStats = ref<PrinterStats | null>(null)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const [d, t, m, p, r] = await Promise.all([
      get<DashboardData>('/stats/dashboard'),
      get<TaskStats>('/stats/task'),
      get<MaterialStats>('/stats/material'),
      get<PrinterStats>('/stats/printer'),
      get<MemberRank[]>('/stats/member', { limit: 8 } as any),
    ])
    dashboard.value = d
    taskStats.value = t
    materialStats.value = m
    printerStats.value = p
    memberRanking.value = r
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// 7 天趋势：手写 SVG 折线图
const trendPath = (data: any[] | undefined, width = 580, height = 160) => {
  if (!data || data.length === 0) return { path: '', area: '', points: [] as any[], max: 0 }
  const max = Math.max(...data.map(d => d.count), 1)
  const points = data.map((d, i) => {
    const x = (i / (data.length - 1)) * width
    const y = height - (d.count / max) * (height - 30) - 10
    return { x, y, date: d.date, count: d.count }
  })
  const path = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x.toFixed(1)} ${p.y.toFixed(1)}`).join(' ')
  const area = path + ` L ${width} ${height} L 0 ${height} Z`
  return { path, area, points, max }
}

const trend = ref<{ path: string; area: string; points: any[]; max: number }>({ path: '', area: '', points: [], max: 0 })
const updateTrend = () => {
  trend.value = trendPath(dashboard.value?.trend7d, 580, 160)
}

import { watch } from 'vue'
watch(dashboard, updateTrend, { immediate: true })
</script>

<template>
  <div class="admin-dashboard-page" v-loading="loading">
    <PageHeader title="数据驾驶舱" />

    <!-- 4 个核心数字 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card stat-1">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard?.totalTasks || 0 }}</div>
            <div class="stat-label">总任务数</div>
            <div class="stat-sub">已完成 {{ dashboard?.doneTasks || 0 }} · 待审批 {{ dashboard?.pendingTasks || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-2">
          <div class="stat-icon"><el-icon><Timer /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ formatDuration(dashboard?.totalPrintHours || 0) }}</div>
            <div class="stat-label">累计打印时长</div>
            <div class="stat-sub">完成率 {{ dashboard?.completionRate || 0 }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-3">
          <div class="stat-icon"><el-icon><Goods /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ Math.round(dashboard?.totalWeight || 0) }}<span style="font-size: 14px; margin-left: 4px">g</span></div>
            <div class="stat-label">累计耗材</div>
            <div class="stat-sub">PLA/PETG/ABS/TPU 全部</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-4">
          <div class="stat-icon"><el-icon><Star /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ dashboard?.totalArtworks || 0 }}</div>
            <div class="stat-label">作品数</div>
            <div class="stat-sub">⭐ 推荐 {{ dashboard?.recommendedArtworks || 0 }} 个</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <!-- 7 天任务趋势 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span><el-icon><TrendCharts /></el-icon> 近 7 天任务提交趋势</span>
              <span class="chart-max">峰值：{{ trend.max }}</span>
            </div>
          </template>
          <div class="trend-chart">
            <svg viewBox="0 0 580 160" class="trend-svg">
              <defs>
                <linearGradient id="trend-gradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#409eff" stop-opacity="0.3" />
                  <stop offset="100%" stop-color="#409eff" stop-opacity="0.05" />
                </linearGradient>
              </defs>
              <path :d="trend.area" fill="url(#trend-gradient)" />
              <path :d="trend.path" fill="none" stroke="#409eff" stroke-width="2" />
              <g v-for="(p, i) in trend.points" :key="i">
                <circle :cx="p.x" :cy="p.y" r="4" fill="white" stroke="#409eff" stroke-width="2" />
                <text :x="p.x" y="155" text-anchor="middle" font-size="10" fill="#909399">
                  {{ p.date.slice(5) }}
                </text>
                <text :x="p.x" :y="p.y - 8" text-anchor="middle" font-size="11" fill="#303133" font-weight="600">
                  {{ p.count }}
                </text>
              </g>
            </svg>
          </div>
        </el-card>
      </el-col>

      <!-- 任务状态分布 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span><el-icon><Operation /></el-icon> 任务状态分布</span>
          </template>
          <div class="status-list">
            <div v-for="item in taskStats?.byStatus" :key="item.status" class="status-item">
              <div class="status-info">
                <span class="status-dot" :class="`status-${item.status}`"></span>
                <span>{{ ['', '', '已通过', '已驳回', '排队中', '打印中', '已完成', '已取消'][item.status] || '待审批' }}</span>
              </div>
              <div class="status-bar">
                <div
                  class="status-fill"
                  :class="`status-${item.status}`"
                  :style="{ width: (item.count / (taskStats?.byStatus?.reduce((s, x) => s + x.count, 0) || 1) * 100) + '%' }"
                />
              </div>
              <div class="status-count">{{ item.count }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <!-- 月度趋势 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span><el-icon><TrendCharts /></el-icon> 最近 6 个月任务数</span>
          </template>
          <div class="month-chart">
            <div v-for="item in taskStats?.monthly" :key="item.month" class="month-bar">
              <div
                class="month-fill"
                :style="{ height: (item.count / Math.max(...(taskStats?.monthly || []).map(m => m.count), 1) * 100) + '%' }"
              >
                <span class="month-value">{{ item.count }}</span>
              </div>
              <div class="month-label">{{ item.month }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 材料消耗 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span><el-icon><Goods /></el-icon> 材料消耗排行</span>
          </template>
          <div class="material-list">
            <div v-for="(item, i) in materialStats?.byMaterial" :key="i" class="material-item">
              <div class="material-rank">#{{ i + 1 }}</div>
              <div class="material-name">{{ item.material_type }}</div>
              <div class="material-bar">
                <div
                  class="material-fill"
                  :style="{ width: (item.total_used / Math.max(...(materialStats?.byMaterial || []).map(m => m.total_used), 1) * 100) + '%' }"
                />
              </div>
              <div class="material-value">{{ Math.round(item.total_used) }} g</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <!-- 打印机使用率 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span><el-icon><Printer /></el-icon> 打印机状态</span>
          </template>
          <div class="printer-summary">
            <div class="printer-stat">
              <div class="printer-num" style="color: #67c23a">{{ printerStats?.normalCount || 0 }}</div>
              <div class="printer-label">正常</div>
            </div>
            <div class="printer-stat">
              <div class="printer-num" style="color: #e6a23c">{{ printerStats?.maintCount || 0 }}</div>
              <div class="printer-label">维修中</div>
            </div>
            <div class="printer-stat">
              <div class="printer-num" style="color: #909399">{{ printerStats?.scrappedCount || 0 }}</div>
              <div class="printer-label">报废</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 活跃排行 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span><el-icon><User /></el-icon> 活跃社员 Top 8</span>
          </template>
          <div class="ranking-list">
            <div v-for="(item, i) in memberRanking" :key="item.applicant_id" class="ranking-item">
              <div class="ranking-num" :class="i < 3 ? `top-${i + 1}` : ''">{{ i + 1 }}</div>
              <div class="ranking-id">{{ item.applicant_id }}</div>
              <div class="ranking-meta">
                完成 <b>{{ item.done_count }}</b> 次 · {{ Math.round(item.total_hours || 0) }}h
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.admin-dashboard-page { padding: 0; }
.stat-row { margin-bottom: $spacing-medium; }
.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-base;
  border: none;
  background: linear-gradient(135deg, #fff 0%, #f8fafc 100%);
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(64, 158, 255, 0.1);
    color: #409eff;
    font-size: 28px;
  }
  .stat-value {
    font-size: 26px;
    font-weight: 600;
    color: $text-primary;
    line-height: 1.2;
  }
  .stat-label {
    font-size: 13px;
    color: $text-secondary;
    margin-top: 2px;
  }
  .stat-sub {
    font-size: 12px;
    color: $text-placeholder;
    margin-top: 2px;
  }
  &.stat-1 .stat-icon { background: rgba(64, 158, 255, 0.1); color: #409eff; }
  &.stat-2 .stat-icon { background: rgba(230, 162, 60, 0.1); color: #e6a23c; }
  &.stat-3 .stat-icon { background: rgba(103, 194, 58, 0.1); color: #67c23a; }
  &.stat-4 .stat-icon { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }
}
.chart-row { margin-bottom: $spacing-medium; }
.chart-header {
  display: flex; justify-content: space-between; align-items: center;
  .chart-max {
    font-size: 12px;
    color: $text-secondary;
  }
}
.trend-chart { padding: $spacing-small 0; }
.trend-svg { width: 100%; height: auto; }
.status-list { display: flex; flex-direction: column; gap: $spacing-small; }
.status-item {
  display: grid;
  grid-template-columns: 100px 1fr 40px;
  align-items: center;
  gap: $spacing-small;
  .status-info {
    display: flex; align-items: center; gap: 6px;
    font-size: 13px;
  }
  .status-dot {
    width: 8px; height: 8px; border-radius: 50%;
  }
  .status-bar {
    height: 8px;
    background: $bg-base;
    border-radius: 4px;
    overflow: hidden;
  }
  .status-fill {
    height: 100%;
    border-radius: 4px;
    transition: width 0.3s;
  }
  .status-count {
    font-weight: 600;
    color: $text-primary;
    text-align: right;
  }
  .status-dot.status-0, .status-fill.status-0 { background: #909399; }
  .status-dot.status-1, .status-fill.status-1 { background: #67c23a; }
  .status-dot.status-2, .status-fill.status-2 { background: #f56c6c; }
  .status-dot.status-3, .status-fill.status-3 { background: #e6a23c; }
  .status-dot.status-4, .status-fill.status-4 { background: #409eff; }
  .status-dot.status-5, .status-fill.status-5 { background: #67c23a; }
  .status-dot.status-6, .status-fill.status-6 { background: #c0c4cc; }
}
.month-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  padding: $spacing-small 0;
}
.month-bar {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 0 4px;
  height: 100%;
}
.month-fill {
  width: 80%;
  max-width: 50px;
  background: linear-gradient(180deg, #409eff 0%, #79bbff 100%);
  border-radius: 4px 4px 0 0;
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
  min-height: 2px;
  transition: height 0.3s;
  .month-value {
    color: white;
    font-size: 11px;
    font-weight: 600;
  }
}
.month-label {
  margin-top: 6px;
  font-size: 12px;
  color: $text-secondary;
}
.material-list { display: flex; flex-direction: column; gap: $spacing-small; }
.material-item {
  display: grid;
  grid-template-columns: 40px 80px 1fr 80px;
  align-items: center;
  gap: $spacing-small;
  font-size: 13px;
  .material-rank {
    color: $text-placeholder;
    font-weight: 600;
  }
  .material-name {
    color: $text-regular;
    font-weight: 500;
  }
  .material-bar {
    height: 6px;
    background: $bg-base;
    border-radius: 3px;
    overflow: hidden;
  }
  .material-fill {
    height: 100%;
    background: linear-gradient(90deg, #409eff, #67c23a);
    border-radius: 3px;
  }
  .material-value {
    text-align: right;
    color: $text-primary;
    font-weight: 500;
  }
}
.printer-summary {
  display: flex;
  justify-content: space-around;
  padding: $spacing-large 0;
}
.printer-stat {
  text-align: center;
  .printer-num {
    font-size: 36px;
    font-weight: 600;
  }
  .printer-label {
    font-size: 13px;
    color: $text-secondary;
    margin-top: 4px;
  }
}
.ranking-list { display: flex; flex-direction: column; gap: 6px; }
.ranking-item {
  display: flex;
  align-items: center;
  gap: $spacing-small;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background 0.2s;
  &:hover { background: $bg-base; }
  .ranking-num {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: $bg-base;
    color: $text-secondary;
    display: flex; align-items: center; justify-content: center;
    font-weight: 600;
    font-size: 13px;
    &.top-1 { background: linear-gradient(135deg, #ffd700, #ffa500); color: white; }
    &.top-2 { background: linear-gradient(135deg, #c0c0c0, #909399); color: white; }
    &.top-3 { background: linear-gradient(135deg, #cd7f32, #8b4513); color: white; }
  }
  .ranking-id {
    font-family: monospace;
    color: $text-primary;
    font-weight: 500;
  }
  .ranking-meta {
    flex: 1;
    font-size: 12px;
    color: $text-secondary;
    text-align: right;
  }
}
</style>
