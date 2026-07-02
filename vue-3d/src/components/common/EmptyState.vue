<script setup lang="ts">
/**
 * 空状态占位
 *
 * 用法：
 *   <EmptyState description="暂无任务" hint="提交一个新任务试试？" />
 *   <EmptyState illustration="empty-task" description="还没有任务" hint="提交第一个打印任务开始吧">
 *     <el-button type="primary" @click="router.push('/task/apply')">立即申请</el-button>
 *   </EmptyState>
 */
import { Box } from '@element-plus/icons-vue'
import emptyTask from '@/assets/member/empty-task.png'
import emptyProject from '@/assets/member/empty-project.png'
import emptyArtwork from '@/assets/member/empty-artwork.png'
import emptyQueue from '@/assets/member/empty-queue.png'

interface Props {
  /** 主标题 */
  description?: string
  /** 辅助提示（副标题） */
  hint?: string
  /** 是否显示图标 */
  showIcon?: boolean
  /** 自定义图标名（Element Plus icon） */
  icon?: string
  /** 插图资源 key，可选 'empty-task' / 'empty-project' / 'empty-artwork' / 'empty-queue' */
  illustration?: 'empty-task' | 'empty-project' | 'empty-artwork' | 'empty-queue' | null
}

withDefaults(defineProps<Props>(), {
  description: '暂无数据',
  hint: '',
  showIcon: true,
  icon: '',
  illustration: null,
})

const illustrationMap: Record<string, string> = {
  'empty-task': emptyTask,
  'empty-project': emptyProject,
  'empty-artwork': emptyArtwork,
  'empty-queue': emptyQueue,
}
</script>

<template>
  <div class="empty-state">
    <img
      v-if="illustration && illustrationMap[illustration]"
      :src="illustrationMap[illustration]"
      :alt="description"
      class="empty-illustration"
    />
    <el-icon v-else-if="showIcon" :size="72" color="#dcdfe6" class="empty-icon">
      <component :is="icon || Box" />
    </el-icon>
    <p class="empty-description">{{ description }}</p>
    <p v-if="hint" class="empty-hint">{{ hint }}</p>
    <div v-if="$slots.default" class="empty-actions">
      <slot />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl $spacing-medium;
  color: var(--text-secondary);
  text-align: center;
}

.empty-illustration {
  width: 200px;
  height: auto;
  max-width: 80%;
  margin-bottom: $spacing-base;
  animation: float 4s ease-in-out infinite;
}

.empty-icon {
  margin-bottom: $spacing-base;
  opacity: 0.7;
}

.empty-description {
  margin: 0;
  font-size: 18px;
  color: var(--text-regular);
  font-weight: 500;
}

.empty-hint {
  margin: $spacing-small 0 0;
  font-size: 14px;
  color: var(--text-secondary);
  max-width: 360px;
  line-height: 1.6;
}

.empty-actions {
  margin-top: $spacing-large;
  display: flex;
  gap: $spacing-small;
  flex-wrap: wrap;
  justify-content: center;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}
</style>
