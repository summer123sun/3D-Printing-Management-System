<script setup lang="ts">
/**
 * 待审批任务（**B** - 管理端）
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useTaskStore } from '@/stores/task'
import { formatRelativeTime } from '@/utils/format'

const router = useRouter()
const taskStore = useTaskStore()

const rejectDialogVisible = ref(false)
const rejectForm = reactive({ taskId: '', approveComment: '' })

const fetchData = async () => {
  await taskStore.fetchPendingTasks({ page: 1, size: 50 })
}
onMounted(fetchData)

const handleApprove = async (taskId: string) => {
  await taskStore.approve(taskId)
  ElMessage.success('已审批通过')
  fetchData()
}

const openReject = (taskId: string) => {
  rejectForm.taskId = taskId
  rejectForm.approveComment = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  if (!rejectForm.approveComment.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  await taskStore.reject(rejectForm.taskId, { approveComment: rejectForm.approveComment })
  ElMessage.success('已驳回')
  rejectDialogVisible.value = false
  fetchData()
}
</script>

<template>
  <div class="admin-task-pending-page">
    <PageHeader title="待审批任务">
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <el-card v-loading="taskStore.loading">
      <template v-if="!taskStore.pendingTasks || taskStore.pendingTasks.list.length === 0">
        <EmptyState
          description="没有待审批任务"
          hint="所有提交的任务都已处理完。社员提交新任务后会出现在这里。"
        />
      </template>

      <template v-else>
        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
          共 <b>{{ taskStore.pendingTasks.total }}</b> 条待审批任务
        </el-alert>

        <el-table :data="taskStore.pendingTasks.list" stripe>
          <el-table-column prop="taskId" label="任务编号" width="180" />
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column label="申请人" width="100">
            <template #default="{ row }">{{ row.applicantId }}</template>
          </el-table-column>
          <el-table-column prop="materialType" label="材料" width="80" />
          <el-table-column prop="color" label="颜色" width="80" />
          <el-table-column label="优先级" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.priority === 1" type="danger" size="small">紧急</el-tag>
              <el-tag v-else-if="row.priority === 3" size="small">低优</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="140">
            <template #default="{ row }">{{ formatRelativeTime(row.applyTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click="router.push(`/task/${row.taskId}`)">详情</el-button>
              <el-button text type="success" @click="handleApprove(row.taskId)">通过</el-button>
              <el-button text type="danger" @click="openReject(row.taskId)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>

    <!-- 驳回弹窗 -->
    <AppDialog v-model="rejectDialogVisible" title="驳回任务" icon="Warning" type="danger" width="480px"
               confirm-text="确认驳回" @confirm="handleReject">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectForm.approveComment" type="textarea" :rows="3" placeholder="必填，告知申请人修改方向" />
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-task-pending-page {
  padding: 0;
}
</style>