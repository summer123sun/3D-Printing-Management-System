<script setup lang="ts">
/**
 * 项目列表（统一版 v2.2 round 4）
 * v2.3 重构：成员端表格 -> 卡片网格
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { Plus, Calendar, User, View, Edit, Check, CircleClose } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import HeroBanner from '@/components/member/HeroBanner.vue'
import MemberCard from '@/components/member/MemberCard.vue'
import { useProjectStore } from '@/stores/project'
import { useAuthStore } from '@/stores/auth'
import { useMemberStyle } from '@/composables/useMemberStyle'
import { ProjectStatusText, ProjectTypeText, ProjectStatus, ProjectType, type ProjectCreateDTO } from '@/types/project'
import { Role } from '@/utils/enum'
import { formatDate } from '@/utils/format'

const router = useRouter()
const projectStore = useProjectStore()
const authStore = useAuthStore()
const { isMember } = useMemberStyle()

const activeTab = ref<'mine' | 'all'>('all')
const filter = ref({
  status: undefined as number | undefined,
  keyword: '',
})

const isStaff = computed(() => {
  const r = authStore.user?.role
  return r === Role.PRESIDENT || r === Role.TECH_LEAD
})
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

const projectList = computed(() => projectStore.projectList?.list ?? [])

// 状态色
const statusMeta = (status: number) => {
  const colors: Record<number, { label: string; bg: string; color: string }> = {
    [ProjectStatus.PREPARING]: { label: '筹备中', bg: '#E8EEF5', color: '#0A2540' },
    [ProjectStatus.RUNNING]: { label: '进行中', bg: '#E0FAF4', color: '#00A88A' },
    [ProjectStatus.DONE]: { label: '已完成', bg: '#DCFCE7', color: '#15803D' },
    [ProjectStatus.CANCELLED]: { label: '已取消', bg: '#F4F4F5', color: '#71717A' },
  }
  return colors[status] || colors[ProjectStatus.RUNNING]!
}
const statusStyle = (status: number) => {
  const m = statusMeta(status)
  return { background: m.bg, color: m.color }
}
</script>

<template>
  <div class="project-list-page">
    <!-- 成员端 HeroBanner -->
    <HeroBanner
      v-if="isMember"
      title="项目中心"
      :subtitle="activeTab === 'mine' ? '我参与的项目' : '浏览全部项目 · 找到你想加入的那个'"
    >
      <template #actions>
        <el-input
          v-model="filter.keyword"
          placeholder="按名称搜索"
          clearable
          round
          size="large"
          style="width: 240px"
          @keyup.enter="fetchData"
          @clear="fetchData"
        />
        <el-button type="primary" size="large" round @click="router.push('/project/create')">
          <el-icon><Plus /></el-icon> 立项新项目
        </el-button>
      </template>
    </HeroBanner>

    <!-- 后台端 PageHeader -->
    <PageHeader v-else title="项目管理">
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

    <!-- Tab 切换 -->
    <MemberCard v-if="isMember" padding="20px">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部项目" name="all" />
        <el-tab-pane label="我参与的" name="mine" />
      </el-tabs>
    </MemberCard>

    <el-card v-else>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部项目" name="all" />
        <el-tab-pane label="我参与的" name="mine" />
      </el-tabs>
    </el-card>

    <!-- ============== 成员端：卡片网格 ============== -->
    <template v-if="isMember">
      <MemberCard v-loading="projectStore.loading" padding="32px">
        <template v-if="projectList.length === 0">
          <EmptyState
            :description="activeTab === 'mine' ? '你还没参与任何项目' : '还没有项目记录'"
            :hint="activeTab === 'mine' ? '请让项目负责人把你加入项目，或切换到【全部项目】看看。' : '立项第一个项目，把分散的打印任务组织起来。'"
          >
            <el-button v-if="activeTab === 'all'" type="primary" round size="large" @click="router.push('/project/create')">
              <el-icon><Plus /></el-icon> 立项新项目
            </el-button>
            <el-button v-else round size="large" @click="activeTab = 'all'">查看全部项目</el-button>
          </EmptyState>
        </template>

        <el-row v-else :gutter="20">
          <el-col v-for="proj in projectList" :key="proj.projectId" :xs="24" :sm="12" :md="8" :lg="8" :xl="6">
            <MemberCard hoverable padding="0" :radius="16" @click="router.push(`/project/${proj.projectId}`)">
              <!-- 封面 banner -->
              <div class="project-cover" :class="'type-' + proj.projectType">
                <span class="project-type-tag">{{ ProjectTypeText[proj.projectType as keyof typeof ProjectTypeText] }}</span>
                <el-image
                  v-if="proj.coverImage"
                  :src="proj.coverImage"
                  fit="cover"
                  class="cover-image"
                >
                  <template #error>
                    <div class="cover-placeholder">
                      <span class="placeholder-icon">📁</span>
                      <span class="placeholder-text">项目封面</span>
                    </div>
                  </template>
                </el-image>
                <div v-else class="cover-placeholder">
                  <span class="placeholder-icon">📁</span>
                </div>
              </div>
              <!-- 内容区 -->
              <div class="project-content">
                <h3 class="project-name">{{ proj.projectName }}</h3>
                <div class="project-status-row">
                  <span class="status-badge" :style="statusStyle(proj.status)">
                    {{ statusMeta(proj.status).label }}
                  </span>
                </div>
                <div class="project-meta">
                  <div class="meta-item">
                    <el-icon :size="14"><User /></el-icon>
                    <span>{{ proj.leaderName || proj.leaderId }}</span>
                  </div>
                  <div class="meta-item">
                    <el-icon :size="14"><Calendar /></el-icon>
                    <span>{{ formatDate(proj.startDate, 'YYYY-MM-DD') }}</span>
                  </div>
                </div>
              </div>
            </MemberCard>
          </el-col>
        </el-row>
      </MemberCard>
    </template>

    <!-- ============== 后台端：保留原表格 ============== -->
    <template v-else>
      <el-card>
        <div v-loading="projectStore.loading">
          <template v-if="projectList.length === 0">
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
          <el-table v-else :data="projectList" stripe>
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
                <el-button text type="primary" size="small" @click="router.push(`/project/${row.projectId}`)">查看</el-button>
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
    </template>

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
  </div>
</template>

<script lang="ts">
function statusTagType(status: number): 'info' | 'primary' | 'success' | 'danger' {
  return status === ProjectStatus.PREPARING ? 'info' : status === ProjectStatus.RUNNING ? 'primary' : status === ProjectStatus.DONE ? 'success' : 'danger'
}
export default { name: 'ProjectListPage' }
</script>

<style lang="scss" scoped>
.project-list-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-large;
}

// ============ 成员端：项目卡片 ============
.project-cover {
  position: relative;
  width: 100%;
  height: 140px;
  border-radius: 16px 16px 0 0;
  overflow: hidden;
  background: linear-gradient(135deg, #0A2540 0%, #1E3A5F 100%);

  &.type-1 { background: linear-gradient(135deg, #0A2540 0%, #00A88A 100%); }
  &.type-2 { background: linear-gradient(135deg, #F2A93B 0%, #CCB000 100%); }
  &.type-3 { background: linear-gradient(135deg, #DC2626 0%, #F472B6 100%); }
  &.type-4 { background: linear-gradient(135deg, #6366F1 0%, #0A2540 100%); }
}
.cover-image {
  width: 100%;
  height: 100%;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0A2540 0%, #1E3A5F 100%);
}
.placeholder-icon {
  font-size: 48px;
  opacity: 0.5;
}
.project-type-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.95);
  color: #0A2540;
  font-size: 12px;
  font-weight: 600;
  border-radius: 12px;
  backdrop-filter: blur(8px);
}
.project-content {
  padding: 16px 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.project-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.project-status-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}
.project-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 4px;
  font-size: 12px;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
}
</style>
