<script setup lang="ts">
/**
 * 作品详情（M4）
 *
 * 大图预览 + 作者信息 + 心得 + 推荐/编辑按钮（按角色）
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElNotification } from 'element-plus'
import { ArrowLeft, Edit, Star, StarFilled, View } from '@element-plus/icons-vue'
import { useArtworkStore } from '@/stores/artwork'
import { useAuthStore } from '@/stores/auth'
import { formatDate } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const store = useArtworkStore()
const authStore = useAuthStore()

const artworkId = computed(() => Number(route.params.id))
const editing = ref(false)
const editForm = ref({
  artworkName: '',
  finishPhotos: '',
  experience: '',
})

const isMine = computed(() => {
  return authStore.user?.studentId === store.current?.authorId
})

const canRecommend = computed(() => {
  const role = authStore.user?.role
  return role === 1 || role === 2
})

const fetchData = async () => {
  await store.fetchDetail(artworkId.value)
  editForm.value = {
    artworkName: store.current?.artworkName || '',
    finishPhotos: store.current?.finishPhotos || '',
    experience: store.current?.experience || '',
  }
}

onMounted(fetchData)

const handleEdit = () => {
  editing.value = true
}

const handleCancelEdit = () => {
  editing.value = false
  editForm.value = {
    artworkName: store.current?.artworkName || '',
    finishPhotos: store.current?.finishPhotos || '',
    experience: store.current?.experience || '',
  }
}

const handleSave = async () => {
  if (!editForm.value.artworkName) {
    ElNotification.warning('作品名不能为空')
    return
  }
  await store.update(artworkId.value, editForm.value)
  ElNotification.success('已更新')
  editing.value = false
  await fetchData()
}

const handleToggleRecommend = async () => {
  if (!store.current) return
  const newVal = store.current.isRecommended === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(
      newVal === 1 ? '确定设为推荐作品吗？' : '确定取消推荐吗？',
      '提示',
      { type: 'warning', center: true, confirmButtonText: '确定', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await store.setRecommend(artworkId.value, newVal)
  ElNotification.success(newVal === 1 ? '已设为推荐' : '已取消推荐')
  await fetchData()
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定删除此作品？此操作不可撤销！', '删除确认', {
      type: 'warning',
      center: true,
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }
  await store.remove(artworkId.value)
  ElNotification.success('已删除')
  router.back()
}
</script>

<template>
  <div class="artwork-detail-page" v-loading="store.loading">
    <el-button text class="back-btn" @click="router.back()">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <el-card v-if="store.current" class="detail-card">
      <div class="header">
        <div class="header-left">
          <h1 class="artwork-name">
            <el-tag v-if="store.current.isRecommended === 1" type="danger" effect="dark" round>⭐ 推荐</el-tag>
            {{ store.current.artworkName }}
          </h1>
          <div class="meta">
            <span>作者：<b>{{ store.current.authorName || store.current.authorId }}</b></span>
            <span>任务编号：<code>{{ store.current.taskId }}</code></span>
            <span>{{ formatDate(store.current.createTime) }}</span>
            <span><el-icon><View /></el-icon> {{ store.current.viewCount }} 次浏览</span>
          </div>
        </div>
        <div class="header-right">
          <el-button v-if="canRecommend" @click="handleToggleRecommend">
            <el-icon><StarFilled v-if="store.current.isRecommended === 1" /><Star v-else /></el-icon>
            {{ store.current.isRecommended === 1 ? '取消推荐' : '设为推荐' }}
          </el-button>
          <el-button v-if="isMine && !editing" type="primary" @click="handleEdit">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button v-if="isMine" type="danger" plain @click="handleDelete">删除</el-button>
        </div>
      </div>

      <el-divider />

      <!-- 编辑模式 -->
      <template v-if="editing">
        <el-form :model="editForm" label-width="100px">
          <el-form-item label="作品名">
            <el-input v-model="editForm.artworkName" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="成品照片">
            <el-input
              v-model="editForm.finishPhotos"
              type="textarea"
              :rows="3"
              placeholder="多个 URL 用逗号分隔"
            />
          </el-form-item>
          <el-form-item label="心得总结">
            <el-input
              v-model="editForm.experience"
              type="textarea"
              :rows="6"
              maxlength="2000"
              show-word-limit
              placeholder="分享你的制作心得、参数选择、踩坑经验..."
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存</el-button>
            <el-button @click="handleCancelEdit">取消</el-button>
          </el-form-item>
        </el-form>
      </template>

      <!-- 浏览模式 -->
      <template v-else>
        <div v-if="store.current.previewImage || store.current.finishPhotos" class="photos">
          <el-image
            v-if="store.current.previewImage"
            :src="store.current.previewImage"
            fit="contain"
            class="main-photo"
          />
          <div v-if="store.current.finishPhotos" class="sub-photos">
            <el-image
              v-for="(url, i) in store.current.finishPhotos.split(',').filter(Boolean)"
              :key="i"
              :src="url"
              fit="cover"
              class="sub-photo"
              :preview-src-list="store.current.finishPhotos.split(',').filter(Boolean)"
            />
          </div>
        </div>

        <h3 class="section-title">💡 心得总结</h3>
        <div class="experience">
          <template v-if="store.current.experience">
            <p v-for="(line, i) in store.current.experience.split('\n')" :key="i">{{ line }}</p>
          </template>
          <el-empty v-else description="作者还没有写心得" :image-size="80" />
        </div>
      </template>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.artwork-detail-page {
  padding: 0;
}
.back-btn {
  margin-bottom: $spacing-small;
}
.detail-card {
  max-width: 960px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: $spacing-medium;
  flex-wrap: wrap;
}
.artwork-name {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: $spacing-small;
}
.meta {
  display: flex;
  gap: $spacing-base;
  flex-wrap: wrap;
  font-size: 13px;
  color: $text-secondary;
  code {
    padding: 2px 6px;
    background: $bg-base;
    border-radius: 4px;
    font-family: monospace;
  }
  .el-icon { vertical-align: -2px; }
}
.header-right {
  display: flex;
  gap: $spacing-small;
  flex-wrap: wrap;
}
.photos {
  margin-bottom: $spacing-large;
  .main-photo {
    width: 100%;
    max-height: 400px;
    background: $bg-base;
    border-radius: 8px;
  }
  .sub-photos {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: $spacing-small;
    margin-top: $spacing-small;
    .sub-photo {
      width: 100%;
      aspect-ratio: 1;
      border-radius: 4px;
      cursor: pointer;
    }
  }
}
.section-title {
  margin: $spacing-large 0 $spacing-small;
  font-size: 18px;
  font-weight: 500;
  color: $text-primary;
}
.experience {
  background: $bg-base;
  border-radius: 8px;
  padding: $spacing-medium;
  line-height: 1.8;
  color: $text-regular;
  p {
    margin: 0 0 4px;
  }
}
</style>
