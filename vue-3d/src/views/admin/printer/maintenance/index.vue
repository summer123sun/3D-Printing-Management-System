<script setup lang="ts">
/**
 * 维护记录（M5）
 *
 * STAFF+ 可新增，ADMIN 可删除
 */
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { usePrinterStore } from '@/stores/printer'
import { useAuthStore } from '@/stores/auth'
import { MaintType, MaintTypeText, MaintTypeTagType } from '@/types/printer'
import { formatDate, formatDateTime } from '@/utils/format'

const store = usePrinterStore()
const authStore = useAuthStore()

const isAdmin = computed(() => authStore.user?.role === 1)

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
  try {
    await store.addMaintenance(form.value)
    ElNotification.success('维护记录已添加')
    dialogVisible.value = false
    onSearch()
  } catch (e: any) {
    ElNotification.error(e.message || '添加失败')
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
  await store.removeMaintenance(row.maintId)
  ElNotification.success('已删除')
  onSearch()
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

    <el-dialog v-model="dialogVisible" title="新增维护记录" width="540px" :close-on-click-modal="false">
      <el-form :model="form" label-width="100px">
        <el-form-item label="打印机" required>
          <el-select v-model="form.printerId" filterable style="width: 100%">
            <el-option v-for="p in printers" :key="p.printerId" :label="`${p.printerId} - ${p.model}`" :value="p.printerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="维护类型" required>
          <el-select v-model="form.maintType" style="width: 100%">
            <el-option v-for="(name, value) in MaintTypeText" :key="value" :label="name" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="维护内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="详细描述维护操作" />
        </el-form-item>
        <el-form-item label="下次保养">
          <el-date-picker v-model="form.nextMaintDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-printer-maintenance-page { padding: 0; }
.pagination-wrap {
  display: flex; justify-content: center; margin-top: $spacing-large;
}
</style>
