<script setup lang="ts">
/**
 * 项目管理列表（**B** - 管理端）
 *
 * 显示所有项目 + 行内操作（查看/编辑/完成/取消）
 */
import { onMounted, reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useProjectStore } from '@/stores/project'
import { useAuthStore } from '@/stores/auth'
import { ProjectStatusText, ProjectTypeText, ProjectStatus, ProjectType, type ProjectCreateDTO } from '@/types/project'
import { formatDate } from '@/utils/format'

const router = useRouter()
const projectStore = useProjectStore()
const authStore = useAuthStore()  // ✅ v2.2 提到顶部给 canEdit 用

// ✅ 能否看到「编辑/完成/取消」按钮：只有社长（role=1）+ 技术骨干（role=2）
const canEdit = computed(() => authStore.isTechLead)  // isTechLead 已包含 president 和 tech_lead

const filter = ref({
  status: undefined as number | undefined,
  keyword: '',
})

const fetchData = async () => {
  await projectStore.fetchList({
    page: 1,
    size: 50,
    status: filter.value.status,
    keyword: filter.value.keyword || undefined,
  })
}
onMounted(fetchData)

const handleComplete = async (id: number) => {
  await projectStore.complete(id)
  ElMessage.success('已标记完成')
  fetchData()
}

const handleCancel = async (id: number) => {
  await projectStore.cancel(id)
  ElMessage.success('已取消')
  fetchData()
}

// ===== v2.2 编辑项目弹窗 =====
const editDialogVisible = ref(false)
const editForm = reactive<ProjectCreateDTO & { projectId: number }>({
  projectId: 0,
  projectName: '',
  projectType: ProjectType.CREATION,  // ✅ 默认「作品创作」（ProjectType 没 OTHER；只有 CREATION/COMPETE/ORDER/ACTIVITY）
  startDate: '',
  endDate: '',
  budget: 0,
  description: '',
  deliverables: '',
  coverImage: '',
})
const submittingEdit = ref(false)

const openEditDialog = async (row: any) => {
  // ✅ 按钮已经 v-if 隐藏了，这里只做"项目级越权防御"（非本人项目不可编辑）
  const userId = authStore.user?.studentId
  const isStaff = authStore.isTechLead
  const isProjectLeader = row.leaderId === userId
  if (!isStaff && !isProjectLeader) {
    ElNotification.warning('只有项目负责人/社长/技术骨干可以编辑此项目')
    return
  }
  // 拉取最新详情填充表单
  try {
    const detail = await projectStore.fetchDetail(row.projectId)
    editForm.projectId = row.projectId
    editForm.projectName = detail.project.projectName
    editForm.projectType = detail.project.projectType
    editForm.startDate = detail.project.startDate || ''
    editForm.endDate = detail.project.endDate || ''
    editForm.budget = detail.project.budget || 0
    editForm.description = detail.project.description || ''
    editForm.deliverables = detail.project.deliverables || ''
    editForm.coverImage = detail.project.coverImage || ''
    editDialogVisible.value = true
  } catch (e) {
    console.error('[编辑项目] 拉取详情失败：', e)
  }
}

const handleEditConfirm = async () => {
  if (!editForm.projectName?.trim()) {
    ElNotification.warning('项目名称不能为空')
    return
  }
  submittingEdit.value = true
  try {
    await projectStore.update(editForm.projectId, {
      projectName: editForm.projectName.trim(),
      projectType: editForm.projectType,
      startDate: editForm.startDate || undefined,
      endDate: editForm.endDate || undefined,
      budget: editForm.budget || 0,
      description: editForm.description || '',
      deliverables: editForm.deliverables || '',
      coverImage: editForm.coverImage || '',
    } as ProjectCreateDTO)
    ElNotification.success('项目已更新')
    editDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error('[编辑项目] 失败：', e)
  } finally {
    submittingEdit.value = false
  }
}

const statusTagType = (s: number) => {
  return s === ProjectStatus.PREPARING ? 'info' : s === ProjectStatus.RUNNING ? 'primary' : s === ProjectStatus.DONE ? 'success' : 'danger'
}
</script>

<template>
  <div class="admin-project-manage-page">
    <PageHeader title="项目管理">
      <el-button type="primary" @click="router.push('/admin/project/create')">
        <el-icon><Plus /></el-icon> 创建项目
      </el-button>
      <el-input v-model="filter.keyword" placeholder="按名称搜索" clearable style="width: 200px" @keyup.enter="fetchData" @clear="fetchData" />
      <el-button @click="fetchData">搜索</el-button>
    </PageHeader>

    <el-card v-loading="projectStore.loading">
      <template v-if="!projectStore.projectList || projectStore.projectList.list.length === 0">
        <EmptyState
          description="还没有项目"
          hint="立项你的第一个项目，把分散的任务组织起来。"
        >
          <el-button type="primary" @click="router.push('/project/create')">
            <el-icon><Plus /></el-icon> 创建项目
          </el-button>
        </EmptyState>
      </template>

      <template v-else>
        <el-table :data="projectStore.projectList.list" stripe>
          <el-table-column prop="projectId" label="ID" width="80" />
          <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              {{ ProjectTypeText[row.projectType as keyof typeof ProjectTypeText] }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTagType(row.status)" effect="dark">{{ ProjectStatusText[row.status as keyof typeof ProjectStatusText] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="负责人" width="120">
            <template #default="{ row }">
              <el-tooltip v-if="row.leaderName" :content="`学号：${row.leaderId}`" placement="top">
                <span class="leader-name">{{ row.leaderName }}</span>
              </el-tooltip>
              <span v-else class="leader-id">{{ row.leaderId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="开始日期" width="120">
            <template #default="{ row }">{{ formatDate(row.startDate, 'YYYY-MM-DD') }}</template>
          </el-table-column>
          <el-table-column label="预算/实际" width="120">
            <template #default="{ row }">
              {{ row.budget || 0 }} / {{ row.actualCost || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <!-- 查看：所有登录用户都能看 -->
              <el-button text type="primary" size="small" @click="router.push(`/project/${row.projectId}`)">查看</el-button>
              <!-- 以下三按钮：只有社长 + 技术骨干可见（普通社员/新成员完全不展示） -->
              <template v-if="canEdit">
                <el-button text type="warning" size="small" @click="openEditDialog(row)">编辑</el-button>
                <el-button v-if="row.status === ProjectStatus.RUNNING" text type="success" size="small" @click="handleComplete(row.projectId)">完成</el-button>
                <el-button v-if="row.status !== ProjectStatus.DONE" text type="danger" size="small" @click="handleCancel(row.projectId)">取消</el-button>
              </template>
              <!-- 否则给个占位 -->
              <span v-else class="readonly-tip" title="仅社长/技术骨干可操作">
                <el-icon><Lock /></el-icon>
              </span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>

  <!-- 编辑项目弹窗（v2.2 新增） -->
  <AppDialog v-model="editDialogVisible" title="编辑项目" icon="Edit" type="warning" width="560px"
             confirm-text="保存" :loading="submittingEdit" @confirm="handleEditConfirm">
    <el-form :model="editForm" label-width="100px">
      <el-form-item label="项目名称" required>
        <el-input v-model="editForm.projectName" placeholder="项目名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="项目类型">
        <el-select v-model="editForm.projectType" style="width: 100%">
          <el-option v-for="(label, value) in ProjectTypeText" :key="value" :label="label" :value="Number(value)" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期">
        <el-input v-model="editForm.startDate" placeholder="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-input v-model="editForm.endDate" placeholder="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="预算（元）">
        <el-input-number v-model="editForm.budget" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="项目简介" />
      </el-form-item>
      <el-form-item label="交付物">
        <el-input v-model="editForm.deliverables" type="textarea" :rows="2" placeholder="预期交付" />
      </el-form-item>
      <el-alert type="warning" :closable="false" show-icon style="margin-top: 8px">
        仅可修改基础信息，阶段和成员请到项目详情页操作
      </el-alert>
    </el-form>
  </AppDialog>
</template>

<script lang="ts">
import { Plus } from '@element-plus/icons-vue'
export default { name: 'AdminProjectManagePage' }
</script>

<style lang="scss" scoped>
.admin-project-manage-page {
  padding: 0;
}
.leader-name {
  color: var(--text-primary);
  font-weight: 500;
}
.leader-id {
  color: var(--text-secondary);
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
}
// 只读提示（普通社员/新成员看到的"锁"图标）
.readonly-tip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-placeholder, #999);
  cursor: help;
  .el-icon {
    font-size: 14px;
  }
}
</style>