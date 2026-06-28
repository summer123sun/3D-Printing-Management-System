<script setup lang="ts">
/**
 * 维护记录（M5）
 *
 * STAFF+ 可新增，ADMIN 可删除
 */
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import { Delete, Plus, Postcard, Tools, Document, Calendar } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { usePrinterStore } from '@/stores/printer'
import { useAuthStore } from '@/stores/auth'
import { MaintType, MaintTypeText, MaintTypeTagType, PrinterStatus, PrinterStatusText } from '@/types/printer'
import { Role } from '@/utils/enum'
import { formatDate, formatDateTime } from '@/utils/format'

const store = usePrinterStore()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.user?.role === Role.PRESIDENT)

const submitting = ref(false)

const filter = ref({
  page: 1,
  size: 20,
  printerId: '',
})

const dialogVisible = ref(false)
const form = ref({
  printerId: '',
  maintType: MaintType.ROUTINE,
  content: '',
  nextMaintDate: '',
})

const printers = ref<any[]>([])

const fetchData = async () => {
  await store.fetchList({ page: 1, size: 100 })
  printers.value = store.list?.list || []
  await store.fetchMaintenance({
    page: filter.value.page,
    size: filter.value.size,
    printerId: filter.value.printerId || undefined,
  })
}

onMounted(fetchData)

const onSearch = () => {
  filter.value.page = 1
  store.fetchMaintenance({
    page: 1,
    size: filter.value.size,
    printerId: filter.value.printerId || undefined,
  })
}

const onPageChange = (page: number) => {
  filter.value.page = page
  store.fetchMaintenance({
    page,
    size: filter.value.size,
    printerId: filter.value.printerId || undefined,
  })
}

const openCreate = () => {
  form.value = {
    printerId: filter.value.printerId || '',
    maintType: MaintType.ROUTINE,
    content: '',
    nextMaintDate: '',
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.printerId || !form.value.content) {
    ElNotification.warning('请选择打印机并填写维护内容')
    return
  }
  submitting.value = true
  try {
    await store.addMaintenance(form.value)

    // 联动：维修完成后，自动把打印机从【维修中】切回【正常】
    // - 仅当类型 = 维修 时触发
    // - 当前状态是维修中才切（避免误覆盖正常/报废状态）
    if (form.value.maintType === MaintType.REPAIR) {
      try {
        const target = printers.value.find(p => p.printerId === form.value.printerId)
        if (target && target.status === PrinterStatus.MAINTENANCE) {
          await store.setStatus(form.value.printerId, PrinterStatus.NORMAL)
          ElNotification.success(
            `维护记录已添加；${form.value.printerId} 已自动切回【${PrinterStatusText[PrinterStatus.NORMAL]}】`
          )
        } else {
          ElNotification.success('维护记录已添加')
        }
      } catch (e: any) {
        // 联动失败不阻塞主流程（记录已加成功）
        ElNotification.warning(`维护记录已添加，但状态联动失败：${e?.message || '未知错误'}`)
      }
    } else {
      ElNotification.success('维护记录已添加')
    }

    dialogVisible.value = false
    onSearch()
    // 同步刷新打印机列表（让 printer/list 的状态显示最新）
    store.fetchList({ page: 1, size: 100 })
  } catch {
    // 业务错误通知已由 request.ts 拦截器自动弹
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除此维护记录？', '删除确认', {
      type: 'warning',
      center: true,
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }
  try {
    await store.removeMaintenance(row.maintId)
    ElNotification.success('已删除')
    onSearch()
  } catch {
    // 业务错误通知已由 request.ts 拦截器自动弹
  }
}

const printerName = (id: string) => {
  return printers.value.find(p => p.printerId === id)?.printerId || id
}
</script>

<template>
  <div class="admin-printer-maintenance-page">
    <PageHeader title="维护记录">
      <el-select v-model="filter.printerId" placeholder="按打印机筛选" clearable filterable style="width: 180px" @change="onSearch">
        <el-option v-for="p in printers" :key="p.printerId" :label="`${p.printerId} - ${p.model}`" :value="p.printerId" />
      </el-select>
      <el-button @click="onSearch">筛选</el-button>
      <el-button type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新增记录
      </el-button>
    </PageHeader>

    <el-card v-loading="store.loading">
      <EmptyState
        v-if="!store.maintenanceList || store.maintenanceList.list.length === 0"
        description="还没有维护记录"
        hint="打印机维护后会自动把维修中的设备切回正常状态"
      >
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 新增记录
        </el-button>
      </EmptyState>

      <el-table v-else :data="store.maintenanceList.list" stripe>
        <el-table-column prop="maintId" label="ID" width="80" />
        <el-table-column label="打印机" width="160">
          <template #default="{ row }">
            <b>{{ printerName(row.printerId) }}</b>
          </template>
        </el-table-column>
        <el-table-column label="维护类型" width="100">
          <template #default="{ row }">
            <el-tag :type="MaintTypeTagType[row.maintType as keyof typeof MaintTypeTagType]">
              {{ MaintTypeText[row.maintType as keyof typeof MaintTypeText] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="维护内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="maintainerId" label="维护人" width="100" />
        <el-table-column label="维护时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.maintTime) }}</template>
        </el-table-column>
        <el-table-column label="下次保养" width="120">
          <template #default="{ row }">{{ formatDate(row.nextMaintDate) || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="store.maintenanceList && store.maintenanceList.total > filter.size" class="pagination-wrap">
        <el-pagination
          :current-page="filter.page"
          :page-size="filter.size"
          :total="store.maintenanceList.total"
          layout="prev, pager, next, total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <AppDialog v-model="dialogVisible" title="新增维护记录" icon="Tools" type="primary" width="560px"
               confirm-text="保存" :loading="submitting" @confirm="handleSubmit">
      <div class="maint-form">
        <!-- 维护对象 -->
        <div class="form-section">
          <div class="section-header">
            <el-icon class="section-icon"><Postcard /></el-icon>
            <span class="section-title">维护对象</span>
          </div>
          <div class="form-field">
            <label class="field-label">打印机 <span class="required">*</span></label>
            <el-select v-model="form.printerId" filterable placeholder="选择打印机（如 P-001）" size="large" style="width: 100%">
              <el-option v-for="p in printers" :key="p.printerId"
                         :label="`${p.printerId} - ${p.model}`" :value="p.printerId" />
            </el-select>
          </div>
        </div>

        <el-divider class="section-divider" />

        <!-- 维护信息 -->
        <div class="form-section">
          <div class="section-header">
            <el-icon class="section-icon"><Tools /></el-icon>
            <span class="section-title">维护信息</span>
            <span class="section-hint" v-if="form.maintType === MaintType.REPAIR">选"维修"保存后会自动把维修中的设备切回【正常】</span>
          </div>
          <div class="form-field">
            <label class="field-label">维护类型 <span class="required">*</span></label>
            <el-radio-group v-model="form.maintType" size="large">
              <el-radio-button :value="MaintType.ROUTINE">保养</el-radio-button>
              <el-radio-button :value="MaintType.REPAIR">维修</el-radio-button>
              <el-radio-button :value="MaintType.REPLACE">换件</el-radio-button>
              <el-radio-button :value="MaintType.CALIBRATE">校准</el-radio-button>
            </el-radio-group>
          </div>
          <div class="form-field">
            <label class="field-label">维护内容 <span class="required">*</span></label>
            <el-input v-model="form.content" type="textarea" :rows="4" maxlength="500" show-word-limit
                      placeholder="详细描述维护操作，如：更换 0.4mm 喷嘴，检查 Z 轴丝杆..." />
          </div>
        </div>

        <el-divider class="section-divider" />

        <!-- 后续 -->
        <div class="form-section">
          <div class="section-header">
            <el-icon class="section-icon"><Calendar /></el-icon>
            <span class="section-title">后续安排</span>
          </div>
          <div class="form-field">
            <label class="field-label">下次保养日期</label>
            <el-date-picker v-model="form.nextMaintDate" type="date" value-format="YYYY-MM-DD"
                            placeholder="选择下次保养日期（可选）" size="large" style="width: 100%" />
          </div>
        </div>
      </div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-printer-maintenance-page { padding: 0; }
.pagination-wrap {
  display: flex; justify-content: center; margin-top: $spacing-large;
}

// ========== 新增维护记录表单美化（与打印机新增弹窗统一风格） ==========
.maint-form {
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
}
.section-hint {
  margin-left: auto;
  font-size: 12px;
  color: #FF7A45;
  background: rgba(255, 122, 69, 0.08);
  padding: 2px 8px;
  border-radius: 4px;
}
.section-divider {
  margin: 16px 0 !important;
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
  margin-bottom: 8px;
  font-weight: 500;
  .required {
    color: #FF4757;
    margin-left: 2px;
  }
}
</style>
