<script setup lang="ts">
/**
 * 空状态占位
 */
import { Box } from '@element-plus/icons-vue'

interface Props {
  description?: string
  showIcon?: boolean
}

withDefaults(defineProps<Props>(), {
  description: '暂无数据',
  showIcon: true,
})
</script>

<template>
  <div class="empty-state">
    <el-icon v-if="showIcon" :size="60" color="#c0c4cc">
      <Box />
    </el-icon>
    <p class="empty-description">{{ description }}</p>
    <slot />
  </div>
</template>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl $spacing-medium;
  color: $text-secondary;
}
.empty-description {
  margin: $spacing-small 0;
  font-size: $font-size-base;
  color: $text-secondary;
}
</style>