<script setup lang="ts">
/**
 * 编辑作品（v2 紫系 + 深海蓝金风）
 *
 * 基于已存在的 artwork 修改：
 * - 作品名（必填）
 * - 封面图（可换）
 * - 成品照片（可增删）
 * - 心得总结
 *
 * 不能改：taskId / authorId（创建后固定）
 * 不能改：isRecommended / viewCount（系统管理）
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, UploadFilled, Delete } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { artworkDetail, updateArtwork } from '@/api/artwork'
import type { Artwork } from '@/types/artwork'
import { formatDate } from '@/utils/format'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const artworkId = computed(() => Number(route.params.id))

interface FormState {
  artworkName: string
  previewImage: string
  finishPhotos: string
  experience: string
}

const form = ref<FormState>({
  artworkName: '',
  previewImage: '',
  finishPhotos: '',
  experience: '',
})

const loading = ref(true)
const submitting = ref(false)
const original = ref<Artwork | null>(null)

// 文件上传
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${getToken()}` }))
const uploadUrl = '/api/file/upload'

// 计算属性
const previewImageUrl = computed(() => form.value.previewImage)
const photosUrls = computed(() =>
  form.value.finishPhotos ? form.value.finishPhotos.split(',').filter(Boolean) : [],
)

const loadData = async () => {
  if (!artworkId.value || isNaN(artworkId.value)) {
    ElMessage.error('无效的作品 ID')
    router.push('/artwork/my')
    return
  }
  loading.value = true
  try {
    const data = await artworkDetail(artworkId.value)
    original.value = data
    form.value = {
      artworkName: data.artworkName || '',
      previewImage: data.previewImage || '',
      finishPhotos: data.finishPhotos || '',
      experience: data.experience || '',
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
    router.back()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

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

// 成品照上传成功（多图，追加到 finishPhotos）
const handlePhotosSuccess = (res: any) => {
  if (res?.code === 200 && res?.data?.url) {
    const list = form.value.finishPhotos ? form.value.finishPhotos.split(',') : []
    list.push(res.data.url)
    form.value.finishPhotos = list.join(',')
    ElMessage.success('上传成功')
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
const successDialog = ref({ visible: false })
const failDialog = ref({ visible: false, reason: '', status: undefined, url: '' })

const handleSubmit = async () => {
  if (!form.value.artworkName || !form.value.artworkName.trim()) {
    ElMessage.warning('请填写作品名')
    return
  }
  submitting.value = true
  try {
    await updateArtwork(artworkId.value, {
      artworkName: form.value.artworkName.trim(),
      previewImage: form.value.previewImage || undefined,
      finishPhotos: form.value.finishPhotos || undefined,
      experience: form.value.experience || undefined,
    })
    successDialog.value.visible = true
  } catch (e: any) {
    // 完整日志到 console（F12 可见）
    console.error('[编辑作品] 保存失败', e)
    // 提取多源错误信息（保证 reason 一定有内容）
    const reason =
      e?.response?.data?.msg ||     // axios 响应体里的业务消息
      e?.response?.data?.message || // spring 默认 message 字段
      e?.message ||                 // Error.message
      (typeof e === 'string' ? e : null) ||
      '请稍后再试'
    failDialog.value.reason = reason
    failDialog.value.status = e?.response?.status
    failDialog.value.url = e?.config?.url
    failDialog.value.visible = true
  } finally {
    submitting.value = false
  }
}

const goToDetail = () => {
  successDialog.value.visible = false
  router.push(`/artwork/${artworkId.value}`)
}
const closeSuccess = () => {
  successDialog.value.visible = false
  router.push('/artwork/my')
}
const closeFail = () => {
  failDialog.value.visible = false
}
</script>

<template>
  <div class="artwork-edit-page">
    <PageHeader title="编辑作品" :show-back="true" @back="router.back()" />

    <div v-loading="loading" class="edit-form-wrap">
      <el-card v-if="original" class="form-card">
        <div class="form-tip">
          <el-icon><Picture /></el-icon>
          <span>编辑作品信息（作品创建后，关联任务和作者不能修改）</span>
        </div>

        <!-- 作品元信息（只读） -->
        <div class="meta-row">
          <div class="meta-item">
            <span class="meta-label">作品编号：</span>
            <span class="meta-value code">#{{ original.artworkId }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">作者：</span>
            <span class="meta-value">{{ original.authorName || original.authorId }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">浏览：</span>
            <span class="meta-value">{{ original.viewCount || 0 }} 次</span>
          </div>
          <div v-if="original.createTime" class="meta-item">
            <span class="meta-label">创建：</span>
            <span class="meta-value">{{ formatDate(original.createTime) }}</span>
          </div>
        </div>

        <el-form :model="form" label-width="110px" label-position="right" class="edit-form">
          <!-- 作品名 -->
          <el-form-item label="作品名" required>
            <el-input
              v-model="form.artworkName"
              placeholder="给作品起个名字"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <!-- 封面图 -->
          <el-form-item label="封面图">
            <el-upload
              :headers="uploadHeaders"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handlePreviewSuccess"
              :on-remove="handlePreviewRemove"
              accept="image/*"
              class="cover-uploader"
            >
              <div v-if="!previewImageUrl" class="cover-empty">
                <el-icon :size="36"><Picture /></el-icon>
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
              :headers="uploadHeaders"
              :action="uploadUrl"
              :on-success="handlePhotosSuccess"
              :on-remove="handlePhotosRemove"
              accept="image/*"
              list-type="picture-card"
              multiple
            >
              <el-icon :size="24"><Picture /></el-icon>
              <template #tip>
                <div class="el-upload__tip">
                  可上传多张成品图（点击图片右上角 × 可删除）
                </div>
              </template>
            </el-upload>
            <div v-if="photosUrls.length > 0" class="photos-count">
              已上传 <b>{{ photosUrls.length }}</b> 张
            </div>
          </el-form-item>

          <!-- 心得 -->
          <el-form-item label="心得总结">
            <el-input
              v-model="form.experience"
              type="textarea"
              :rows="6"
              placeholder="分享你的创作过程、踩坑经验、改进思路等"
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
              保存修改
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>

    <!-- 成功弹窗 -->
    <AppDialog
      v-model="successDialog.visible"
      title="作品已更新"
      icon="Check"
      type="success"
      width="440px"
      confirm-text="查看作品"
      cancel-text="回到我的作品"
      @confirm="goToDetail"
      @cancel="closeSuccess"
    >
      <div class="success-content">
        <div class="success-icon-big">
          <el-icon :size="56"><component :is="'Check'" /></el-icon>
        </div>
        <h3>作品信息已保存</h3>
        <p>作品编号：<b class="project-id">#{{ artworkId }}</b></p>
      </div>
    </AppDialog>

    <!-- 失败弹窗 -->
    <AppDialog
      v-model="failDialog.visible"
      title="保存失败"
      icon="Warning"
      type="danger"
      width="520px"
      confirm-text="我知道了"
      @confirm="closeFail"
      @cancel="closeFail"
    >
      <div class="fail-content">
        <p class="fail-reason">{{ failDialog.reason }}</p>
        <div v-if="failDialog.status || failDialog.url" class="fail-debug">
          <span v-if="failDialog.status">HTTP {{ failDialog.status }}</span>
          <code v-if="failDialog.url">{{ failDialog.url }}</code>
        </div>
        <p class="fail-hint">
          详细堆栈请按 F12 查看 console
        </p>
      </div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.artwork-edit-page {
  padding: 0;
}
.edit-form-wrap {
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
  margin-bottom: 20px;
  background: linear-gradient(135deg,
    color-mix(in srgb, $accent-color 8%, transparent) 0%,
    color-mix(in srgb, $gold-color 8%, transparent) 100%);
  border-left: 3px solid $accent-color;
  border-radius: 8px;
  color: var(--text-regular);
  font-size: 14px;
  .el-icon {
    color: $accent-color;
    font-size: 18px;
  }
}

// 只读元信息条
.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 24px;
  padding: 14px 18px;
  margin-bottom: 24px;
  background: var(--bg-base);
  border-radius: 8px;
  border-left: 3px solid $primary-color;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  .meta-label { color: var(--text-secondary); }
  .meta-value {
    color: var(--text-primary);
    font-weight: 500;
  }
  .meta-value.code {
    font-family: monospace;
    color: $accent-color-dark;
  }
}

.edit-form {
  margin-top: 8px;
}

.cover-uploader {
  :deep(.el-upload) {
    border: 2px dashed var(--border-light);
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
  color: var(--text-placeholder);
  .el-icon { color: var(--text-placeholder); margin-bottom: 8px; }
  p { margin: 0; font-size: 13px; }
  .hint { font-size: 12px; margin-top: 4px; color: var(--text-placeholder); }
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
  &:hover .cover-actions { opacity: 1; }
}
.photos-count {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  b {
    color: $accent-color-dark;
    font-weight: 600;
  }
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-lighter);
}

// 成功/失败弹窗样式
.success-content {
  text-align: center;
  padding: 8px 0;
  h3 {
    margin: 12px 0 8px;
    font-size: 16px;
    color: var(--text-primary);
  }
  p {
    margin: 0;
    color: var(--text-secondary);
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
    margin: 0 0 10px;
    padding: 12px 16px;
    background: rgba(255, 71, 87, 0.08);
    border-left: 3px solid $danger-color;
    border-radius: 4px;
    color: var(--text-primary);
    font-size: 14px;
    font-weight: 500;
    word-break: break-all;
  }
  .fail-debug {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 6px 10px;
    margin-bottom: 10px;
    background: var(--bg-base);
    border-radius: 4px;
    font-size: 12px;
    color: var(--text-secondary);
    span {
      padding: 2px 6px;
      background: $danger-color;
      color: white;
      border-radius: 3px;
      font-weight: 600;
    }
    code {
      font-family: monospace;
      color: var(--text-primary);
      word-break: break-all;
    }
  }
  .fail-hint {
    margin: 0;
    font-size: 12px;
    color: var(--text-placeholder);
    text-align: center;
  }
}
</style>
