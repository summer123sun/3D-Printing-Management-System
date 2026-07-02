<script setup lang="ts">
/**
 * 打印机台账（M5 - 管理端）
 *
 * STAFF+ 可设置状态，ADMIN 可增删改
 */
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import {
  Plus, Edit, Delete, Tools,
  Postcard, Box, ShoppingCart, Position, Document,
} from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { usePrinterStore } from '@/stores/printer'
import { useAuthStore } from '@/stores/auth'
import { PrinterStatus, PrinterStatusText, PrinterStatusTagType } from '@/types/printer'
import { Role } from '@/utils/enum'
import { formatDate } from '@/utils/format'

const store = usePrinterStore()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.user?.role === Role.PRESIDENT)

const submitting = ref(false)

const filter = ref({
  page: 1,
  size: 20,
  status: undefined as number | undefined,
  keyword: '',
})

const dialogVisible = ref(false)
const editingId = ref<string | null>(null)
const form = ref({
  printerId: '',
  model: '',
  brand: '',
  purchaseDate: '',
  location: '',
  nozzleSize: 0.4,
  buildVolume: '',
  remark: '',
})

const resetForm = () => {
  form.value = {
    printerId: '',
    model: '',
    brand: '',
    purchaseDate: '',
    location: '',
    nozzleSize: 0.4,
    buildVolume: '',
    remark: '',
  }
  editingId.value = null
}

const fetchData = async () => {
  await store.fetchList({
    page: filter.value.page,
    size: filter.value.size,
    status: filter.value.status,
    keyword: filter.value.keyword || undefined,
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

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  editingId.value = row.printerId
  form.value = {
    printerId: row.printerId,
    model: row.model,
    brand: row.brand || '',
    purchaseDate: row.purchaseDate || '',
    location: row.location || '',
    nozzleSize: row.nozzleSize || 0.4,
    buildVolume: row.buildVolume || '',
    remark: row.remark || '',
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.printerId || !form.value.model) {
    ElNotification.warning('请填写打印机编号和型号')
    return
  }
  submitting.value = true
  try {
    if (editingId.value) {
      await store.update(editingId.value, form.value)
      ElNotification.success('已更新')
    } else {
      await store.create(form.value)
      ElNotification.success('已新增')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 业务错误通知已由 request.ts 拦截器自动弹
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定删除「${row.printerId}」吗？仅【报废】状态的打印机可删除。`,
      '删除确认',
      {
        type: 'warning',
        center: true,
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
      }
    )
  } catch {
    return
  }
  try {
    await store.remove(row.printerId)
    ElNotification.success('已删除')
    fetchData()
  } catch {
    // 业务错误通知已由 request.ts 拦截器自动弹
  }
}

const handleSetStatus = async (row: any, newStatus: number) => {
  const statusName = PrinterStatusText[newStatus as keyof typeof PrinterStatusText]
  try {
    await ElMessageBox.confirm(
      `确定将「${row.printerId}」设为【${statusName}】吗？`,
      '设置状态',
      { type: 'warning', center: true, confirmButtonText: '确定', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  try {
    await store.setStatus(row.printerId, newStatus)
    ElNotification.success(`已设为【${statusName}】`)
    fetchData()
  } catch {
    // 业务错误通知已由 request.ts 拦截器自动弹
  }
}
</script>

<template>
  <div class="admin-printer-list-page">
    <PageHeader title="打印机管理">
      <el-select v-model="filter.status" placeholder="筛选状态" clearable style="width: 140px" @change="onSearch">
        <el-option v-for="(name, value) in PrinterStatusText" :key="value" :label="name" :value="Number(value)" />
      </el-select>
      <el-input
        v-model="filter.keyword"
        placeholder="按编号/型号搜索"
        clearable
        style="width: 200px"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-button type="primary" @click="onSearch">搜索</el-button>
      <el-button @click="fetchData">刷新</el-button>
      <el-button v-if="isAdmin" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新增打印机
      </el-button>
    </PageHeader>

    <el-card v-loading="store.loading">
      <EmptyState
        v-if="!store.list || store.list.list.length === 0"
        description="还没有打印机"
        hint="新增第一台打印机开始管理吧"
      >
        <el-button v-if="isAdmin" type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 新增打印机
        </el-button>
      </EmptyState>

      <el-table v-else :data="store.list.list" stripe>
        <el-table-column prop="printerId" label="编号" width="120" fixed />
        <el-table-column prop="model" label="型号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="buildVolume" label="成型尺寸" width="140" />
        <el-table-column prop="nozzleSize" label="喷嘴" width="80">
          <template #default="{ row }">{{ row.nozzleSize }}mm</template>
        </el-table-column>
        <el-table-column prop="location" label="位置" width="140" show-overflow-tooltip />
        <el-table-column prop="totalPrintHours" label="累计时长" width="100" sortable>
          <template #default="{ row }">{{ row.totalPrintHours }} h</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="PrinterStatusTagType[row.status as keyof typeof PrinterStatusTagType]" effect="dark">
              {{ PrinterStatusText[row.status as keyof typeof PrinterStatusText] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(cmd: string) => handleSetStatus(row, Number(cmd))">
              <el-button size="small">
                <el-icon><Tools /></el-icon> 切状态
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="(name, value) in PrinterStatusText"
                    :key="value"
                    :command="value"
                    :disabled="row.status === Number(value)"
                  >
                    {{ name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-if="isAdmin" size="small" @click="openEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button v-if="isAdmin" size="small" type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
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

    <!-- 新增/编辑对话框 -->
    <AppDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑打印机' : '新增打印机'"
      :icon="editingId ? 'Edit' : 'Plus'"
      :type="editingId ? 'warning' : 'primary'"
      width="640px"
      confirm-text="保存"
      :loading="submitting"
      @confirm="handleSubmit"
    >
      <div class="printer-form">
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="section-header">
            <el-icon class="section-icon"><Postcard /></el-icon>
            <span class="section-title">基本信息</span>
            <span class="section-required">必填项标 *</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="form-field">
                <label class="field-label">
                  编号 <span class="required">*</span>
                </label>
                <el-input
                  v-model="form.printerId"
                  :disabled="!!editingId"
                  placeholder="如 P-001"
                  maxlength="10"
                  size="large"
                >
                  <template #prefix><el-icon><Postcard /></el-icon></template>
                </el-input>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="form-field">
                <label class="field-label">
                  型号 <span class="required">*</span>
                </label>
                <el-input
                  v-model="form.model"
                  placeholder="如 创想三维 K1"
                  size="large"
                >
                  <template #prefix><el-icon><Box /></el-icon></template>
                </el-input>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="form-field">
                <label class="field-label">品牌</label>
                <el-input v-model="form.brand" placeholder="如 Creality" size="large" />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="form-field">
                <label class="field-label">位置</label>
                <el-input
                  v-model="form.location"
                  placeholder="如 打印室A-3号"
                  size="large"
                >
                  <template #prefix><el-icon><Position /></el-icon></template>
                </el-input>
              </div>
            </el-col>
          </el-row>
        </div>

        <el-divider class="section-divider" />

        <!-- 技术参数 -->
        <div class="form-section">
          <div class="section-header">
            <el-icon class="section-icon"><Tools /></el-icon>
            <span class="section-title">技术参数</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="form-field">
                <label class="field-label">成型尺寸</label>
                <el-input
                  v-model="form.buildVolume"
                  placeholder="如 220x220x250mm"
                  size="large"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="form-field">
                <label class="field-label">喷嘴尺寸</label>
                <div class="nozzle-input">
                  <el-input-number
                    v-model="form.nozzleSize"
                    :min="0.1"
                    :max="2"
                    :step="0.1"
                    :precision="1"
                    size="large"
                    style="width: 100%"
                  />
                  <span class="unit">mm</span>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <el-divider class="section-divider" />

        <!-- 其他信息 -->
        <div class="form-section">
          <div class="section-header">
            <el-icon class="section-icon"><Document /></el-icon>
            <span class="section-title">其他信息</span>
          </div>
          <el-row :gutter="16">
            <el-col :span="24">
              <div class="form-field">
                <label class="field-label">购买日期</label>
                <el-date-picker
                  v-model="form.purchaseDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="选择购买日期"
                  size="large"
                  style="width: 100%"
                />
              </div>
            </el-col>
            <el-col :span="24">
              <div class="form-field">
                <label class="field-label">备注</label>
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="其他需要说明的信息（可选）"
                />
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-printer-list-page { padding: 0; }
.pagination-wrap {
  display: flex; justify-content: center; margin-top: $spacing-large;
}

// ========== 新增/编辑打印机表单美化 ==========
.printer-form {
  padding: 4px 4px 0;
}
.form-section {
  margin-bottom: 4px;
  &:last-child { margin-bottom: 0; }
}
.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  padding-left: 2px;
}
.section-icon {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: linear-gradient(135deg, #0A2540 0%, #1a4d7a 100%);
  color: #fff;
  padding: 3px;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #0A2540;
  letter-spacing: 0.3px;
  // 暗色下换亮色
  html.dark & {
    color: #4FE5C7;
  }
}
.section-required {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}
.section-divider {
  margin: 18px 0 !important;
  border-top: 1px dashed #e4e7ed !important;
}
.form-field {
  margin-bottom: 14px;
  &:last-child { margin-bottom: 0; }
}
.field-label {
  display: block;
  font-size: 13px;
  color: #303133;
  margin-bottom: 6px;
  font-weight: 500;
  .required {
    color: #FF4757;
    margin-left: 2px;
  }
}
.nozzle-input {
  display: flex;
  align-items: center;
  gap: 8px;
  .unit {
    font-size: 13px;
    color: #606266;
    flex-shrink: 0;
  }
}
</style>
