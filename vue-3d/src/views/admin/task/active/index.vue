<script setup lang="ts">
/**
 * 进行中任务（**B** - 管理端）
 *
 * 状态=3 排队中 + 4 打印中
 * 行内操作：分配打印机 / 开始打印 / 完成打印
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useTaskStore } from '@/stores/task'
import { TaskStatus } from '@/types/task'

const router = useRouter()
const taskStore = useTaskStore()

const fetchData = async () => {
  await taskStore.fetchQueue({ page: 1, size: 50 })
}
onMounted(fetchData)

const assignDialogVisible = ref(false)
const assignForm = reactive({ taskId: '', printerId: '' })

const finishDialogVisible = ref(false)
const finishForm = reactive({ taskId: '', actualWeight: 0, actualTime: 0, qualityScore: 5 })

const openAssign = (taskId: string) => {
  assignForm.taskId = taskId
  assignForm.printerId = ''
  assignDialogVisible.value = true
}

const handleAssign = async () => {
  if (!assignForm.printerId.trim()) {
    ElMessage.warning('请输入打印机编号')
    return
  }
  await taskStore.assignPrinter(assignForm.taskId, { printerId: assignForm.printerId })
  ElMessage.success('已分配打印机')
  assignDialogVisible.value = false
  fetchData()
}

const handleStart = async (taskId: string) => {
  await taskStore.startPrint(taskId)
  ElMessage.success('已开始打印')
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
    ElMessage.warning('实际耗材和耗时必须大于 0')
    return
  }
  await taskStore.finishPrint(finishForm.taskId, {
    actualWeight: finishForm.actualWeight,
    actualTime: finishForm.actualTime,
    qualityScore: finishForm.qualityScore,
  })
  ElMessage.success('已完成（已自动扣库存 + 归档作品库）')
  finishDialogVisible.value = false
  fetchData()
}
</script>

<template>
  <div class="admin-task-active-page">
    <PageHeader title="进行中任务">
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <el-card v-loading="taskStore.loading">
      <template v-if="!taskStore.queue || taskStore.queue.list.length === 0">
        <EmptyState description="队列里没有进行中的任务" />
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
          <el-table-column prop="applicantId" label="申请人" width="100" />
          <el-table-column prop="materialType" label="材料" width="80" />
          <el-table-column prop="printerId" label="打印机" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.printerId" size="small">{{ row.printerId }}</el-tag>
              <el-tag v-else type="warning" size="small">未分配</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="router.push(`/task/${row.taskId}`)">详情</el-button>
              <template v-if="row.status === TaskStatus.QUEUED">
                <el-button v-if="!row.printerId" text type="primary" @click="openAssign(row.taskId)">分配</el-button>
                <el-button v-else text type="success" @click="handleStart(row.taskId)">开始</el-button>
              </template>
              <el-button v-if="row.status === TaskStatus.PRINTING" text type="success" @click="openFinish(row.taskId)">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>

    <!-- 分配打印机弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="分配打印机" width="400px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="打印机编号">
          <el-input v-model="assignForm.printerId" placeholder="如 P-001 / P-002 / P-003" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 完成打印弹窗 -->
    <el-dialog v-model="finishDialogVisible" title="完成打印" width="400px">
      <el-form :model="finishForm" label-width="100px">
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
          完成后会自动：扣减库存 + 累计打印次数 + 归档作品库
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="finishDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFinish">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-task-active-page {
  padding: 0;
}
</style>