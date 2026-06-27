<script setup lang="ts">
/**
 * 登记作品（v2 紫系 + 深海蓝金风）
 *
 * 用户从已完结的打印任务里选一个，登记为作品：
 * - 选择 taskId（自动筛选"我已完结且未登记"的任务）
 * - 作品名（默认取 task.title）
 * - 上传封面图（el-upload，调 /api/file/upload）
 * - 上传成品照片（多图，el-upload，逗号分隔存 finishPhotos）
 * - 心得总结（textarea）
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { Plus, Picture, UploadFilled, Delete } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { createArtwork } from '@/api/artwork'
import { myTasks } from '@/api/task'
import type { PrintTask } from '@/types/task'
import { formatDate } from '@/utils/format'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

interface FormState {
  taskId: string
  artworkName: string
  previewImage: string
  finishPhotos: string
  experience: string
}

const form = ref<FormState>({
  taskId: '',
  artworkName: '',
  previewImage: '',
  finishPhotos: '',
  experience: '',
})

const submitting = ref(false)

// 可选任务列表（当前用户已完结 + 未登记过）
const availableTasks = ref<PrintTask[]>([])
const loadingTasks = ref(false)

const selectedTask = computed(() => availableTasks.value.find((t) => t.taskId === form.value.taskId))

// 文件上传相关
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${getToken()}` }))
const uploadUrl = '/api/file/upload'
const previewFileList = ref<any[]>([])
const photosFileList = ref<any[]>([])

const loadAvailableTasks = async () => {
  loadingTasks.value = true
  try {
    const res = await myTasks({ status: 5, size: 100 })  // status=5 已完结
    availableTasks.value = res.records || []
  } finally {
    loadingTasks.value = false
  }
}

onMounted(() => {
  loadAvailableTasks()
  // 支持从 URL 预填 taskId（从 task/my 跳转过来）
  const t = route.query.taskId as string | undefined
  if (t) {
    form.value.taskId = t
  }
})

// 任务选中后自动填默认作品名
watch(() => form.value.taskId, (newId) => {
  if (newId) {
    const t = availableTasks.value.find((x) => x.taskId === newId)
    if (t && !form.value.artworkName) {
      form.value.artworkName = t.title || ''
    }
  }
})

// 封面图上传成功
const handlePreviewSuccess = (res: any) => {
  if (res?.code === 200 && res?.data?.url) {
    form.value.previewImage = res.data.url
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(res?.msg || '封面上传失败')
  }
}
const handlePreviewRemove = () => {
  form.value.previewImage = ''
}

// 成品照上传成功
const handlePhotosSuccess = (res: any, file: any) => {
  if (res?.code === 200 && res?.data?.url) {
    // 多图：追加到 finishPhotos 字符串
    const list = form.value.finishPhotos ? form.value.finishPhotos.split(',') : []
    list.push(res.data.url)
    form.value.finishPhotos = list.join(',')
    ElMessage.success(`第 ${photosFileList.value.length} 张上传成功`)
  } else {
    ElMessage.error(res?.msg || '上传失败')
  }
}
const handlePhotosRemove = (file: any) => {
  if (file?.response?.data?.url) {
    const url = file.response.data.url
    const list = form.value.finishPhotos.split(',').filter((u) => u !== url)
    form.value.finishPhotos = list.join(',')
  } else if (file?.url) {
    const list = form.value.finishPhotos.split(',').filter((u) => u !== file.url)
    form.value.finishPhotos = list.join(',')
  }
}

// 弹窗
const successDialog = ref({ visible: false, artworkId: 0, artworkName: '' })
const failDialog = ref({ visible: false, reason: '' })

const handleSubmit = async () => {
  if (!form.value.taskId) {
    ElMessage.warning('请选择关联任务')
    return
  }
  if (!form.value.artworkName || !form.value.artworkName.trim()) {
    ElMessage.warning('请填写作品名')
    return
  }

  submitting.value = true
  try {
    const id = await createArtwork({
      taskId: form.value.taskId,
      artworkName: form.value.artworkName.trim(),
      previewImage: form.value.previewImage || undefined,
      finishPhotos: form.value.finishPhotos || undefined,
      experience: form.value.experience || undefined,
    })
    successDialog.value.artworkId = id
    successDialog.value.artworkName = form.value.artworkName.trim()
    successDialog.value.visible = true
  } catch (e: any) {
    failDialog.value.reason = e?.message || '请稍后再试'
    failDialog.value.visible = true
  } finally {
    submitting.value = false
  }
}

const goToDetail = () => {
  successDialog.value.visible = false
  router.push(`/artwork/${successDialog.value.artworkId}`)
}
const goBack = () => {
  successDialog.value.visible = false
  router.push('/artwork/my')
}
const closeFail = () => {
  failDialog.value.visible = false
}

const previewImageUrl = computed(() => form.value.previewImage)
const photosUrls = computed(() =>
  form.value.finishPhotos ? form.value.finishPhotos.split(',').filter(Boolean) : [],
)
</script>

<template>
  <div class="artwork-create-page">
    <PageHeader title="登记作品" :show-back="true" @back="router.back()" />

    <div class="create-form-wrap">
      <el-card v-loading="submitting" class="form-card">
        <div class="form-tip">
          <el-icon><Picture /></el-icon>
          <span>从你已完结的打印任务中选一个，登记为作品（让其他社员看到你的成果）</span>
        </div>

        <el-form :model="form" label-width="110px" label-position="right">
          <!-- 选择关联任务 -->
          <el-form-item label="关联任务" required>
            <el-select
              v-model="form.taskId"
              placeholder="选择已完结的任务"
              style="width: 100%"
              :loading="loadingTasks"
              filterable
            >
              <el-option
                v-for="t in availableTasks"
                :key="t.taskId"
                :value="t.taskId"
                :label="`${t.taskId} · ${t.title}`"
              >
                <div class="task-option">
                  <span class="task-option-id">{{ t.taskId }}</span>
                  <span class="task-option-title">{{ t.title }}</span>
                  <span class="task-option-date">{{ formatDate(t.finishTime, 'YYYY-MM-DD') }}</span>
                </div>
              </el-option>
            </el-select>
            <div v-if="availableTasks.length === 0 && !loadingTasks" class="empty-tip">
              你还没有已完结的任务，无法登记作品
            </div>
          </el-form-item>

          <!-- 选中任务的预览 -->
          <el-form-item v-if="selectedTask" label="任务预览">
            <div class="task-preview">
              <div class="task-preview-row">
                <span class="label">任务编号：</span>
                <span class="value code">{{ selectedTask.taskId }}</span>
              </div>
              <div class="task-preview-row">
                <span class="label">模型：</span>
                <span class="value">{{ selectedTask.modelName }}</span>
              </div>
              <div class="task-preview-row">
                <span class="label">耗材：</span>
                <span class="value">{{ selectedTask.materialType }} · {{ selectedTask.color }}</span>
              </div>
              <div class="task-preview-row">
                <span class="label">完成时间：</span>
                <span class="value">{{ formatDate(selectedTask.finishTime) }}</span>
              </div>
            </div>
          </el-form-item>

          <!-- 作品名 -->
          <el-form-item label="作品名" required>
            <el-input
              v-model="form.artworkName"
              placeholder="给作品起个名字（默认跟任务标题）"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <!-- 封面图 -->
          <el-form-item label="封面图">
            <el-upload
              v-model:file-list="previewFileList"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handlePreviewSuccess"
              :on-remove="handlePreviewRemove"
              accept="image/*"
              class="cover-uploader"
            >
              <div v-if="!previewImageUrl" class="cover-empty">
                <el-icon :size="36"><Plus /></el-icon>
                <p>点击上传封面图</p>
                <p class="hint">建议 16:9 / JPG · PNG</p>
              </div>
              <div v-else class="cover-preview">
                <img :src="previewImageUrl" alt="封面" />
                <div class="cover-actions">
                  <el-button type="danger" size="small" :icon="Delete" @click.stop="handlePreviewRemove">
                    移除
                  </el-button>
                </div>
              </div>
            </el-upload>
          </el-form-item>

          <!-- 成品照片 -->
          <el-form-item label="成品照片">
            <el-upload
              v-model:file-list="photosFileList"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handlePhotosSuccess"
              :on-remove="handlePhotosRemove"
              accept="image/*"
              list-type="picture-card"
              multiple
            >
              <el-icon :size="24"><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip">
                  可上传多张成品图（建议 ≤ 6 张）
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <!-- 心得 -->
          <el-form-item label="心得总结">
            <el-input
              v-model="form.experience"
              type="textarea"
              :rows="5"
              placeholder="分享你的创作过程、踩坑经验、改进思路等（可选）"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <div class="action-bar">
            <el-button @click="router.back()">取消</el-button>
            <el-button
              type="primary"
              :loading="submitting"
              :icon="UploadFilled"
              @click="handleSubmit"
            >
              提交登记
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>

    <!-- 成功弹窗 -->
    <AppDialog
      v-model="successDialog.visible"
      title="作品登记成功"
      icon="Check"
      type="success"
      width="440px"
      confirm-text="查看作品"
      cancel-text="回到我的作品"
      @confirm="goToDetail"
      @cancel="goBack"
    >
      <div class="success-content">
        <div class="success-icon-big">
          <el-icon :size="56"><component :is="'Check'" /></el-icon>
        </div>
        <h3>「{{ successDialog.artworkName }}」已登记</h3>
        <p>作品编号：<b class="project-id">#{{ successDialog.artworkId }}</b></p>
      </div>
    </AppDialog>

    <!-- 失败弹窗 -->
    <AppDialog
      v-model="failDialog.visible"
      title="登记失败"
      icon="Warning"
      type="danger"
      width="440px"
      confirm-text="我知道了"
      :show-footer="true"
      @confirm="closeFail"
      @cancel="closeFail"
    >
      <div class="fail-content">
        <p class="fail-reason">{{ failDialog.reason }}</p>
      </div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.artwork-create-page {
  padding: 0;
}
.create-form-wrap {
  max-width: 880px;
  margin: 0 auto;
}
.form-card {
  padding: 8px;
}
.form-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin-bottom: 24px;
  background: linear-gradient(135deg,
    color-mix(in srgb, $accent-color 8%, transparent) 0%,
    color-mix(in srgb, $gold-color 8%, transparent) 100%);
  border-left: 3px solid $accent-color;
  border-radius: 8px;
  color: $text-regular;
  font-size: 14px;
  .el-icon {
    color: $accent-color;
    font-size: 18px;
  }
}

.task-option {
  display: flex;
  align-items: center;
  gap: 10px;
  .task-option-id {
    color: $text-placeholder;
    font-size: 12px;
    font-family: monospace;
  }
  .task-option-title {
    flex: 1;
    color: $text-primary;
  }
  .task-option-date {
    color: $text-secondary;
    font-size: 12px;
  }
}
.empty-tip {
  margin-top: 8px;
  color: $text-secondary;
  font-size: 13px;
}

.task-preview {
  width: 100%;
  padding: 12px 16px;
  background: $bg-base;
  border-radius: 8px;
  border-left: 3px solid $primary-color;
  .task-preview-row {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 3px 0;
    font-size: 13px;
    .label { color: $text-secondary; min-width: 80px; }
    .value { color: $text-primary; }
    .value.code {
      font-family: monospace;
      color: $accent-color-dark;
    }
  }
}

.cover-uploader {
  :deep(.el-upload) {
    border: 2px dashed $border-light;
    border-radius: 12px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 280px;
    height: 180px;
    transition: all 0.2s ease;
    &:hover {
      border-color: $accent-color;
      background: color-mix(in srgb, $accent-color 4%, transparent);
    }
  }
}
.cover-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: $text-placeholder;
  .el-icon { color: $text-placeholder; margin-bottom: 8px; }
  p { margin: 0; font-size: 13px; }
  .hint { font-size: 12px; margin-top: 4px; color: $text-placeholder; }
}
.cover-preview {
  position: relative;
  width: 100%;
  height: 100%;
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 8px;
  }
  .cover-actions {
    position: absolute;
    top: 8px;
    right: 8px;
    opacity: 0;
    transition: opacity 0.2s;
  }
  &:hover .cover-actions {
    opacity: 1;
  }
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid $border-lighter;
}

// 成功/失败弹窗样式
.success-content {
  text-align: center;
  padding: 8px 0;
  h3 {
    margin: 12px 0 8px;
    font-size: 16px;
    color: $text-primary;
  }
  p {
    margin: 0;
    color: $text-secondary;
    font-size: 14px;
  }
  .project-id {
    color: $accent-color-dark;
    font-size: 16px;
    font-weight: 700;
  }
}
.success-icon-big {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  border-radius: 50%;
  background: linear-gradient(135deg, $accent-color 0%, $accent-color-light 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px color-mix(in srgb, $accent-color 35%, transparent);
  animation: pop-in 0.5s $ease-out-back;
}
.fail-content {
  .fail-reason {
    margin: 0;
    padding: 12px 16px;
    background: rgba(255, 71, 87, 0.08);
    border-left: 3px solid $danger-color;
    border-radius: 4px;
    color: $text-primary;
    font-size: 14px;
  }
}
</style>
