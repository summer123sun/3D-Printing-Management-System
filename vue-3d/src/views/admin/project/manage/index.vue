<script setup lang="ts">
/**
 * 项目管理列表（**B** - 管理端）
 *
 * 显示所有项目 + 行内操作（查看/编辑/完成/取消）
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useProjectStore } from '@/stores/project'
import { ProjectStatusText, ProjectTypeText } from '@/types/project'
import { formatDate } from '@/utils/format'

const router = useRouter()
const projectStore = useProjectStore()

const filter = ref({
  status: undefined as number | undefined,
  keyword: '',
})

const fetchData = async () => {
  await projectStore.fetchList({
    page: 1,
    size: 50,
    status: filter.value.status,
    keyword: filter.value.keyword || undefined,
  })
}
onMounted(fetchData)

const handleComplete = async (id: number) => {
  await projectStore.complete(id)
  ElMessage.success('已标记完成')
  fetchData()
}

const handleCancel = async (id: number) => {
  await projectStore.cancel(id)
  ElMessage.success('已取消')
  fetchData()
}

const statusTagType = (s: number) => {
  return s === 0 ? 'info' : s === 1 ? 'primary' : s === 2 ? 'success' : 'danger'
}
</script>

<template>
  <div class="admin-project-manage-page">
    <PageHeader title="项目管理">
      <el-button type="primary" @click="router.push('/admin/project/create')">
        <el-icon><Plus /></el-icon> 创建项目
      </el-button>
      <el-input v-model="filter.keyword" placeholder="按名称搜索" clearable style="width: 200px" @keyup.enter="fetchData" @clear="fetchData" />
      <el-button @click="fetchData">搜索</el-button>
    </PageHeader>

    <el-card v-loading="projectStore.loading">
      <template v-if="!projectStore.projectList || projectStore.projectList.list.length === 0">
        <EmptyState
          description="还没有项目"
          hint="立项你的第一个项目，把分散的任务组织起来。"
        >
          <el-button type="primary" @click="router.push('/project/create')">
            <el-icon><Plus /></el-icon> 创建项目
          </el-button>
        </EmptyState>
      </template>

      <template v-else>
        <el-table :data="projectStore.projectList.list" stripe>
          <el-table-column prop="projectId" label="ID" width="80" />
          <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              {{ ProjectTypeText[row.projectType as keyof typeof ProjectTypeText] }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTagType(row.status)">{{ ProjectStatusText[row.status as keyof typeof ProjectStatusText] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="leaderId" label="负责人" width="100" />
          <el-table-column label="开始日期" width="120">
            <template #default="{ row }">{{ formatDate(row.startDate, 'YYYY-MM-DD') }}</template>
          </el-table-column>
          <el-table-column label="预算/实际" width="120">
            <template #default="{ row }">
              {{ row.budget || 0 }} / {{ row.actualCost || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" size="small" @click="router.push(`/project/${row.projectId}`)">查看</el-button>
              <el-button v-if="row.status === 1" text type="success" size="small" @click="handleComplete(row.projectId)">完成</el-button>
              <el-button v-if="row.status !== 2" text type="danger" size="small" @click="handleCancel(row.projectId)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Plus } from '@element-plus/icons-vue'
export default { name: 'AdminProjectManagePage' }
</script>

<style lang="scss" scoped>
.admin-project-manage-page {
  padding: 0;
}
</style>