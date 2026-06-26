<script setup lang="ts">
/**
 * 项目详情（**B** - 社员端）
 *
 * 聚合 4 张表 + 关联任务 + 阶段时间线
 * 项目负责人可编辑阶段/成员/完成/取消
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { useProjectStore } from '@/stores/project'
import { useAuthStore } from '@/stores/auth'
import {
  ProjectStatus, ProjectStatusText,
  ProjectRole, ProjectRoleText,
  StageStatus, StageStatusText,
  ProjectMemberStatus,
} from '@/types/project'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const authStore = useAuthStore()

const projectId = computed(() => Number(route.params.id))

const isLeader = computed(() => projectStore.currentProject?.project.leaderId === authStore.user?.studentId)
const isStaff = computed(() => {
  const role = authStore.user?.role ?? 0
  return role === 1 || role === 2
})

const activeTab = ref<'overview' | 'members' | 'stages' | 'files' | 'tasks'>('overview')

const stageStatusDialogVisible = ref(false)
const stageStatusForm = ref({ progressId: 0, status: 0 })

const memberDialogVisible = ref(false)
const memberForm = ref({ memberId: '', roleInProject: ProjectRole.PARTICIPANT, contribution: '' })

const fetchData = async () => {
  await projectStore.fetchDetail(projectId.value)
}
onMounted(fetchData)

const handleComplete = async () => {
  await ElMessageBox.confirm('确认标记项目完成？此操作不可撤销', '提示', { type: 'warning' })
  await projectStore.complete(projectId.value)
  ElMessage.success('项目已完成')
  fetchData()
}

const handleCancel = async () => {
  await ElMessageBox.confirm('确认取消项目？此操作不可撤销', '提示', { type: 'warning' })
  await projectStore.cancel(projectId.value)
  ElMessage.success('项目已取消')
  fetchData()
}

const openAddMember = () => {
  memberForm.value = { memberId: '', roleInProject: ProjectRole.PARTICIPANT, contribution: '' }
  memberDialogVisible.value = true
}

const handleAddMember = async () => {
  if (!memberForm.value.memberId.trim()) {
    ElMessage.warning('请填写成员学号')
    return
  }
  await projectStore.addMember(projectId.value, memberForm.value)
  ElMessage.success('已添加成员')
  memberDialogVisible.value = false
  fetchData()
}

const handleRemoveMember = async (mid: string) => {
  await ElMessageBox.confirm(`确认移除成员 ${mid}？`, '提示', { type: 'warning' })
  await projectStore.removeMember(projectId.value, mid)
  ElMessage.success('已移除')
  fetchData()
}

const openStageStatus = (pid: number, status: number) => {
  stageStatusForm.value = { progressId: pid, status }
  stageStatusDialogVisible.value = true
}

const handleUpdateStageStatus = async () => {
  await projectStore.updateStageStatus(projectId.value, stageStatusForm.value.progressId, stageStatusForm.value.status)
  ElMessage.success('阶段状态已更新')
  stageStatusDialogVisible.value = false
  fetchData()
}

const memberStatusText = (s: number) => {
  return s === ProjectMemberStatus.ACTIVE ? '进行中' : s === ProjectMemberStatus.QUIT ? '已退出' : '已完成'
}

const stageStatusTagType = (s: number) => {
  return s === StageStatus.PENDING ? 'info' : s === StageStatus.RUNNING ? 'primary' : 'success'
}

const memberRoleTagType = (r: number): 'danger' | 'warning' | 'primary' => {
  return r === ProjectRole.LEADER ? 'danger' : r === ProjectRole.CORE ? 'warning' : 'primary'
}
</script>

<template>
  <div class="project-detail-page" v-loading="projectStore.loading">
    <PageHeader
      :title="projectStore.currentProject?.project.projectName || '项目详情'"
      :show-back="true"
      @back="router.back()"
    >
      <el-button v-if="isLeader && projectStore.currentProject?.project.status === ProjectStatus.RUNNING" type="success" @click="handleComplete">
        标记完成
      </el-button>
      <el-button v-if="isLeader && projectStore.currentProject?.project.status !== ProjectStatus.DONE" type="danger" plain @click="handleCancel">
        取消项目
      </el-button>
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <template v-if="projectStore.currentProject">
      <!-- 项目头部卡片 -->
      <el-card class="project-header-card">
        <div class="header-content">
          <div class="left">
            <el-image
              v-if="projectStore.currentProject.project.coverImage"
              :src="projectStore.currentProject.project.coverImage"
              fit="cover"
              class="cover"
            />
            <div v-else class="cover-placeholder">📁</div>
          </div>
          <div class="right">
            <h2>{{ projectStore.currentProject.project.projectName }}</h2>
            <p class="meta">
              <el-tag size="small">{{ ProjectStatusText[projectStore.currentProject.project.status] }}</el-tag>
              <span>负责人：{{ projectStore.currentProject.project.leaderId }}</span>
              <span>开始：{{ formatDate(projectStore.currentProject.project.startDate, 'YYYY-MM-DD') }}</span>
            </p>
            <p v-if="projectStore.currentProject.project.description" class="description">
              {{ projectStore.currentProject.project.description }}
            </p>
          </div>
        </div>
      </el-card>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="概览" name="overview" />
        <el-tab-pane :label="`成员 (${projectStore.currentProject.members.length})`" name="members" />
        <el-tab-pane :label="`阶段 (${projectStore.currentProject.stages.length})`" name="stages" />
        <el-tab-pane :label="`文件 (${projectStore.currentProject.files.length})`" name="files" />
        <el-tab-pane :label="`关联任务 (${projectStore.currentProject.relatedTasks.length})`" name="tasks" />
      </el-tabs>

      <!-- 概览 Tab -->
      <el-card v-if="activeTab === 'overview'">
        <el-descriptions title="项目信息" :column="2" border>
          <el-descriptions-item label="项目编号">#{{ projectStore.currentProject.project.projectId }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ projectStore.currentProject.project.projectType === 1 ? '作品创作' : projectStore.currentProject.project.projectType === 2 ? '竞赛备赛' : projectStore.currentProject.project.projectType === 3 ? '定制订单' : '社团活动' }}</el-descriptions-item>
          <el-descriptions-item label="预算">{{ projectStore.currentProject.project.budget }}元</el-descriptions-item>
          <el-descriptions-item label="实际花费">{{ projectStore.currentProject.project.actualCost }}元</el-descriptions-item>
          <el-descriptions-item label="预计结束">{{ formatDate(projectStore.currentProject.project.endDate, 'YYYY-MM-DD') }}</el-descriptions-item>
          <el-descriptions-item label="实际结束">{{ formatDate(projectStore.currentProject.project.actualEndDate, 'YYYY-MM-DD') }}</el-descriptions-item>
          <el-descriptions-item v-if="projectStore.currentProject.project.deliverables" label="交付物" :span="2">
            {{ projectStore.currentProject.project.deliverables }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 成员 Tab -->
      <el-card v-if="activeTab === 'members'">
        <template #header>
          <div class="card-header">
            <span>项目成员</span>
            <el-button v-if="isLeader || isStaff" type="primary" size="small" @click="openAddMember">添加成员</el-button>
          </div>
        </template>
        <el-table :data="projectStore.currentProject.members" stripe>
          <el-table-column prop="memberId" label="学号" width="120" />
          <el-table-column prop="memberName" label="姓名" width="120">
            <template #default="{ row }">
              <span v-if="row.memberName">{{ row.memberName }}</span>
              <span v-else class="text-placeholder">{{ row.memberId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              <el-tag size="small" :type="memberRoleTagType(row.roleInProject)">
                {{ ProjectRoleText[row.roleInProject as keyof typeof ProjectRoleText] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="contribution" label="贡献" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ memberStatusText(row.status) }}</template>
          </el-table-column>
          <el-table-column v-if="isLeader" label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.roleInProject !== 1" text type="danger" size="small" @click="handleRemoveMember(row.memberId)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 阶段 Tab -->
      <el-card v-if="activeTab === 'stages'">
        <template #header><span>项目阶段</span></template>
        <el-timeline>
          <el-timeline-item
            v-for="s in projectStore.currentProject.stages"
            :key="s.progressId"
            :timestamp="`阶段 ${s.stageOrder}`"
            :type="stageStatusTagType(s.status)"
            :hollow="s.status === 0"
            size="normal"
          >
            <div class="stage-item">
              <strong>{{ s.stageName }}</strong>
              <el-tag size="small" :type="stageStatusTagType(s.status)" effect="plain" style="margin-left: 8px">
                {{ StageStatusText[s.status as keyof typeof StageStatusText] }}
              </el-tag>
              <span v-if="s.responsibleName || s.responsibleId" class="stage-meta">
                负责人：{{ s.responsibleName || s.responsibleId }}
              </span>
              <p v-if="s.description" class="stage-desc">{{ s.description }}</p>
              <p v-if="s.startTime || s.endTime" class="stage-time">
                {{ formatDate(s.startTime, 'YYYY-MM-DD') }} ~ {{ formatDate(s.endTime, 'YYYY-MM-DD') }}
              </p>
              <div v-if="isLeader || isStaff" class="stage-actions">
                <el-button v-if="s.status === 0" text type="primary" size="small" @click="openStageStatus(s.progressId, 1)">开始</el-button>
                <el-button v-if="s.status === 1" text type="success" size="small" @click="openStageStatus(s.progressId, 2)">标记完成</el-button>
                <el-button v-if="s.status === 2" text size="small" @click="openStageStatus(s.progressId, 0)">重置为未开始</el-button>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <!-- 文件 Tab -->
      <el-card v-if="activeTab === 'files'">
        <template #header><span>项目文件</span></template>
        <el-table v-if="projectStore.currentProject.files.length > 0" :data="projectStore.currentProject.files" stripe>
          <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ ['', '设计图', 'STL', '照片', '文档', '其他'][row.fileType] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="大小" width="120">
            <template #default="{ row }">
              {{ row.fileSize ? `${(row.fileSize / 1024).toFixed(1)} KB` : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="uploaderId" label="上传者" width="120" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-link :href="row.filePath" target="_blank" type="primary">下载</el-link>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="还没有上传文件" />
      </el-card>

      <!-- 关联任务 Tab -->
      <el-card v-if="activeTab === 'tasks'">
        <template #header><span>关联的打印任务</span></template>
        <el-table v-if="projectStore.currentProject.relatedTasks.length > 0" :data="projectStore.currentProject.relatedTasks" stripe>
          <el-table-column prop="taskId" label="任务编号" width="180" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ ['', '', '已通过', '已驳回', '排队中', '打印中', '已完成', '已取消'][row.status] || '待审批' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="160">
            <template #default="{ row }">{{ formatDate(row.applyTime) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="还没有关联的打印任务" />
      </el-card>
    </template>

    <!-- 添加成员弹窗 -->
    <el-dialog v-model="memberDialogVisible" title="添加项目成员" width="400px">
      <el-form :model="memberForm" label-width="100px">
        <el-form-item label="学号" required>
          <el-input v-model="memberForm.memberId" placeholder="如 2023010005" />
        </el-form-item>
        <el-form-item label="项目内角色" required>
          <el-radio-group v-model="memberForm.roleInProject">
            <el-radio :value="ProjectRole.CORE">核心成员</el-radio>
            <el-radio :value="ProjectRole.PARTICIPANT">参与成员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="贡献描述">
          <el-input v-model="memberForm.contribution" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 阶段状态更新弹窗 -->
    <el-dialog v-model="stageStatusDialogVisible" title="更新阶段状态" width="400px">
      <el-form :model="stageStatusForm" label-width="100px">
        <el-form-item label="新状态">
          <el-radio-group v-model="stageStatusForm.status">
            <el-radio :value="StageStatus.PENDING">未开始</el-radio>
            <el-radio :value="StageStatus.RUNNING">进行中</el-radio>
            <el-radio :value="StageStatus.DONE">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stageStatusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateStageStatus">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
export default { name: 'ProjectDetailPage' }
</script>

<style lang="scss" scoped>
.project-detail-page {
  padding: 0;
}
.project-header-card {
  margin-bottom: $spacing-medium;
}
.header-content {
  display: flex;
  gap: $spacing-large;
}
.cover {
  width: 160px;
  height: 100px;
  border-radius: $border-radius-base;
}
.cover-placeholder {
  width: 160px;
  height: 100px;
  background: $bg-base;
  border-radius: $border-radius-base;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
}
.right {
  flex: 1;
  h2 {
    margin: 0 0 $spacing-small;
  }
}
.meta {
  display: flex;
  align-items: center;
  gap: $spacing-medium;
  color: $text-secondary;
  font-size: $font-size-small;
}
.description {
  margin-top: $spacing-small;
  color: $text-regular;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stage-item {
  .stage-meta {
    margin-left: 12px;
    color: $text-secondary;
    font-size: $font-size-small;
  }
  .stage-desc {
    margin: 4px 0 0;
    color: $text-regular;
  }
  .stage-time {
    margin: 4px 0;
    color: $text-secondary;
    font-size: $font-size-small;
  }
  .stage-actions {
    margin-top: 4px;
  }
}
.detail-tabs {
  margin-bottom: $spacing-medium;
}
.text-placeholder {
  color: $text-placeholder;
}
</style>