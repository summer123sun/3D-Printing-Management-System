<script setup lang="ts">
/**
 * 项目列表（**B** - 社员端）
 *
 * Tab：我参与的 / 全部
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useProjectStore } from '@/stores/project'
import { ProjectStatusText, ProjectTypeText } from '@/types/project'
import { formatDate } from '@/utils/format'

const router = useRouter()
const projectStore = useProjectStore()

const activeTab = ref<'mine' | 'all'>('all')
const filter = ref({
  status: undefined as number | undefined,
  keyword: '',
})

const fetchData = async () => {
  await projectStore.fetchList({
    page: 1,
    size: 20,
    scope: activeTab.value,
    status: filter.value.status,
    keyword: filter.value.keyword || undefined,
  })
}
onMounted(fetchData)

const onTabChange = () => {
  filter.value.keyword = ''
  fetchData()
}
</script>

<template>
  <div class="project-list-page">
    <PageHeader title="项目">
      <el-input
        v-model="filter.keyword"
        placeholder="按名称搜索"
        clearable
        style="width: 200px"
        @keyup.enter="fetchData"
        @clear="fetchData"
      />
      <el-button @click="fetchData">搜索</el-button>
    </PageHeader>

    <el-card>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部项目" name="all" />
        <el-tab-pane label="我参与的" name="mine" />
      </el-tabs>

      <div v-loading="projectStore.loading">
        <template v-if="!projectStore.projectList || projectStore.projectList.list.length === 0">
          <EmptyState
            :description="activeTab === 'mine' ? '你还没参与任何项目' : '还没有项目'"
            :hint="activeTab === 'mine' ? '请联系管理员把你加入项目，或切换到【全部项目】看看。' : '立项第一个项目，把分散的打印任务组织起来。'"
          >
            <el-button v-if="activeTab === 'all'" type="primary" @click="router.push('/project/create')">
              <el-icon><Plus /></el-icon> 创建项目
            </el-button>
            <el-button v-else @click="activeTab = 'all'">查看全部项目</el-button>
          </EmptyState>
        </template>

        <el-table
          v-else
          :data="projectStore.projectList.list"
          stripe
          @row-click="(row: any) => router.push(`/project/${row.projectId}`)"
        >
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
          <el-table-column label="负责人" width="120">
            <template #default="{ row }">
              <el-tooltip :content="`学号：${row.leaderId}`" placement="top">
                <span>{{ row.leaderName || row.leaderId }}</span>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="开始日期" width="120">
            <template #default="{ row }">{{ formatDate(row.startDate, 'YYYY-MM-DD') }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" @click.stop="router.push(`/project/${row.projectId}`)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
function statusTagType(status: number): 'info' | 'primary' | 'success' | 'danger' {
  return status === 0 ? 'info' : status === 1 ? 'primary' : status === 2 ? 'success' : 'danger'
}
export default { name: 'ProjectListPage' }
</script>

<style lang="scss" scoped>
.project-list-page {
  padding: 0;
}
</style>