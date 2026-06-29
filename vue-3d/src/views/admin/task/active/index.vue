<script setup lang="ts">
/**
 * 进行中任务（**B** - 管理端）
 *
 * 状态=3 排队中 + 4 打印中
 * 行内操作：分配打印机 / 开始打印 / 完成打印
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useTaskStore } from '@/stores/task'
import { usePrinterStore } from '@/stores/printer'
import { TaskStatus, Priority } from '@/types/task'
import { updateTask } from '@/api/task'

const router = useRouter()
const taskStore = useTaskStore()
const printerStore = usePrinterStore()

const fetchData = async () => {
  await taskStore.fetchQueue({ page: 1, size: 50, keyword: searchKeyword.value.trim() || undefined })
  // 加载可用打印机（不传参=全部，让后端过滤空闲）
  await printerStore.fetchAvailable()
}

// ✅ v2.2 新增：搜索框
const searchKeyword = ref('')
const onSearch = () => fetchData()

onMounted(fetchData)

const assignDialogVisible = ref(false)
const assignForm = reactive({ taskId: '', printerId: '', printerName: '' })
const submittingAssign = ref(false)

const finishDialogVisible = ref(false)
const finishForm = reactive({ taskId: '', actualWeight: 0, actualTime: 0, qualityScore: 5 })
const submittingFinish = ref(false)

// ===== 优先级修改弹窗（v2.2 新增） =====
const priorityDialogVisible = ref(false)
const priorityForm = reactive({ taskId: '', taskTitle: '', newPriority: Priority.NORMAL })
const submittingPriority = ref(false)

const PriorityText: Record<Priority, string> = {
  [Priority.URGENT]: '紧急',
  [Priority.NORMAL]: '普通',
  [Priority.LOW]: '低优',
}

const openPriority = (row: { taskId: string; title: string; priority: number }) => {
  priorityForm.taskId = row.taskId
  priorityForm.taskTitle = row.title
  priorityForm.newPriority = row.priority || Priority.NORMAL
  priorityDialogVisible.value = true
}

const handlePriority = async () => {
  submittingPriority.value = true
  try {
    await updateTask(priorityForm.taskId, { priority: priorityForm.newPriority })
    ElNotification.success(`优先级已更新为「${PriorityText[priorityForm.newPriority]}」`)
    priorityDialogVisible.value = false
    await fetchData()
  } catch (e: any) {
    // ✅ v2.2 改进：把 500/422/400 都直接弹给用户，方便诊断（之前只 console.error）
    const msg = e?.response?.data?.message || e?.response?.data?.msg || e?.message || '未知错误'
    const code = e?.response?.status || '?'
    ElNotification.error({
      title: `修改优先级失败（HTTP ${code}）`,
      message: msg,
      duration: 0,  // 不自动关，让用户能看清
      showClose: true,
    })
    console.error('[修改优先级] 失败完整堆栈：', e)
  } finally {
    submittingPriority.value = false
  }
}

/** 当前选中的打印机详情 */
const selectedPrinter = computed(() => {
  return printerStore.availableList.find(p => p.printerId === assignForm.printerId)
})

const openAssign = (taskId: string) => {
  assignForm.taskId = taskId
  assignForm.printerId = ''
  assignForm.printerName = ''
  assignDialogVisible.value = true
}

const handleAssign = async () => {
  if (!assignForm.printerId) {
    ElNotification.warning('请选择打印机')
    return
  }
  submittingAssign.value = true
  try {
    await taskStore.assignPrinter(assignForm.taskId, { printerId: assignForm.printerId })
    // ✅ 关闭弹窗
    assignDialogVisible.value = false

    // ✅ 弹醒目的成功弹窗（不依赖 await，用户可以"关闭"先看表格变化）
    ElNotification.success({
      title: '✅ 分配成功',
      message: `任务 ${assignForm.taskId} 已分配到 ${assignForm.printerId}`,
      duration: 4000,
    })

    // ✅ 刷新列表（强制重新拉取）
    await fetchData()
    // ✅ 最后弹可视化大弹窗（用 setTimeout 延迟 200ms 让列表先刷新）
    setTimeout(async () => {
      try {
        await ElMessageBox.alert(
          `<div class="assign-success">
            <div class="success-icon-wrap">
              <div class="success-icon-circle">
                <svg viewBox="0 0 52 52" class="success-icon-svg">
                  <circle class="success-icon-circle-bg" cx="26" cy="26" r="25" fill="none"/>
                  <path class="success-icon-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
                </svg>
              </div>
            </div>
            <h2 class="success-title">分配成功！</h2>
            <p class="success-subtitle">任务 <b>${assignForm.taskId}</b> 已分配到 <b style="color:#409eff">${assignForm.printerId}</b></p>
            <p style="color:var(--text-secondary);font-size:13px;margin:0">📋 你可以现在点"开始"按钮启动打印</p>
          </div>`,
          '',
          {
            confirmButtonText: '好的',
            type: 'success',
            center: true,
            dangerouslyUseHTMLString: true,
            showClose: true,
          }
        )
      } catch {}
    }, 200)
  } catch (e: any) {
    console.error('[分配打印机] 失败：', e)
    // 错误码 1502 = 打印机占用，1501 = 打印机不存在
    const msg = e?.message || '请检查后端服务'
    const isBusy = msg.includes('正在打印') || msg.includes('占用')
    const isNotFound = msg.includes('不存在')
    ElNotification.error({
      title: '❌ 分配失败',
      message: isBusy
        ? `${msg}（请选其他空闲的打印机）`
        : isNotFound
        ? `打印机不存在（请刷新后重试）`
        : msg,
      duration: 6000,
    })
  } finally {
    submittingAssign.value = false
  }
}

const handleStart = async (taskId: string) => {
  try {
    await ElMessageBox.confirm(
      `确认开始打印任务 <b>${taskId}</b> 吗？<br><br><span style="color:var(--text-secondary);font-size:13px">打印开始后打印机状态会切换为"打印中"</span>`,
      '确认开始',
      {
        type: 'warning',
        center: true,
        confirmButtonText: '✓ 开始打印',
        cancelButtonText: '再等等',
        dangerouslyUseHTMLString: true,
      }
    )
  } catch {
    return
  }
  await taskStore.startPrint(taskId)
  ElNotification.success('已开始打印')
  fetchData()
}

const openFinish = (taskId: string) => {
  finishForm.taskId = taskId
  finishForm.actualWeight = 0
  finishForm.actualTime = 0
  finishForm.qualityScore = 5
  finishDialogVisible.value = true
}

const handleFinish = async () => {
  if (finishForm.actualWeight <= 0 || finishForm.actualTime <= 0) {
    ElNotification.warning('实际耗材和耗时必须大于 0')
    return
  }
  submittingFinish.value = true
  try {
    await taskStore.finishPrint(finishForm.taskId, {
      actualWeight: finishForm.actualWeight,
      actualTime: finishForm.actualTime,
      qualityScore: finishForm.qualityScore,
    })
    ElNotification.success({
      title: '✅ 打印完成',
      message: '已自动扣减库存 + 累计打印次数。请到【我的作品】手动登记作品（带照片 + 心得）',
      duration: 4000,
    })
    finishDialogVisible.value = false
    fetchData()
  } finally {
    submittingFinish.value = false
  }
}
</script>

<template>
  <div class="admin-task-active-page">
    <PageHeader title="进行中任务">
      <el-input
        v-model="searchKeyword"
        placeholder="按任务/学号/姓名搜索"
        clearable
        style="width: 220px"
        @keyup.enter="onSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="onSearch">搜索</el-button>
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <el-card v-loading="taskStore.loading">
      <template v-if="!taskStore.queue || taskStore.queue.list.length === 0">
        <EmptyState
          description="队列里没有进行中的任务"
          hint="所有任务都已完成或还在排队。等审批通过后任务会自动进入队列。"
        >
          <el-button @click="fetchData">刷新一下</el-button>
        </EmptyState>
      </template>

      <template v-else>
        <el-table :data="taskStore.queue.list" stripe>
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
          <el-table-column prop="materialType" label="材料" width="80" />
          <el-table-column label="打印机" width="140">
            <template #default="{ row }">
              <el-tooltip v-if="row.printerId" :content="`编号：${row.printerId}`" placement="top">
                <el-tag size="small" effect="dark">{{ row.printerModel || row.printerId }}</el-tag>
              </el-tooltip>
              <el-tag v-else type="warning" size="small" effect="dark">未分配</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="router.push(`/task/${row.taskId}`)">详情</el-button>
              <el-button text type="warning" @click="openPriority(row)">改优先级</el-button>
              <template v-if="row.status === TaskStatus.QUEUED || row.status === TaskStatus.APPROVED">
                <el-button v-if="!row.printerId" text type="primary" @click="openAssign(row.taskId)">分配</el-button>
                <el-button v-else text type="success" @click="handleStart(row.taskId)">开始</el-button>
              </template>
              <el-button v-if="row.status === TaskStatus.PRINTING" text type="success" @click="openFinish(row.taskId)">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>

    <!-- 分配打印机弹窗（下拉选择） -->
    <AppDialog v-model="assignDialogVisible" title="分配打印机" icon="Printer" width="520px" @confirm="handleAssign">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="任务编号">
          <el-tag size="large" type="info" effect="dark">{{ assignForm.taskId }}</el-tag>
        </el-form-item>
        <el-form-item label="选择打印机" required>
          <el-select
            v-model="assignForm.printerId"
            placeholder="请选择一台状态正常的打印机"
            filterable
            style="width: 100%"
            :loading="printerStore.loading"
          >
            <el-option
              v-for="p in printerStore.availableList"
              :key="p.printerId"
              :value="p.printerId"
              :label="`${p.printerId} - ${p.model}（${p.brand || '未知品牌'}）`"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-weight: 500">{{ p.printerId }} - {{ p.model }}</span>
                <span style="font-size: 12px; color: var(--text-secondary)">
                  {{ p.location || '未指定位置' }} · 累计 {{ p.totalPrintHours }}h
                </span>
              </div>
            </el-option>
          </el-select>
          <div v-if="printerStore.availableList.length === 0" class="empty-tip">
            ⚠️ 当前没有【正常】状态的打印机，请先在"打印机管理"里添加/恢复
          </div>
        </el-form-item>
        <el-form-item v-if="selectedPrinter" label="打印机详情">
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="品牌">{{ selectedPrinter.brand || '-' }}</el-descriptions-item>
            <el-descriptions-item label="喷嘴">{{ selectedPrinter.nozzleSize }}mm</el-descriptions-item>
            <el-descriptions-item label="成型尺寸">{{ selectedPrinter.buildVolume || '-' }}</el-descriptions-item>
            <el-descriptions-item label="累计">{{ selectedPrinter.totalPrintHours }} 小时 / {{ selectedPrinter.location || '未指定' }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
      </el-form>
    </AppDialog>

    <!-- 完成打印弹窗 -->
    <AppDialog v-model="finishDialogVisible" title="完成打印" icon="Check" width="520px" @confirm="handleFinish">
      <el-form :model="finishForm" label-width="100px">
        <el-form-item label="任务编号">
          <el-tag size="large" type="info" effect="dark">{{ finishForm.taskId }}</el-tag>
        </el-form-item>
        <el-form-item label="实际耗材 (g)" required>
          <el-input-number v-model="finishForm.actualWeight" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实际耗时 (分钟)" required>
          <el-input-number v-model="finishForm.actualTime" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="质量评分">
          <el-rate v-model="finishForm.qualityScore" :max="5" />
        </el-form-item>
        <el-alert type="warning" :closable="false" show-icon>
          完成后会自动：扣减库存 + 累计打印次数。<br />
          <b>作品需要用户手动登记</b>（到【我的作品】→ 登记作品上传照片 + 心得）
        </el-alert>
      </el-form>
    </AppDialog>

    <!-- 修改优先级弹窗（v2.2 新增） -->
    <AppDialog v-model="priorityDialogVisible" title="修改优先级" icon="Bell" type="warning" width="440px"
               confirm-text="保存" :loading="submittingPriority" @confirm="handlePriority">
      <el-form :model="priorityForm" label-width="80px">
        <el-form-item label="任务编号">
          <el-tag size="large" type="info" effect="dark">{{ priorityForm.taskId }}</el-tag>
        </el-form-item>
        <el-form-item label="任务标题">
          <span>{{ priorityForm.taskTitle }}</span>
        </el-form-item>
        <el-form-item label="优先级" required>
          <el-radio-group v-model="priorityForm.newPriority">
            <el-radio :value="Priority.URGENT" :label="Priority.URGENT">
              <el-tag type="danger" effect="dark" size="small">紧急</el-tag>
            </el-radio>
            <el-radio :value="Priority.NORMAL" :label="Priority.NORMAL">
              <el-tag type="primary" effect="dark" size="small">普通</el-tag>
            </el-radio>
            <el-radio :value="Priority.LOW" :label="Priority.LOW">
              <el-tag effect="dark" size="small">低优</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-task-active-page {
  padding: 0;
}
.empty-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #e6a23c;
}
</style>

<!-- 分配成功弹窗全局样式 -->
<style lang="scss">
.assign-success {
  text-align: center;
  padding: 4px 0;
  .success-icon-wrap {
    display: flex; justify-content: center; margin-bottom: 20px;
  }
  .success-icon-circle {
    width: 72px; height: 72px;
    background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
    border-radius: 50%;
    box-shadow: 0 8px 24px rgba(103, 194, 58, 0.3);
    display: flex; align-items: center; justify-content: center;
  }
  .success-icon-svg {
    width: 40px; height: 40px;
    stroke: white; stroke-width: 4;
    stroke-linecap: round; stroke-linejoin: round;
    fill: none;
  }
  .success-icon-circle-bg {
    stroke: rgba(255, 255, 255, 0.4);
    stroke-width: 2;
    stroke-dasharray: 166;
    stroke-dashoffset: 166;
    animation: assign-success-circle 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
  }
  .success-icon-check {
    stroke-dasharray: 48;
    stroke-dashoffset: 48;
    animation: assign-success-check 0.4s 0.5s cubic-bezier(0.65, 0, 0.45, 1) forwards;
  }
  @keyframes assign-success-circle { to { stroke-dashoffset: 0; } }
  @keyframes assign-success-check { to { stroke-dashoffset: 0; } }
  .success-title {
    margin: 0 0 8px;
    font-size: 22px;
    font-weight: 600;
    color: #303133;
  }
  .success-subtitle {
    margin: 0 0 12px;
    font-size: 14px;
    color: var(--text-regular);
  }
}
</style>