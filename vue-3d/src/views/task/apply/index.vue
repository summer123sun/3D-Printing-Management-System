<script setup lang="ts">
/**
 * 提交打印申请（**B** - 社员端）
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StlUploader from '@/components/task/apply/StlUploader.vue'
import ParamForm from '@/components/task/apply/ParamForm.vue'
import { useTaskStore } from '@/stores/task'
import { Priority } from '@/types/task'
import type { TaskApplyDTO } from '@/types/task'

const router = useRouter()
const taskStore = useTaskStore()

const stlPath = ref<string>('')

const form = reactive<TaskApplyDTO>({
  title: '',
  modelName: '',
  stlFilePath: '',
  materialType: 'PLA',
  color: undefined,
  layerHeight: 0.2,
  infillRate: 20,
  needSupport: 0,
  priority: Priority.NORMAL,
  estWeight: undefined,
  estTime: undefined,
  projectId: undefined,
})

const submitting = ref(false)

const handleSubmit = async () => {
  if (!stlPath.value) {
    ElMessage.warning('请先上传 STL 文件')
    return
  }
  if (!form.title || !form.modelName) {
    ElMessage.warning('请填写任务标题和模型名称')
    return
  }

  submitting.value = true
  try {
    form.stlFilePath = stlPath.value
    const taskId = await taskStore.apply(form)
    ElMessage.success(`提交成功！任务编号：${taskId}`)
    router.push(`/task/${taskId}`)
  } catch (e) {
    // 错误已由 axios 拦截器提示
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="task-apply-page">
    <PageHeader title="提交打印申请" :show-back="true" @back="router.back()" />

    <el-card>
      <!-- 第一步：上传 STL -->
      <h3 class="step-title">① 上传 STL 文件</h3>
      <StlUploader v-model="stlPath" />

      <el-divider />

      <!-- 第二步：填写参数 -->
      <h3 class="step-title">② 填写打印参数</h3>
      <ParamForm v-model="form" />

      <el-divider />

      <!-- 提交 -->
      <div class="action-bar">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交申请
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.task-apply-page {
  padding: 0;
}
.step-title {
  margin: 0 0 $spacing-medium;
  font-size: $font-size-large;
  font-weight: 500;
  color: $brand-color;
}
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-small;
}
</style>