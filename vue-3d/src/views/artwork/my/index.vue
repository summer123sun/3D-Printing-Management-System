<script setup lang="ts">
/**
 * 我的作品（M4）
 * v2.3 重构：HeroBanner + 卡片样式
 */
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElNotification } from 'element-plus'
import { Picture, View, Edit, Plus } from '@element-plus/icons-vue'
import { useArtworkStore } from '@/stores/artwork'
import { useMemberStyle } from '@/composables/useMemberStyle'
import EmptyState from '@/components/common/EmptyState.vue'
import HeroBanner from '@/components/member/HeroBanner.vue'
import MemberCard from '@/components/member/MemberCard.vue'
import { formatDate } from '@/utils/format'
import { RecommendedFlag } from '@/utils/enum'

const router = useRouter()
const store = useArtworkStore()
const { isMember, isNewbie } = useMemberStyle()

const filter = ref({
  page: 1,
  size: 12,
  sortBy: 'latest' as 'latest' | 'hottest',
})

const myArtworkList = computed(() => store.myArtworks?.list ?? [])

const fetchData = async () => {
  await store.fetchMine(filter.value)
}

onMounted(fetchData)

const onPageChange = (page: number) => {
  filter.value.page = page
  fetchData()
}

const onSortChange = () => {
  filter.value.page = 1
  fetchData()
}

const viewDetail = (id: number) => {
  router.push(`/artwork/${id}`)
}

const handleEdit = (id: number) => {
  router.push(`/artwork/edit/${id}`)
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
  await store.remove(id)
  ElNotification.success('作品已删除')
  fetchData()
}
</script>

<template>
  <div class="artwork-my-page">
    <!-- 成员端 HeroBanner -->
    <HeroBanner
      v-if="isMember"
      title="我的作品"
      subtitle="记录你的 3D 创作历程 · 分享你的灵感与心得"
      illustration="hero-home"
      :is-newbie="isNewbie"
      newbie-tip="完成打印任务后，请到这里点击「登记作品」上传照片 + 心得。"
    >
      <template #actions>
        <el-button type="primary" size="large" round @click="router.push('/artwork/create')">
          <el-icon><Plus /></el-icon> 登记新作品
        </el-button>
      </template>
    </HeroBanner>

    <el-card v-else class="filter-card">
      <div class="filter-row">
        <el-select v-model="filter.sortBy" style="width: 140px" @change="onSortChange">
          <el-option label="最新" value="latest" />
          <el-option label="最热" value="hottest" />
        </el-select>
        <el-button @click="fetchData">刷新</el-button>
      </div>
    </el-card>

    <!-- 成员端：MemberCard 包裹 -->
    <MemberCard v-if="isMember" v-loading="store.loading" padding="32px">
      <EmptyState
        v-if="myArtworkList.length === 0"
        illustration="empty-artwork"
        description="你还没有作品"
        hint="完成打印任务后，请到这里点击「登记作品」上传照片 + 心得~"
      >
        <el-button type="primary" round size="large" @click="router.push('/artwork/create')">
          登记第一个作品
        </el-button>
      </EmptyState>

      <div v-else class="artwork-grid">
        <MemberCard
          v-for="item in myArtworkList"
          :key="item.artworkId"
          hoverable
          padding="0"
          :radius="16"
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
            <div v-if="item.isRecommended === RecommendedFlag.YES" class="recommend-badge">⭐ 推荐</div>
            <div class="view-count">
              <el-icon><View /></el-icon> {{ item.viewCount || 0 }}
            </div>
          </div>
          <div class="artwork-info">
            <h3 class="artwork-name">{{ item.artworkName }}</h3>
            <div class="artwork-meta">
              <span>{{ formatDate(item.createTime) }}</span>
            </div>
            <p v-if="item.experience" class="artwork-experience">
              {{ item.experience.slice(0, 50) }}{{ item.experience.length > 50 ? '...' : '' }}
            </p>
          </div>
          <template #footer>
            <div class="artwork-actions">
              <el-button size="small" text @click.stop="viewDetail(item.artworkId)">查看</el-button>
              <el-button size="small" type="primary" plain @click.stop="handleEdit(item.artworkId)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button size="small" type="danger" plain @click.stop="handleDelete(item.artworkId)">删除</el-button>
            </div>
          </template>
        </MemberCard>
      </div>

      <div v-if="store.myArtworks && store.myArtworks.total > filter.size" class="pagination-wrap">
        <el-pagination
          :current-page="filter.page"
          :page-size="filter.size"
          :total="store.myArtworks.total"
          layout="prev, pager, next, total"
          @current-change="onPageChange"
        />
      </div>
    </MemberCard>

    <el-card v-else v-loading="store.loading" class="content-card">
      <EmptyState
        v-if="myArtworkList.length === 0"
        description="你还没有作品"
        hint="完成打印任务后，请到这里点击「登记作品」上传照片 + 心得~"
      />
      <div v-else class="artwork-grid">
        <div
          v-for="item in myArtworkList"
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
            <div v-if="item.isRecommended === RecommendedFlag.YES" class="recommend-badge">⭐ 推荐</div>
            <div class="view-count">
              <el-icon><View /></el-icon> {{ item.viewCount || 0 }}
            </div>
          </div>
          <div class="artwork-info">
            <h3 class="artwork-name">{{ item.artworkName }}</h3>
            <div class="artwork-meta">
              <span>{{ formatDate(item.createTime) }}</span>
            </div>
            <p v-if="item.experience" class="artwork-experience">
              {{ item.experience.slice(0, 50) }}{{ item.experience.length > 50 ? '...' : '' }}
            </p>
            <div class="artwork-actions" @click.stop>
              <el-button size="small" @click="viewDetail(item.artworkId)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(item.artworkId)">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(item.artworkId)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
      <div v-if="store.myArtworks && store.myArtworks.total > filter.size" class="pagination-wrap">
        <el-pagination
          :current-page="filter.page"
          :page-size="filter.size"
          :total="store.myArtworks.total"
          layout="prev, pager, next, total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.artwork-my-page {
  display: flex;
  flex-direction: column;
  gap: $spacing-large;
}
.filter-card { margin-bottom: 0; }
.filter-row { display: flex; gap: $spacing-small; }
.content-card { min-height: 500px; }
.artwork-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: $spacing-large;
}
.artwork-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--bg-base);
  overflow: hidden;
}
.cover-img { width: 100%; height: 100%; }
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #E8EEF5 0%, #F0F4F8 100%);
  color: #909399; gap: 8px;
}
.recommend-badge {
  position: absolute; top: 10px; left: 10px;
  background: linear-gradient(135deg, #F2A93B 0%, #CCB000 100%);
  color: white; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600;
  box-shadow: 0 2px 8px rgba(242, 169, 59, 0.4);
}
.view-count {
  position: absolute; bottom: 10px; right: 10px;
  background: rgba(10, 37, 64, 0.65); color: white;
  padding: 4px 10px; border-radius: 12px; font-size: 12px;
  display: flex; align-items: center; gap: 4px;
  backdrop-filter: blur(8px);
}
.artwork-info { padding: 16px 20px 8px; }
.artwork-name {
  margin: 0 0 6px; font-size: 16px; font-weight: 600;
  color: var(--text-primary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.artwork-meta {
  font-size: 12px; color: var(--text-secondary); margin-bottom: 8px;
}
.artwork-experience {
  margin: 8px 0 0; font-size: 13px; color: var(--text-regular);
  line-height: 1.5; min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.artwork-actions {
  display: flex; gap: 4px; align-items: center; width: 100%;
}
.pagination-wrap {
  display: flex; justify-content: center; margin-top: $spacing-large;
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
</style>
