<script setup lang="ts">
/**
 * 作品列表（M4 - 公开浏览）
 *
 * 卡片网格 + 顶部筛选（材料/作者/排序）
 * 数据来自 /api/artwork
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElNotification } from 'element-plus'
import { useArtworkStore } from '@/stores/artwork'
import { useAuthStore } from '@/stores/auth'
import { RoleText } from '@/types/member'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatDate } from '@/utils/format'
import type { ArtworkQuery } from '@/types/artwork'

const router = useRouter()
const store = useArtworkStore()
const authStore = useAuthStore()

const filter = ref<ArtworkQuery>({
  page: 1,
  size: 12,
  keyword: '',
  sortBy: 'latest',
})

const fetchData = async () => {
  await store.fetchList({
    ...filter.value,
    authorId: undefined,
  })
}

onMounted(fetchData)

const onSearch = () => {
  filter.value.page = 1
  fetchData()
}

const onSortChange = () => {
  filter.value.page = 1
  fetchData()
}

const onPageChange = (page: number) => {
  filter.value.page = page
  fetchData()
}

const viewDetail = (id: number) => {
  router.push(`/artwork/${id}`)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定删除这个作品吗？推荐中的作品无法删除。',
      '删除确认',
      { type: 'warning', center: true, confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  try {
    await store.remove(id)
    ElNotification.success('作品已删除')
    fetchData()
  } catch {
    // 业务错误通知已由 request.ts 拦截器自动弹 ElNotification，这里不重复弹
  }
}

const canDelete = (authorId: string) => {
  if (!authStore.user) return false
  return authStore.user.studentId === authorId
}
</script>

<template>
  <div class="artwork-list-page">
    <el-card class="filter-card">
      <div class="filter-row">
        <el-input
          v-model="filter.keyword"
          placeholder="按作品名/作者名搜索"
          clearable
          style="width: 280px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select v-model="filter.sortBy" style="width: 140px" @change="onSortChange">
          <el-option label="最新" value="latest" />
          <el-option label="最热" value="hottest" />
          <el-option label="推荐优先" value="recommended" />
        </el-select>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="fetchData">刷新</el-button>
      </div>
    </el-card>

    <el-card v-loading="store.loading" class="content-card">
      <EmptyState
        v-if="!store.list || store.list.list.length === 0"
        description="还没有作品"
        hint="打印完成的任务会自动归档到这里哦~"
      />

      <div v-else class="artwork-grid">
        <div
          v-for="item in store.list.list"
          :key="item.artworkId"
          class="artwork-card"
          @click="viewDetail(item.artworkId)"
        >
          <div class="artwork-cover">
            <el-image
              v-if="item.previewImage"
              :src="item.previewImage"
              fit="cover"
              class="cover-img"
            />
            <div v-else class="cover-placeholder">
              <el-icon :size="48"><Picture /></el-icon>
              <span>暂无预览</span>
            </div>
            <div v-if="item.isRecommended === 1" class="recommend-badge">
              ⭐ 推荐
            </div>
            <div class="view-count">
              <el-icon><View /></el-icon> {{ item.viewCount || 0 }}
            </div>
          </div>
          <div class="artwork-info">
            <h3 class="artwork-name">{{ item.artworkName }}</h3>
            <div class="artwork-meta">
              <span>作者：{{ item.authorName || item.authorId }}</span>
              <span>{{ formatDate(item.createTime) }}</span>
            </div>
            <p v-if="item.experience" class="artwork-experience">
              {{ item.experience.slice(0, 50) }}{{ item.experience.length > 50 ? '...' : '' }}
            </p>
            <div v-if="canDelete(item.authorId)" class="artwork-actions" @click.stop>
              <el-button size="small" type="danger" @click="handleDelete(item.artworkId)">
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="store.list && store.list.total > filter.size!" class="pagination-wrap">
        <el-pagination
          :current-page="filter.page"
          :page-size="filter.size"
          :total="store.list.total"
          layout="prev, pager, next, total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.artwork-list-page {
  padding: 0;
}
.filter-card {
  margin-bottom: $spacing-medium;
}
.filter-row {
  display: flex;
  gap: $spacing-small;
  align-items: center;
}
.content-card {
  min-height: 500px;
}
.artwork-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: $spacing-medium;
}
.artwork-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
}
.artwork-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--bg-base);
  overflow: hidden;
}
.cover-img {
  width: 100%;
  height: 100%;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  gap: 8px;
}
.recommend-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: linear-gradient(135deg, #f56c6c, #e04545);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.view-count {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.artwork-info {
  padding: $spacing-small $spacing-base;
}
.artwork-name {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.artwork-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.artwork-experience {
  margin: 6px 0;
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.5;
  min-height: 40px;
}
.artwork-actions {
  display: flex;
  gap: $spacing-small;
  margin-top: $spacing-small;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: $spacing-large;
}
</style>
