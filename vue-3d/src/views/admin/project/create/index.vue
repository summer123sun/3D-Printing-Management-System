<script setup lang="ts">
/**
 * 创建项目（**B** - 管理端）
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StageEditor from '@/components/project/manage/StageEditor.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useProjectStore } from '@/stores/project'
import { ProjectType } from '@/types/project'
import type { ProjectCreateDTO, StageDTO } from '@/types/project'

const router = useRouter()
const projectStore = useProjectStore()

const form = reactive<ProjectCreateDTO>({
  projectName: '',
  projectType: ProjectType.CREATION,
  // ⚠️ 留空走后端默认（用当前登录人），传 null 而不是空字符串，后端判空逻辑会兜底
  leaderId: undefined as unknown as string,
  startDate: '',
  endDate: '',
  budget: 0,
  description: '',
  deliverables: '',
  coverImage: '',
  stages: [],
  initialMembers: [],
})

const stages = ref<StageDTO[]>([])

// ============== 成功/失败反馈弹窗 ==============
const successDialog = reactive({ visible: false, projectId: 0, projectName: '' })
const failDialog = reactive({ visible: false, reason: '' })
let redirectTimer: number | null = null

const closeSuccessAndGo = () => {
  if (redirectTimer) {
    clearTimeout(redirectTimer)
    redirectTimer = null
  }
  successDialog.visible = false
  router.push(`/project/${successDialog.projectId}`)
}

const closeFail = () => {
  failDialog.visible = false
}

onMounted(() => {
  // 默认给一个示例阶段
  if (stages.value.length === 0) {
    stages.value = [
      { stageName: '需求分析', stageOrder: 1, description: '明确项目目标' },
      { stageName: '建模设计', stageOrder: 2, description: '' },
      { stageName: '打印测试', stageOrder: 3, description: '' },
      { stageName: '批量生产', stageOrder: 4, description: '' },
      { stageName: '交付', stageOrder: 5, description: '' },
    ]
  }
})

const handleSubmit = async () => {
  if (!form.projectName || !form.projectName.trim()) {
    ElMessage.warning('请填写项目名称')
    return
  }
  if (!form.startDate) {
    ElMessage.warning('请选择开始日期')
    return
  }
  // 阶段名称去重 + 过滤空名
  const validStages = (stages.value || []).filter(
    (s) => s.stageName && s.stageName.trim(),
  )
  if (validStages.length === 0) {
    ElMessage.warning('至少保留 1 个有效阶段')
    return
  }
  validStages.forEach((s, i) => (s.stageOrder = i + 1))

  const payload: ProjectCreateDTO = {
    ...form,
    // 把空字符串归一为 undefined，让后端走当前用户兜底
    leaderId: form.leaderId && form.leaderId.trim() ? form.leaderId.trim() : undefined,
    stages: validStages,
  }
  try {
    const id = await projectStore.create(payload)
    // 显示大弹窗（AppDialog 风格，跟全站统一）
    successDialog.projectId = id
    successDialog.projectName = form.projectName.trim()
    successDialog.visible = true
    // 5 秒倒计时自动跳转
    redirectTimer = window.setTimeout(() => {
      closeSuccessAndGo()
    }, 5000)
  } catch (e: any) {
    console.error('[创建项目] 失败', e)
    failDialog.reason = e?.message || '请稍后再试'
    failDialog.visible = true
  }
}

onBeforeUnmount(() => {
  if (redirectTimer) clearTimeout(redirectTimer)
})
</script>

<template>
  <div class="project-create-page">
    <PageHeader title="创建项目" :show-back="true" @back="router.back()" />

    <el-card v-loading="projectStore.submitting">
      <el-form :model="form" label-width="120px" label-position="right">
        <h3 class="section-title">基本信息</h3>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目名称" required>
              <el-input v-model="form.projectName" placeholder="如：机械键盘定制套装" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目类型" required>
              <el-select v-model="form.projectType" style="width: 100%">
                <el-option :value="ProjectType.CREATION" label="作品创作" />
                <el-option :value="ProjectType.COMPETE" label="竞赛备赛" />
                <el-option :value="ProjectType.ORDER" label="定制订单" />
                <el-option :value="ProjectType.ACTIVITY" label="社团活动" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="负责人学号">
              <el-input v-model="form.leaderId" placeholder="留空则默认当前登录用户" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="预算 (元)">
              <el-input-number v-model="form.budget" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="封面图 URL">
              <el-input v-model="form.coverImage" placeholder="可选，先上传再填" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期" required>
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计结束日期">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="交付物清单">
          <el-input v-model="form.deliverables" placeholder="如：20套键帽+5个拔键器+3个键盘支架" />
        </el-form-item>

        <h3 class="section-title">项目阶段（可调整顺序）</h3>
        <StageEditor v-model="stages" />

        <div class="action-bar">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" :loading="projectStore.submitting" @click="handleSubmit">创建项目</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 创建成功弹窗（走 AppDialog 风格，跟全站统一） -->
    <AppDialog
      v-model="successDialog.visible"
      title="项目创建成功"
      icon="Check"
      type="success"
      width="460px"
      confirm-text="立即查看详情"
      cancel-text="留在本页"
      :show-footer="true"
      @confirm="closeSuccessAndGo"
      @cancel="successDialog.visible = false"
    >
      <div class="success-content">
        <div class="success-icon-big">
          <el-icon :size="56"><component :is="'Check'" /></el-icon>
        </div>
        <h3 class="success-title">「{{ successDialog.projectName }}」已创建</h3>
        <p class="success-meta">
          项目编号：<b class="project-id">#{{ successDialog.projectId }}</b>
        </p>
        <p class="success-tip">
          <el-icon><component :is="'InfoFilled'" /></el-icon>
          5 秒后将自动跳转到项目详情页…
        </p>
      </div>
    </AppDialog>

    <!-- 创建失败弹窗 -->
    <AppDialog
      v-model="failDialog.visible"
      title="项目创建失败"
      icon="Warning"
      type="danger"
      width="460px"
      confirm-text="我知道了"
      :show-footer="true"
      @confirm="closeFail"
      @cancel="closeFail"
    >
      <div class="fail-content">
        <p class="fail-reason">{{ failDialog.reason }}</p>
        <p class="fail-hint">
          可能原因：<br />
          1) 必填字段缺失<br />
          2) 网络异常（后端未启动）<br />
          3) 登录已过期，请重新登录
        </p>
      </div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.project-create-page {
  padding: 0;
}
.section-title {
  margin: $spacing-base 0 $spacing-medium;
  padding-left: $spacing-small;
  border-left: 4px solid $brand-color;
  font-size: $font-size-large;
  color: $brand-color;
}
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-small;
  margin-top: $spacing-large;
}

// 成功弹窗样式
.success-content {
  text-align: center;
  padding: 8px 0 4px;
}
.success-icon-big {
  width: 96px;
  height: 96px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(103, 194, 58, 0.35);
  animation: success-pop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.success-title {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.success-meta {
  margin: 0 0 16px;
  font-size: 14px;
  color: #606266;
  .project-id {
    color: #67c23a;
    font-size: 18px;
    margin-left: 4px;
    font-weight: 700;
  }
}
.success-tip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  padding: 6px 14px;
  background: #f0f9eb;
  border-radius: 20px;
  font-size: 13px;
  color: #67c23a;
}

// 失败弹窗样式
.fail-content {
  padding: 4px 0;
}
.fail-reason {
  margin: 0 0 14px;
  padding: 12px 16px;
  background: #fef0f0;
  border-left: 3px solid #f56c6c;
  border-radius: 4px;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.fail-hint {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.8;
}

@keyframes success-pop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 1; }
}
</style>