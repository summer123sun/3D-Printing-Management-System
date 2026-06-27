<script setup lang="ts">
/**
 * 我的作品（M4）
 *
 * 与作品列表类似，但用 /api/artwork/my 接口
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElNotification } from 'element-plus'
import { Picture, View } from '@element-plus/icons-vue'
import { useArtworkStore } from '@/stores/artwork'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatDate } from '@/utils/format'

const router = useRouter()
const store = useArtworkStore()

const filter = ref({
  page: 1,
  size: 12,
  sortBy: 'latest' as 'latest' | 'hottest',
})

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
    <el-card class="filter-card">
      <div class="filter-row">
        <el-select v-model="filter.sortBy" style="width: 140px" @change="onSortChange">
          <el-option label="最新" value="latest" />
          <el-option label="最热" value="hottest" />
        </el-select>
        <el-button @click="fetchData">刷新</el-button>
      </div>
    </el-card>

    <el-card v-loading="store.loading" class="content-card">
      <EmptyState
        v-if="!store.myArtworks || store.myArtworks.list.length === 0"
        description="你还没有作品"
        hint="完成打印任务后会自动归档到这里~"
      />

      <div v-else class="artwork-grid">
        <div
          v-for="item in store.myArtworks.list"
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
            <div v-if="item.isRecommended === 1" class="recommend-badge">⭐ 推荐</div>
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
              <el-button size="small" type="danger" @click="handleDelete(item.artworkId)">
                删除
              </el-button>
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
  padding: 0;
}
.filter-card { margin-bottom: $spacing-medium; }
.filter-row { display: flex; gap: $spacing-small; }
.content-card { min-height: 500px; }
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
  background: #f5f7fa;
  overflow: hidden;
}
.cover-img { width: 100%; height: 100%; }
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #c0c4cc; gap: 8px;
}
.recommend-badge {
  position: absolute; top: 8px; left: 8px;
  background: linear-gradient(135deg, #f56c6c, #e04545);
  color: white; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 500;
}
.view-count {
  position: absolute; bottom: 8px; right: 8px;
  background: rgba(0, 0, 0, 0.5); color: white;
  padding: 2px 8px; border-radius: 12px; font-size: 12px;
  display: flex; align-items: center; gap: 4px;
}
.artwork-info { padding: $spacing-small $spacing-base; }
.artwork-name {
  margin: 0 0 4px; font-size: 15px; font-weight: 500;
  color: $text-primary;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.artwork-meta {
  font-size: 12px; color: $text-secondary; margin-bottom: 6px;
}
.artwork-experience {
  margin: 6px 0; font-size: 13px; color: $text-regular;
  line-height: 1.5; min-height: 40px;
}
.artwork-actions {
  display: flex; gap: $spacing-small; margin-top: $spacing-small;
}
.pagination-wrap {
  display: flex; justify-content: center; margin-top: $spacing-large;
}
</style>
