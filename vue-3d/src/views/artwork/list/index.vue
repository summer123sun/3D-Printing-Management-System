<script setup lang="ts">
/**
 * 作品列表（M4 - 公开浏览）
 * v2.3 重构：增强卡片样式 + HeroBanner
 */
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElNotification } from 'element-plus'
import { useArtworkStore } from '@/stores/artwork'
import { useAuthStore } from '@/stores/auth'
import { useMemberStyle } from '@/composables/useMemberStyle'
import { RecommendedFlag } from '@/utils/enum'
import EmptyState from '@/components/common/EmptyState.vue'
import HeroBanner from '@/components/member/HeroBanner.vue'
import MemberCard from '@/components/member/MemberCard.vue'
import { formatDate } from '@/utils/format'
import type { ArtworkQuery } from '@/types/artwork'

const router = useRouter()
const store = useArtworkStore()
const authStore = useAuthStore()
const { isMember } = useMemberStyle()

const filter = ref<ArtworkQuery>({
  page: 1,
  size: 12,
  keyword: '',
  sortBy: 'latest',
})

const artworkList = computed(() => store.list?.list ?? [])

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
    // 业务错误通知已由 request.ts 拦截器自动弹
  }
}

import { Role } from '@/utils/enum'
const canDelete = () => {
  if (!authStore.user) return false
  return authStore.user.role === Role.PRESIDENT || authStore.user.role === Role.TECH_LEAD
}
</script>

<template>
  <div class="artwork-list-page">
    <!-- 成员端 HeroBanner -->
    <HeroBanner
      v-if="isMember"
      title="作品库"
      subtitle="看看社团成员们的得意之作 · 也许下一个就是你"
    >
      <template #actions>
        <el-input
          v-model="filter.keyword"
          placeholder="按作品名/作者名搜索"
          clearable
          round
          size="large"
          style="width: 280px"
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select v-model="filter.sortBy" size="large" style="width: 140px" @change="onSortChange">
          <el-option label="最新" value="latest" />
          <el-option label="最热" value="hottest" />
          <el-option label="推荐优先" value="recommended" />
        </el-select>
        <el-button type="primary" size="large" round @click="onSearch">搜索</el-button>
      </template>
    </HeroBanner>

    <!-- 后台端 -->
    <el-card v-else class="filter-card">
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

    <MemberCard v-if="isMember" v-loading="store.loading" padding="32px">
      <EmptyState
        v-if="artworkList.length === 0"
        description="还没有作品"
        hint="打印完成的任务会自动归档到这里哦~"
      />

      <div v-else class="artwork-grid">
        <div
          v-for="item in artworkList"
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
            >
              <template #error>
                <div class="cover-placeholder">
                  <el-icon :size="48"><Picture /></el-icon>
                  <span>暂无预览</span>
                </div>
              </template>
            </el-image>
            <div v-else class="cover-placeholder">
              <el-icon :size="48"><Picture /></el-icon>
              <span>暂无预览</span>
            </div>
            <div v-if="item.isRecommended === RecommendedFlag.YES" class="recommend-badge">⭐ 推荐</div>
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
    </MemberCard>

    <el-card v-else v-loading="store.loading" class="content-card">
      <EmptyState
        v-if="artworkList.length === 0"
        description="还没有作品"
        hint="打印完成的任务会自动归档到这里哦~"
      />

      <div v-else class="artwork-grid">
        <div
          v-for="item in artworkList"
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
            >
              <template #error>
                <div class="cover-placeholder">
                  <el-icon :size="48"><Picture /></el-icon>
                  <span>暂无预览</span>
                </div>
              </template>
            </el-image>
            <div v-else class="cover-placeholder">
              <el-icon :size="48"><Picture /></el-icon>
              <span>暂无预览</span>
            </div>
            <div v-if="item.isRecommended === RecommendedFlag.YES" class="recommend-badge">⭐ 推荐</div>
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
            <div v-if="canDelete()" class="artwork-actions" @click.stop>
              <el-button size="small" type="danger" @click="handleDelete(item.artworkId)">删除</el-button>
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
  display: flex;
  flex-direction: column;
  gap: $spacing-large;
}
.filter-card {
  margin-bottom: 0;
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
  gap: $spacing-large;
}
.artwork-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 2px 12px rgba(10, 37, 64, 0.06);
  border: 1px solid var(--border-extra-light);
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 28px rgba(10, 37, 64, 0.15);
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
  background: linear-gradient(135deg, #E8EEF5 0%, #F0F4F8 100%);
  color: #909399;
  gap: 8px;
}
.recommend-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #F2A93B 0%, #CCB000 100%);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(242, 169, 59, 0.4);
}
.view-count {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(10, 37, 64, 0.65);
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  backdrop-filter: blur(8px);
}
.artwork-info {
  padding: 16px 18px 20px;
}
.artwork-name {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
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
  margin-bottom: 8px;
}
.artwork-experience {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.5;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
