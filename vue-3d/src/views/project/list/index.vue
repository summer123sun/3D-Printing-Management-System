<script setup lang="ts">
/**
 * 项目列表（统一版 v2.2 round 4）
 *
 * ✅ 合并前：
 *   - 顶级"项目管理"下有"项目列表（社员端）"和"项目管理（管理端）"两个菜单
 *   - 列表有"查看"按钮（任何人可见）
 *   - 管理端有"编辑/完成/取消"按钮（仅 admin + 项目负责人）
 *
 * ✅ 合并后（用户反馈）：
 *   - 顶级"项目管理"下只有"项目列表"一个菜单
 *   - 列表里按角色显示操作按钮：
 *     - 所有人都有"查看"
 *     - 社长 + 技术骨干 + 项目负责人 → 有"编辑/完成/取消"
 *     - 已取消/已完成的 → 隐藏对应按钮
 *
 * Tab：我参与的 / 全部（保留原社员端能力）
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useProjectStore } from '@/stores/project'
import { useAuthStore } from '@/stores/auth'
import { ProjectStatusText, ProjectTypeText, ProjectStatus, ProjectType, type ProjectCreateDTO } from '@/types/project'
import { Role } from '@/utils/enum'
import { formatDate } from '@/utils/format'

const router = useRouter()
const projectStore = useProjectStore()
const authStore = useAuthStore()

const activeTab = ref<'mine' | 'all'>('all')
const filter = ref({
  status: undefined as number | undefined,
  keyword: '',
})

// ============== 角色判断 ==============
const isStaff = computed(() => {
  const r = authStore.user?.role
  return r === Role.PRESIDENT || r === Role.TECH_LEAD
})
// 操作列：社长 + 技术骨干 + 项目负责人 可看到"编辑/完成/取消"
const canOperate = (row: any) => {
  if (!authStore.user) return false
  if (isStaff.value) return true
  return row.leaderId === authStore.user.studentId
}

const fetchData = async () => {
  await projectStore.fetchList({
    page: 1,
    size: 20,
    scope: activeTab.value,
    status: filter.value.status,
    keyword: filter.value.keyword || undefined,
  })
}
onMounted(fetchData)

const onTabChange = () => {
  filter.value.keyword = ''
  fetchData()
}

// ============== 操作：完成/取消 ==============
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

// ============== 操作：编辑（弹窗） ==============
const editDialogVisible = ref(false)
const submittingEdit = ref(false)
const editForm = reactive<ProjectCreateDTO & { projectId: number }>({
  projectId: 0,
  projectName: '',
  projectType: ProjectType.CREATION,
  startDate: '',
  endDate: '',
  budget: 0,
  description: '',
  deliverables: '',
  coverImage: '',
})

const openEditDialog = async (row: any) => {
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
</script>

<template>
  <div class="project-list-page">
    <PageHeader title="项目">
      <el-input
        v-model="filter.keyword"
        placeholder="按名称搜索"
        clearable
        style="width: 200px"
        @keyup.enter="fetchData"
        @clear="fetchData"
      />
      <el-button @click="fetchData">搜索</el-button>
    </PageHeader>

    <el-card>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部项目" name="all" />
        <el-tab-pane label="我参与的" name="mine" />
      </el-tabs>

      <div v-loading="projectStore.loading">
        <template v-if="!projectStore.projectList || projectStore.projectList.list.length === 0">
          <EmptyState
            :description="activeTab === 'mine' ? '你还没参与任何项目' : '还没有项目'"
            :hint="activeTab === 'mine' ? '请联系管理员把你加入项目，或切换到【全部项目】看看。' : '立项第一个项目，把分散的打印任务组织起来。'"
          >
            <el-button v-if="activeTab === 'all'" type="primary" @click="router.push('/project/create')">
              <el-icon><Plus /></el-icon> 创建项目
            </el-button>
            <el-button v-else @click="activeTab = 'all'">查看全部项目</el-button>
          </EmptyState>
        </template>

        <el-table
          v-else
          :data="projectStore.projectList.list"
          stripe
        >
          <el-table-column prop="projectId" label="ID" width="80" />
          <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              {{ ProjectTypeText[row.projectType as keyof typeof ProjectTypeText] }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTagType(row.status)" effect="dark">
                {{ ProjectStatusText[row.status as keyof typeof ProjectStatusText] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="负责人" width="120">
            <template #default="{ row }">
              <el-tooltip :content="`学号：${row.leaderId}`" placement="top">
                <span>{{ row.leaderName || row.leaderId }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="开始日期" width="120">
            <template #default="{ row }">{{ formatDate(row.startDate, 'YYYY-MM-DD') }}</template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <!-- 查看：所有人都有 -->
              <el-button text type="primary" size="small" @click="router.push(`/project/${row.projectId}`)">查看</el-button>
              <!-- 编辑/完成/取消：仅社长 + 技术骨干 + 项目负责人 -->
              <template v-if="canOperate(row)">
                <el-button text type="warning" size="small" @click="openEditDialog(row)">编辑</el-button>
                <el-button v-if="row.status === ProjectStatus.RUNNING" text type="success" size="small" @click="handleComplete(row.projectId)">完成</el-button>
                <el-button v-if="row.status !== ProjectStatus.DONE && row.status !== ProjectStatus.CANCELLED" text type="danger" size="small" @click="handleCancel(row.projectId)">取消</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>

  <!-- 编辑项目弹窗 -->
  <AppDialog
    v-model="editDialogVisible"
    title="编辑项目"
    icon="Edit"
    type="warning"
    width="560px"
    confirm-text="保存"
    :loading="submittingEdit"
    @confirm="handleEditConfirm"
  >
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
function statusTagType(status: number): 'info' | 'primary' | 'success' | 'danger' {
  return status === ProjectStatus.PREPARING ? 'info' : status === ProjectStatus.RUNNING ? 'primary' : status === ProjectStatus.DONE ? 'success' : 'danger'
}
export default { name: 'ProjectListPage' }
</script>

<style lang="scss" scoped>
.project-list-page {
  padding: 0;
}
</style>
