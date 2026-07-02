<script setup lang="ts">
/**
 * EmptyState - 空状态展示（v2.7 简化版）
 *
 * v2.7：取消 illustration 插图 prop
 *       直接用 Element Plus 的 el-empty，加自定义描述文字
 */
import { Box } from '@element-plus/icons-vue'

interface Props {
  description?: string
  hint?: string
  icon?: any
  size?: number
}

withDefaults(defineProps<Props>(), {
  description: '暂无数据',
  hint: '',
  icon: () => Box,
  size: 100,
})
</script>

<template>
  <div class="empty-state">
    <el-empty :description="description" :image-size="size">
      <template #image>
        <el-icon :size="size" color="#94A3B8"><component :is="icon" /></el-icon>
      </template>
    </el-empty>
    <p v-if="hint" class="empty-hint">{{ hint }}</p>
  </div>
</template>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 20px;

  :deep(.el-empty__description) {
    color: #5C6B82;
  }
}
.empty-hint {
  margin: 0;
  font-size: 13px;
  color: #8693A6;
  text-align: center;
  max-width: 320px;
  line-height: 1.5;
}
</style>
