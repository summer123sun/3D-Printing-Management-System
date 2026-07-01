<script setup lang="ts">
/**
 * 管理员文件管理（v2.2 新增）
 *
 * 功能：
 * - 列出所有用户上传的 STL / 图片 / 项目文件
 * - 按类型筛选
 * - 按名称搜索
 * - 管理员可强制删除任意文件
 * - 展示文件大小、修改时间、下载链接
 */
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { Delete, Download, FolderOpened, Refresh, Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { listFiles, adminDeleteFile } from '@/api/file'

interface FileEntry {
  name: string
  type: string
  path: string
  size: number
  lastModified: number
}

const fileList = ref<FileEntry[]>([])
const loading = ref(false)

const filterType = ref<string | undefined>(undefined)
const filterKeyword = ref('')

const TYPE_LABELS: Record<string, string> = {
  stl: 'STL 模型',
  img: '图片',
  project: '项目文件',
}

const fetchData = async () => {
  loading.value = true
  try {
    fileList.value = await listFiles(filterType.value)
  } catch (e) {
    console.error('[文件管理] 加载失败：', e)
  } finally {
    loading.value = false
  }
}
onMounted(fetchData)

const filteredList = computed(() => {
  const kw = filterKeyword.value.trim().toLowerCase()
  if (!kw) return fileList.value
  return fileList.value.filter((f) => f.name.toLowerCase().includes(kw) || f.path.toLowerCase().includes(kw))
})

const onTypeChange = () => fetchData()
const onSearch = () => { /* computed 自动响应 */ }

const handleDownload = (path: string, name: string) => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  const url = `${baseUrl}/file/download/${path}`
  const a = document.createElement('a')
  a.href = url
  a.download = name
  a.target = '_blank'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 <b>${row.path}</b> 吗？<br/><br/>该操作不可撤销，引用此文件的任务 / 作品可能显示异常。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '强制删除',
        cancelButtonText: '取消',
        dangerouslyUseHTMLString: true,
        confirmButtonClass: 'el-button--danger',
      }
    )
  } catch {
    return
  }
  try {
    await adminDeleteFile(row.path)
    ElNotification.success('文件已删除')
    fetchData()
  } catch {
    // 错误通知已由 request.ts 拦截器弹
  }
}

const formatSize = (bytes: number): string => {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`
}

const formatDate = (ms: number): string => {
  const d = new Date(ms)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const typeTagType = (type: string): 'primary' | 'success' | 'warning' | 'info' => {
  if (type === 'stl') return 'primary'
  if (type === 'img') return 'success'
  return 'warning'
}
</script>

<template>
  <div class="admin-file-manage-page">
    <PageHeader title="文件管理" :show-back="false">
      <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 140px" @change="onTypeChange">
        <el-option label="STL 模型" value="stl" />
        <el-option label="图片" value="img" />
        <el-option label="项目文件" value="project" />
      </el-select>
      <el-input
        v-model="filterKeyword"
        placeholder="按文件名搜索"
        clearable
        style="width: 240px"
        @keyup.enter="onSearch"
        @clear="onSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="fetchData">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </PageHeader>

    <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
      <p>本页仅 <b>社长 + 技术骨干</b> 可见，可以管理所有用户上传的 STL / 图片 / 项目文件</p>
      <p>删除文件会影响引用它的任务 / 作品（可能导致预览图失效），操作前请确认</p>
    </el-alert>

    <el-card v-loading="loading">
      <template v-if="filteredList.length === 0 && !loading">
        <EmptyState
          :description="filterKeyword || filterType ? '没有匹配的文件' : '还没有用户上传文件'"
          :hint="filterKeyword || filterType ? '试试清空筛选条件' : '用户在打印任务申请 / 作品登记时会自动上传文件'"
        >
          <el-icon :size="48"><FolderOpened /></el-icon>
        </EmptyState>
      </template>

      <el-table v-else :data="filteredList" stripe>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small" effect="dark">
              {{ TYPE_LABELS[row.type] || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名" min-width="240" show-overflow-tooltip />
        <el-table-column prop="path" label="相对路径" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <code class="path-code">{{ row.path }}</code>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="120" sortable :sort-by="(row: any) => formatSize(row.size)">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column label="修改时间" width="180" sortable :sort-by="(row: any) => String(row.lastModified)">
          <template #default="{ row }">{{ formatDate(row.lastModified) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleDownload(row.path, row.name)">
              <el-icon><Download /></el-icon> 下载
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.admin-file-manage-page { padding: 0; }
.path-code {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--bg-base);
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
