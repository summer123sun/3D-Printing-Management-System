<script setup lang="ts">
/**
 * 项目详情（**B** - 社员端）
 *
 * 聚合 4 张表 + 关联任务 + 阶段时间线
 * 项目负责人可编辑阶段/成员/完成/取消
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useProjectStore } from '@/stores/project'
import { useMemberStyle } from '@/composables/useMemberStyle'
import { useAuthStore } from '@/stores/auth'
import {
  ProjectStatus, ProjectStatusText,
  ProjectRole, ProjectRoleText,
  StageStatus, StageStatusText,
  ProjectMemberStatus, ProjectType, ProjectTypeText,
} from '@/types/project'
import { Role } from '@/utils/enum'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const authStore = useAuthStore()
const { isMember } = useMemberStyle()

const projectId = computed(() => Number(route.params.id))

// ✅ v2.2 修复（用户反馈）：项目查看空白页面
//    原因：fetchDetail 失败时 currentProject 保持 null，<template v-if="currentProject"> 整段不渲染 → 空白
//    修复：catch 错误，保存到 ref，template 显示错误信息 + 重试按钮
const loadError = ref<string | null>(null)

// ✅ v2.2 round 6 修复（用户反馈）：项目查看空白页面的真凶
//    根因：currentProject?.project.leaderId 第二个 .project 没保护 → currentProject=null 时 NPE
//         Vue 3 computed 抛错 → 整页静默白屏（不显示错误信息）
//    修复：全链 ?.
const isLeader = computed(() => projectStore.currentProject?.project?.leaderId === authStore.user?.studentId)
const isStaff = computed(() => {
  const role = authStore.user?.role ?? 0
  return role === Role.PRESIDENT || role === Role.TECH_LEAD
})

const activeTab = ref<'overview' | 'members' | 'stages' | 'files' | 'tasks'>('overview')

const stageStatusDialogVisible = ref(false)
const stageStatusForm = ref({ progressId: 0, status: 0, stageName: '' })
const submittingStageStatus = ref(false)

const memberDialogVisible = ref(false)
const memberForm = ref({ memberId: '', roleInProject: ProjectRole.PARTICIPANT, contribution: '' })
const submittingMember = ref(false)

const fetchData = async (retryCount = 0) => {
  loadError.value = null
  try {
    // ✅ v2.2 修复（用户反馈）：项目查看空白页面
    //    之前现象：路由切换时偶发 fetchDetail 在 onMounted 拿到时 token 还没就绪，
    //             axios 拦截器读到旧 token（或空）→ 401 → 后端返回异常 → 页面无感失败 → 整页空白
    //    修复 1：发请求前先确认 token 存在（避免 401 后又跳登录的死循环）
    if (!authStore.token) {
      console.warn('[项目详情] token 缺失，等 50ms 重试', { retryCount })
      if (retryCount < 3) {
        await new Promise((r) => setTimeout(r, 50))
        return fetchData(retryCount + 1)
      }
      loadError.value = '登录状态丢失，请重新登录'
      return
    }
    console.log('[项目详情] fetchDetail 发起', { id: projectId.value, retryCount })
    await projectStore.fetchDetail(projectId.value)
    console.log('[项目详情] fetchDetail 成功', projectStore.currentProject?.project?.projectName)
  } catch (e: any) {
    console.error('[项目详情] 加载失败：', e, { retryCount })
    // 修复 2：第一次失败自动重试 1 次（应对 chunk 加载竞态 / 网络瞬断）
    if (retryCount === 0 && e?.code !== 401) {
      console.warn('[项目详情] 首次失败，500ms 后重试')
      await new Promise((r) => setTimeout(r, 500))
      return fetchData(retryCount + 1)
    }
    loadError.value = e?.message || '加载失败，请稍后再试'
  }
}
onMounted(fetchData)

// ✅ v2.2 修复（用户反馈）：从 /project/6 跳到 /project/7 组件复用时不重新加载
//    之前：AppMain 用 :key="r.fullPath" 应该会重新挂载，但偶尔出现 watch 不到的情况
//    修复：兜底 watch route.params.id 变化时主动重新拉数据
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    console.log('[项目详情] 路由 id 变化', { from: oldId, to: newId })
    fetchData()
  }
})

/**
 * 通用：判断异常是否是 ElMessageBox 用户取消
 */
const isUserCancel = (e: unknown): boolean => {
  return e === 'cancel' || (e instanceof Error && e.message === 'cancel')
}

const handleComplete = async () => {
  const proj = projectStore.currentProject
  const memberCount = proj?.members?.length ?? 0
  const taskCount = proj?.relatedTasks?.length ?? 0
  const currentStatus = proj?.project.status === ProjectStatus.RUNNING ? '进行中' : proj?.project.status === ProjectStatus.PREPARING ? '筹备中' : '未知'

  try {
    await ElMessageBox.confirm(
      `<div style="line-height:1.9;padding:4px 0">
        <p style="font-size:14px;margin:0 0 12px">
          确认标记项目 <b style="color:#67c23a">「${proj?.project.projectName ?? '该项目'}」</b> 为已完成？
        </p>
        <div style="background: var(--bg-base);padding:10px 14px;border-radius:6px;font-size:13px;color:var(--text-regular);margin-bottom:8px">
          <div>📊 当前状态：<b>${currentStatus}</b>　→　<b style="color:#67c23a">已完成</b></div>
          <div>👥 关联成员：<b>${memberCount}</b> 人</div>
          <div>📋 关联任务：<b>${taskCount}</b> 个</div>
        </div>
        <p style="color:var(--text-secondary);font-size:12px;margin:0">
          ⚠️ 完成后项目不可再修改，但成员仍可查看项目内容
        </p>
      </div>`,
      '标记项目完成',
      {
        type: 'success',
        confirmButtonText: '✓ 确认完成',
        cancelButtonText: '再看看',
        center: true,
        dangerouslyUseHTMLString: true,
      }
    )
  } catch (e) {
    if (isUserCancel(e)) return
    throw e
  }

  await projectStore.complete(projectId.value)
  ElNotification.success({
    title: '项目已完成',
    message:     `「${proj?.project.projectName}」已标记为完成`,
    duration: 3000,
  })
  fetchData()
}

const handleCancel = async () => {
  const proj = projectStore.currentProject
  const memberCount = proj?.members?.length ?? 0
  const taskCount = proj?.relatedTasks?.length ?? 0
  const currentStatus = proj?.project.status === ProjectStatus.RUNNING ? '进行中' : proj?.project.status === ProjectStatus.PREPARING ? '筹备中' : '未知'

  try {
    await ElMessageBox.confirm(
      `<div style="line-height:1.9;padding:4px 0">
        <p style="font-size:14px;margin:0 0 12px">
          确认取消项目 <b style="color:#f56c6c">「${proj?.project.projectName ?? '该项目'}」</b>？
        </p>
        <div style="background: color-mix(in srgb, var(--danger-color) 8%, transparent);padding:10px 14px;border-radius:6px;font-size:13px;color:var(--text-regular);margin-bottom:8px">
          <div>📊 当前状态：<b>${currentStatus}</b>　→　<b style="color:#f56c6c">已取消</b></div>
          <div>👥 关联成员：<b>${memberCount}</b> 人</div>
          <div>📋 关联任务：<b>${taskCount}</b> 个</div>
        </div>
        <p style="color:#f56c6c;font-size:12px;margin:0;font-weight:500">
          ⚠️ 此操作不可撤销！项目数据将进入只读状态，所有阶段进度会丢失
        </p>
      </div>`,
      '取消项目',
      {
        type: 'warning',
        confirmButtonText: '✓ 确认取消',
        cancelButtonText: '再想想',
        confirmButtonClass: 'el-button--danger',
        center: true,
        dangerouslyUseHTMLString: true,
      }
    )
  } catch (e) {
    if (isUserCancel(e)) return
    throw e
  }

  await projectStore.cancel(projectId.value)
  ElNotification.warning({
    title: '项目已取消',
    message:     `「${proj?.project.projectName}」已取消，数据进入只读`,
    duration: 3000,
  })
  fetchData()
}

const openAddMember = () => {
  memberForm.value = { memberId: '', roleInProject: ProjectRole.PARTICIPANT, contribution: '' }
  memberDialogVisible.value = true
}

const handleAddMember = async () => {
  if (!memberForm.value.memberId.trim()) {
    ElNotification.warning('请填写成员学号')
    return
  }
  submittingMember.value = true
  try {
    await projectStore.addMember(projectId.value, memberForm.value)
    ElNotification.success('已添加成员')
    memberDialogVisible.value = false
    fetchData()
  } finally {
    submittingMember.value = false
  }
}

const handleRemoveMember = async (mid: string) => {
  // 查一下成员的姓名展示
  const member = projectStore.currentProject?.members?.find((m: any) => m.memberId === mid)
  const memberName = member?.memberName || mid

  try {
    await ElMessageBox.confirm(
      `<div style="line-height:1.8">
        <p style="margin:0 0 8px">确认移除成员 <b style="color:#f56c6c">${memberName}</b>（学号：${mid}）？</p>
        <p style="color:var(--text-secondary);font-size:13px;margin:0">
          该成员将无法再访问此项目，但其本人的账号不会被删除。
        </p>
      </div>`,
      '移除项目成员',
      {
        type: 'warning',
        confirmButtonText: '✓ 确认移除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
        center: true,
        dangerouslyUseHTMLString: true,
      }
    )
  } catch (e) {
    if (isUserCancel(e)) return
    throw e
  }

  await projectStore.removeMember(projectId.value, mid)
  ElNotification.success({
    title: '成员已移除',
    message: `${memberName} 不再属于此项目`,
    duration: 3000,
  })
  fetchData()
}

const openStageStatus = (pid: number, status: number) => {
  // 找到当前阶段对象，拿到 stageName 展示
  const cur = projectStore.currentProject?.stages?.find((s) => s.progressId === pid)
  stageStatusForm.value = {
    progressId: pid,
    status,
    stageName: cur ? `阶段 ${cur.stageOrder} - ${cur.stageName}` : `#${pid}`,
  }
  stageStatusDialogVisible.value = true
}

const handleUpdateStageStatus = async () => {
  submittingStageStatus.value = true
  try {
    await projectStore.updateStageStatus(
      projectId.value,
      stageStatusForm.value.progressId,
      stageStatusForm.value.status,
    )
    ElNotification.success({
      title: '阶段状态已更新',
      message: `「${stageStatusForm.value.stageName}」已更新`,
      duration: 2500,
    })
    stageStatusDialogVisible.value = false
    fetchData()
  } catch (e: any) {
    console.error('[更新阶段状态] 失败', e)
    ElNotification.error({
      title: '更新失败',
      message: e?.message || '请稍后再试',
      duration: 4000,
    })
  } finally {
    submittingStageStatus.value = false
  }
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
      :title="projectStore.currentProject?.project?.projectName || '项目详情'"
      :show-back="true"
      @back="router.back()"
    >
      <el-button v-if="isLeader && projectStore.currentProject?.project?.status === ProjectStatus.RUNNING" type="success" @click="handleComplete">
        标记完成
      </el-button>
      <!-- ✅ v2.2 修复（用户反馈）：已取消的项目仍显示"取消项目"按钮
           之前：v-if 只排除 status === DONE（2），没排除 CANCELLED（3）
                 → 已取消的项目（status=3）还能再点"取消"，进入异常流程
           修复：v-if 排除 DONE 和 CANCELLED 两种终态
                 只有 PREPARING (0) / RUNNING (1) 的项目才显示"取消项目"按钮
      -->
      <el-button v-if="isLeader && projectStore.currentProject?.project?.status !== ProjectStatus.DONE && projectStore.currentProject?.project?.status !== ProjectStatus.CANCELLED" type="danger" plain @click="handleCancel">
        取消项目
      </el-button>
      <el-button @click="() => fetchData()">刷新</el-button>
    </PageHeader>

    <template v-if="loadError">
      <!-- ✅ v2.2 修复：fetchDetail 失败时显示错误信息（之前整页空白） -->
      <el-card>
        <el-empty :description="loadError" :image-size="80">
          <el-button type="primary" @click="() => fetchData()">重新加载</el-button>
          <el-button @click="router.back()">返回上一页</el-button>
        </el-empty>
      </el-card>
    </template>
    <template v-else-if="projectStore.currentProject && projectStore.currentProject.project">
      <!-- 成员端：项目 Hero 卡片 -->
      <div v-if="isMember" class="member-project-hero">
        <div class="hero-cover">
          <el-image
            v-if="projectStore.currentProject.project.coverImage"
            :src="projectStore.currentProject.project.coverImage"
            fit="cover"
            class="hero-cover-image"
          >
            <template #error>
              <div class="hero-cover-placeholder">
                <span class="placeholder-icon">📁</span>
                <span class="placeholder-text">{{ projectStore.currentProject.project.projectName }}</span>
              </div>
            </template>
          </el-image>
          <div v-else class="hero-cover-placeholder">
            <span class="placeholder-icon">📁</span>
            <span class="placeholder-text">暂无封面</span>
          </div>
          <div class="hero-overlay" />
        </div>
        <div class="hero-body">
          <div class="hero-info">
            <div class="hero-tags">
              <span class="hero-status" :class="'status-' + projectStore.currentProject.project.status">
                {{ ProjectStatusText[projectStore.currentProject.project.status] }}
              </span>
              <span class="hero-type">{{ ProjectTypeText[projectStore.currentProject.project.projectType as keyof typeof ProjectTypeText] }}</span>
            </div>
            <h1 class="hero-title">{{ projectStore.currentProject.project.projectName }}</h1>
            <p v-if="projectStore.currentProject.project.description" class="hero-desc">
              {{ projectStore.currentProject.project.description }}
            </p>
            <div class="hero-meta-row">
              <div class="meta-pill">
                <span class="meta-icon">👤</span>
                <span class="meta-text">负责人：{{ projectStore.currentProject.project.leaderId }}</span>
              </div>
              <div class="meta-pill">
                <span class="meta-icon">📅</span>
                <span class="meta-text">开始：{{ formatDate(projectStore.currentProject.project.startDate, 'YYYY-MM-DD') }}</span>
              </div>
              <div v-if="projectStore.currentProject.project.budget" class="meta-pill">
                <span class="meta-icon">💰</span>
                <span class="meta-text">预算：{{ projectStore.currentProject.project.budget }} 元</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 后台端：原 header card -->
      <el-card v-else class="project-header-card">
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
              <el-tag size="small" effect="dark">{{ ProjectStatusText[projectStore.currentProject.project.status] }}</el-tag>
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
          <el-descriptions-item label="类型">{{ projectStore.currentProject.project.projectType === ProjectType.CREATION ? '作品创作' : projectStore.currentProject.project.projectType === ProjectType.COMPETE ? '竞赛备赛' : projectStore.currentProject.project.projectType === ProjectType.ORDER ? '定制订单' : '社团活动' }}</el-descriptions-item>
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
              <el-tag size="small" :type="memberRoleTagType(row.roleInProject)" effect="dark">
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
            :hollow="s.status === StageStatus.PENDING"
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
                <el-button v-if="s.status === StageStatus.PENDING" text type="primary" size="small" @click="openStageStatus(s.progressId, StageStatus.RUNNING)">开始</el-button>
                <el-button v-if="s.status === StageStatus.RUNNING" text type="success" size="small" @click="openStageStatus(s.progressId, StageStatus.DONE)">标记完成</el-button>
                <el-button v-if="s.status === StageStatus.DONE" text size="small" @click="openStageStatus(s.progressId, StageStatus.PENDING)">重置为未开始</el-button>
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
              <el-tag size="small" effect="dark">{{ ['', '设计图', 'STL', '照片', '文档', '其他'][row.fileType] }}</el-tag>
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
        <EmptyState v-else description="还没有上传文件" hint="上传 STL/3MF 文件，打印任务会更顺畅。" />
      </el-card>

      <!-- 关联任务 Tab -->
      <el-card v-if="activeTab === 'tasks'">
        <template #header><span>关联的打印任务</span></template>
        <el-table v-if="projectStore.currentProject.relatedTasks.length > 0" :data="projectStore.currentProject.relatedTasks" stripe>
          <el-table-column prop="taskId" label="任务编号" width="180" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" effect="dark">{{ ['', '', '已通过', '已驳回', '排队中', '打印中', '已完成', '已取消'][row.status] || '待审批' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="160">
            <template #default="{ row }">{{ formatDate(row.applyTime) }}</template>
          </el-table-column>
        </el-table>
        <EmptyState v-else description="还没有关联的打印任务" hint="在打印任务申请时选择关联到此项目即可。" />
      </el-card>
    </template>
    <!-- ✅ v2.2 round 5 修复：fetchDetail 静默成功但 data=null 时的兜底（之前整页空白） -->
    <template v-else>
      <el-card>
        <el-empty description="项目数据加载中..." :image-size="80">
          <el-button type="primary" @click="() => fetchData()">重新加载</el-button>
        </el-empty>
      </el-card>
    </template>

    <!-- 添加成员弹窗 -->
    <AppDialog v-model="memberDialogVisible" title="添加项目成员" icon="User" type="primary" width="480px"
               confirm-text="确认添加" :loading="submittingMember" @confirm="handleAddMember">
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
    </AppDialog>

    <!-- 阶段状态更新弹窗 -->
    <AppDialog v-model="stageStatusDialogVisible" :title="`更新阶段状态 — ${stageStatusForm.stageName}`" icon="Edit" type="primary" width="440px"
               confirm-text="确认更新" :loading="submittingStageStatus" @confirm="handleUpdateStageStatus">
      <el-form :model="stageStatusForm" label-width="100px">
        <el-form-item label="新状态">
          <el-radio-group v-model="stageStatusForm.status">
            <el-radio :value="StageStatus.PENDING">未开始</el-radio>
            <el-radio :value="StageStatus.RUNNING">进行中</el-radio>
            <el-radio :value="StageStatus.DONE">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>

<script lang="ts">
export default { name: 'ProjectDetailPage' }
</script>

<style lang="scss" scoped>
.project-detail-page {

// ============ 成员端：项目 Hero ============
.member-project-hero {
  border-radius: 20px;
  overflow: hidden;
  background: #FFFFFF;
  box-shadow: 0 4px 20px rgba(10, 37, 64, 0.08);
  border: 1px solid var(--border-extra-light);
}
.hero-cover {
  position: relative;
  width: 100%;
  height: 240px;
  background: linear-gradient(135deg, #0A2540 0%, #1E3A5F 50%, #00A88A 100%);
  overflow: hidden;
}
.hero-cover-image {
  width: 100%;
  height: 100%;
}
.hero-cover-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0A2540 0%, #1E3A5F 50%, #00A88A 100%);
  color: rgba(255, 255, 255, 0.6);
  .placeholder-icon { font-size: 64px; }
  .placeholder-text { font-size: 14px; margin-top: 8px; }
}
.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(10, 37, 64, 0.55) 0%, transparent 50%);
  pointer-events: none;
}
.hero-body {
  padding: 32px 40px;
}
.hero-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.hero-status {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  background: #E0FAF4;
  color: #00A88A;

  &.status-0 { background: #E8EEF5; color: #0A2540; }
  &.status-1 { background: #E0FAF4; color: #00A88A; }
  &.status-2 { background: #DCFCE7; color: #15803D; }
  &.status-3 { background: #F4F4F5; color: #71717A; }
}
.hero-type {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(0, 212, 170, 0.12);
  color: #00A88A;
}
.hero-title {
  margin: 0 0 12px;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}
.hero-desc {
  margin: 0 0 20px;
  font-size: 15px;
  line-height: 1.6;
  color: var(--text-regular);
  max-width: 720px;
}
.hero-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.meta-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: var(--bg-base);
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-regular);
}
.meta-icon { font-size: 14px; }
.meta-text { color: var(--text-regular); }

@include mobile {
  .hero-body { padding: $spacing-large; }
  .hero-title { font-size: 24px; }
  .hero-cover { height: 180px; }
}
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
  background: var(--bg-base);
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
  color: var(--text-secondary);
  font-size: $font-size-small;
}
.description {
  margin-top: $spacing-small;
  color: var(--text-regular);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stage-item {
  .stage-meta {
    margin-left: 12px;
    color: var(--text-secondary);
    font-size: $font-size-small;
  }
  .stage-desc {
    margin: 4px 0 0;
    color: var(--text-regular);
  }
  .stage-time {
    margin: 4px 0;
    color: var(--text-secondary);
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
  color: var(--text-placeholder);
}
</style>