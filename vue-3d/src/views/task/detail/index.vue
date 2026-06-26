<script setup lang="ts">
/**
 * 任务详情（**B** - 社员端）
 *
 * 所有人能看；只有申请人能取件/取消，技术骨干能审批/分配/开始/完成
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import TaskTimeline from '@/components/task/detail/TaskTimeline.vue'
import { useTaskStore } from '@/stores/task'
import { useAuthStore } from '@/stores/auth'
import { TaskStatus, TaskStatusText } from '@/types/task'
import { formatDate, formatWeight, formatDuration, formatFileSize } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const taskStore = useTaskStore()
const authStore = useAuthStore()

const taskId = computed(() => route.params.id as string)

const isMine = computed(() => taskStore.currentTask?.applicantId === authStore.user?.studentId)
const isStaff = computed(() => {
  const role = authStore.user?.role ?? 0
  return role === 1 || role === 2
})
const canPickup = computed(() => isMine.value && taskStore.currentTask?.status === TaskStatus.DONE && !taskStore.currentTask?.pickupTime)
const canCancel = computed(() => isMine.value && (taskStore.currentTask?.status === TaskStatus.PENDING || taskStore.currentTask?.status === TaskStatus.QUEUED))

const rejectDialogVisible = ref(false)
const rejectForm = ref({ approveComment: '' })

const finishDialogVisible = ref(false)
const finishForm = ref({ actualWeight: 0, actualTime: 0, qualityScore: 5 })

const assignDialogVisible = ref(false)
const assignForm = ref({ printerId: '' })

const fetchData = async () => {
  await taskStore.fetchTaskDetail(taskId.value)
}
onMounted(fetchData)

// 操作
const handleApprove = async () => {
  await taskStore.approve(taskId.value)
  ElMessage.success('审批通过')
  fetchData()
}

const handleReject = async () => {
  if (!rejectForm.value.approveComment.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  await taskStore.reject(taskId.value, rejectForm.value)
  ElMessage.success('已驳回')
  rejectDialogVisible.value = false
  rejectForm.value = { approveComment: '' }
  fetchData()
}

const handleAssign = async () => {
  if (!assignForm.value.printerId.trim()) {
    ElMessage.warning('请输入打印机编号')
    return
  }
  await taskStore.assignPrinter(taskId.value, assignForm.value)
  ElMessage.success('已分配打印机')
  assignDialogVisible.value = false
  fetchData()
}

const handleStart = async () => {
  await ElMessageBox.confirm('确认开始打印？', '提示', { type: 'warning' })
  await taskStore.startPrint(taskId.value)
  ElMessage.success('已开始打印')
  fetchData()
}

const handleFinish = async () => {
  if (finishForm.value.actualWeight <= 0 || finishForm.value.actualTime <= 0) {
    ElMessage.warning('实际耗材和耗时必须大于 0')
    return
  }
  await taskStore.finishPrint(taskId.value, finishForm.value)
  ElMessage.success('已完成（已自动扣库存 + 归档作品库）')
  finishDialogVisible.value = false
  fetchData()
}

const handlePickup = async () => {
  await ElMessageBox.confirm('确认取件？', '提示', { type: 'warning' })
  await taskStore.pickup(taskId.value)
  ElMessage.success('签收成功')
  fetchData()
}

const handleCancel = async () => {
  await ElMessageBox.confirm('确认取消此任务？', '提示', { type: 'warning' })
  await taskStore.cancel(taskId.value)
  ElMessage.success('已取消')
  fetchData()
}
</script>

<template>
  <div class="task-detail-page" v-loading="taskStore.loading">
    <PageHeader :title="taskStore.currentTask?.title || '任务详情'" :show-back="true" @back="router.back()">
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <template v-if="taskStore.currentTask">
      <el-row :gutter="16">
        <!-- 左：基本信息 + 时间线 -->
        <el-col :xs="24" :md="14">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
                <StatusTag status-type="task" :status="taskStore.currentTask.status" />
              </div>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="任务编号">{{ taskStore.currentTask.taskId }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ taskStore.currentTask.applicantId }}</el-descriptions-item>
              <el-descriptions-item label="模型名称">{{ taskStore.currentTask.modelName }}</el-descriptions-item>
              <el-descriptions-item label="打印机">{{ taskStore.currentTask.printerId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="材料">{{ taskStore.currentTask.materialType }} / {{ taskStore.currentTask.color || '-' }}</el-descriptions-item>
              <el-descriptions-item label="层高/填充/支撑">
                {{ taskStore.currentTask.layerHeight }}mm / {{ taskStore.currentTask.infillRate }}% / {{ taskStore.currentTask.needSupport ? '是' : '否' }}
              </el-descriptions-item>
              <el-descriptions-item label="预估重量" v-if="taskStore.currentTask.estWeight">{{ formatWeight(taskStore.currentTask.estWeight) }}</el-descriptions-item>
              <el-descriptions-item label="预估耗时" v-if="taskStore.currentTask.estTime">{{ formatDuration(taskStore.currentTask.estTime) }}</el-descriptions-item>
              <el-descriptions-item v-if="taskStore.currentTask.actualWeight" label="实际耗材">{{ formatWeight(taskStore.currentTask.actualWeight) }}</el-descriptions-item>
              <el-descriptions-item v-if="taskStore.currentTask.actualTime" label="实际耗时">{{ formatDuration(taskStore.currentTask.actualTime) }}</el-descriptions-item>
              <el-descriptions-item v-if="taskStore.currentTask.qualityScore" label="质量评分">
                <el-rate v-model="taskStore.currentTask.qualityScore" disabled show-score />
              </el-descriptions-item>
            </el-descriptions>

            <el-divider>状态流转</el-divider>
            <TaskTimeline :task="taskStore.currentTask" />
          </el-card>
        </el-col>

        <!-- 右：操作区 -->
        <el-col :xs="24" :md="10">
          <el-card>
            <template #header><span>操作</span></template>

            <!-- 申请人操作 -->
            <template v-if="isMine">
              <el-button v-if="canPickup" type="success" style="width:100%; margin-bottom: 8px" @click="handlePickup">
                <el-icon><Box /></el-icon> 取件签到
              </el-button>
              <el-button v-if="canCancel" type="danger" plain style="width:100%; margin-bottom: 8px" @click="handleCancel">
                取消任务
              </el-button>
              <EmptyState v-if="!canPickup && !canCancel" description="暂无可执行的操作" :show-icon="false" />
            </template>

            <!-- 技术骨干操作 -->
            <template v-if="isStaff && taskStore.currentTask.status === TaskStatus.PENDING">
              <el-button type="success" style="width:100%; margin-bottom: 8px" @click="handleApprove">
                <el-icon><Check /></el-icon> 审批通过
              </el-button>
              <el-button type="danger" plain style="width:100%; margin-bottom: 8px" @click="rejectDialogVisible = true">
                驳回
              </el-button>
            </template>

            <template v-if="isStaff && taskStore.currentTask.status === TaskStatus.QUEUED">
              <el-button type="primary" style="width:100%; margin-bottom: 8px" @click="assignDialogVisible = true">
                分配打印机
              </el-button>
              <el-button v-if="taskStore.currentTask.printerId" type="success" @click="handleStart">
                开始打印
              </el-button>
            </template>

            <template v-if="isStaff && taskStore.currentTask.status === TaskStatus.PRINTING">
              <el-button type="success" style="width:100%" @click="finishDialogVisible = true">
                标记完成
              </el-button>
            </template>

            <EmptyState v-if="!isMine && !isStaff" description="您是普通社员，只能查看" :show-icon="false" />
            <EmptyState v-else-if="isStaff && taskStore.currentTask.status >= TaskStatus.DONE" description="任务已结束" :show-icon="false" />
          </el-card>

          <!-- STL 文件 -->
          <el-card style="margin-top: 16px" v-if="taskStore.currentTask.stlFilePath">
            <template #header><span>STL 文件</span></template>
            <el-link :href="taskStore.currentTask.stlFilePath" target="_blank">
              {{ taskStore.currentTask.stlFilePath.split('/').pop() }}
            </el-link>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回任务" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectForm.approveComment" type="textarea" :rows="3" placeholder="必填，告知申请人修改方向" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认驳回</el-button>
      </template>
    </el-dialog>

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

<script lang="ts">
import { Box, Check } from '@element-plus/icons-vue'
export default { name: 'TaskDetailPage' }
</script>

<style lang="scss" scoped>
.task-detail-page {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>