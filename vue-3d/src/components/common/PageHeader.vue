<script setup lang="ts">
/**
 * 通用页头：标题 + 面包屑 + 操作区
 */
import { ArrowLeft } from '@element-plus/icons-vue'

interface Crumb {
  title: string
  path?: string
}

interface Props {
  title: string
  crumbs?: Crumb[]
  showBack?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  crumbs: () => [],
  showBack: false,
})

const emit = defineEmits<{
  (e: 'back'): void
}>()

const handleBack = () => {
  emit('back')
}
</script>

<template>
  <div class="page-header">
    <div class="page-header-left">
      <el-button v-if="props.showBack" text @click="handleBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2 class="page-title">{{ props.title }}</h2>
      <el-breadcrumb v-if="props.crumbs.length > 0" separator="/" class="page-crumbs">
        <el-breadcrumb-item v-for="(c, i) in props.crumbs" :key="i">
          {{ c.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="page-header-right">
      <slot />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-medium;
  padding-bottom: $spacing-small;
}
.page-header-left {
  display: flex;
  align-items: center;
  gap: $spacing-small;
}
.page-title {
  font-size: $font-size-title;
  font-weight: 600;
  margin: 0;
}
.page-crumbs {
  margin-left: $spacing-small;
  font-size: $font-size-small;
}
.page-header-right {
  display: flex;
  gap: $spacing-small;
}
</style>