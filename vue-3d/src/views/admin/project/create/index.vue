<script setup lang="ts">
/**
 * 创建项目（**B** - 管理端）
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StageEditor from '@/components/project/manage/StageEditor.vue'
import { useProjectStore } from '@/stores/project'
import { ProjectType } from '@/types/project'
import type { ProjectCreateDTO, StageDTO } from '@/types/project'

const router = useRouter()
const projectStore = useProjectStore()

const form = reactive<ProjectCreateDTO>({
  projectName: '',
  projectType: ProjectType.CREATION,
  leaderId: '',
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
  if (!form.projectName) {
    ElMessage.warning('请填写项目名称')
    return
  }
  if (!form.startDate) {
    ElMessage.warning('请选择开始日期')
    return
  }

  form.stages = stages.value
  try {
    const id = await projectStore.create(form)
    ElMessage.success(`创建成功！项目编号：${id}`)
    router.push(`/project/${id}`)
  } catch (e) {
    // 错误已统一拦截
  }
}
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
</style>