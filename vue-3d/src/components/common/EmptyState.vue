<script setup lang="ts">
/**
 * 空状态占位
 *
 * 用法：
 *   <EmptyState description="暂无任务" hint="提交一个新任务试试？" />
 *   <EmptyState description="暂无项目">
 *     <el-button type="primary" @click="router.push('/admin/project/create')">创建项目</el-button>
 *   </EmptyState>
 */
import { Box } from '@element-plus/icons-vue'

interface Props {
  /** 主标题 */
  description?: string
  /** 辅助提示（副标题） */
  hint?: string
  /** 是否显示图标 */
  showIcon?: boolean
  /** 自定义图标名（Element Plus icon） */
  icon?: string
}

withDefaults(defineProps<Props>(), {
  description: '暂无数据',
  hint: '',
  showIcon: true,
  icon: '',
})
</script>

<template>
  <div class="empty-state">
    <el-icon v-if="showIcon" :size="72" color="#dcdfe6" class="empty-icon">
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
.empty-icon {
  margin-bottom: $spacing-base;
  opacity: 0.7;
}
.empty-description {
  margin: 0;
  font-size: $font-size-large;
  color: var(--text-regular);
  font-weight: 500;
}
.empty-hint {
  margin: $spacing-small 0 0;
  font-size: $font-size-small;
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
</style>