<script setup lang="ts">
/**
 * 作品推荐管理（M4 - 管理端）
 *
 * 技术骨干设置/取消推荐
 */
import { onMounted, ref } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useArtworkStore } from '@/stores/artwork'
import { formatDate } from '@/utils/format'
import { RecommendedFlag } from '@/utils/enum'

const store = useArtworkStore()

const filter = ref({
  page: 1,
  size: 20,
  keyword: '',
})

const fetchData = async () => {
  await store.fetchList({
    ...filter.value,
    sortBy: 'recommended',
  })
}

onMounted(fetchData)

const onSearch = () => {
  filter.value.page = 1
  fetchData()
}

const onPageChange = (page: number) => {
  filter.value.page = page
  fetchData()
}

const handleToggle = async (item: any) => {
  const newVal = item.isRecommended === RecommendedFlag.YES ? RecommendedFlag.NO : RecommendedFlag.YES
  try {
    await ElMessageBox.confirm(
      newVal === RecommendedFlag.YES
        ? `确定将「${item.artworkName}」设为推荐吗？`
        : `确定取消「${item.artworkName}」的推荐吗？`,
      '提示',
      { type: 'warning', center: true, confirmButtonText: '确定', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await store.setRecommend(item.artworkId, newVal)
  ElNotification.success(newVal === RecommendedFlag.YES ? '已设为推荐' : '已取消推荐')
  await fetchData()
}
</script>

<template>
  <div class="admin-artwork-recommend-page">
    <PageHeader title="作品推荐管理">
      <el-input
        v-model="filter.keyword"
        placeholder="按作品名搜索"
        clearable
        style="width: 240px"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-button type="primary" @click="onSearch">搜索</el-button>
      <el-button @click="fetchData">刷新</el-button>
    </PageHeader>

    <el-card v-loading="store.loading">
      <EmptyState
        v-if="!store.list || store.list.list.length === 0"
        description="还没有作品"
        hint="社员完成打印任务后会自动归档作品"
      />

      <el-table v-else :data="store.list.list" stripe>
        <el-table-column prop="artworkId" label="ID" width="80" />
        <el-table-column prop="artworkName" label="作品名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="authorId" label="作者" width="120" />
        <el-table-column label="预览" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.previewImage"
              :src="row.previewImage"
              fit="cover"
              style="width: 50px; height: 50px; border-radius: 4px"
              :preview-src-list="[row.previewImage]"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" sortable />
        <el-table-column label="推荐状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isRecommended === RecommendedFlag.YES" type="danger" effect="dark">⭐ 推荐</el-tag>
            <el-tag v-else type="info" effect="dark">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.isRecommended === RecommendedFlag.YES ? 'warning' : 'primary'"
              @click="handleToggle(row)"
            >
              <el-icon><StarFilled v-if="row.isRecommended === RecommendedFlag.YES" /><Star v-else /></el-icon>
              {{ row.isRecommended === RecommendedFlag.YES ? '取消推荐' : '设为推荐' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="store.list && store.list.total > filter.size" class="pagination-wrap">
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
.admin-artwork-recommend-page { padding: 0; }
.pagination-wrap {
  display: flex; justify-content: center; margin-top: $spacing-large;
}
</style>
