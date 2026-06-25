<script setup lang="ts">
/**
 * 待审批任务（B 负责开发）
 * @see 路径：/admin/task/pending
 */
</script>

<template>
  <div class="placeholder-page">
    <el-card>
      <h2>待审批任务</h2>
      <p>🚧 本页面由 <strong>B</strong> 开发，当前为占位页。</p>
      <p>请到对应 README.md 查看组件清单和接口要求。</p>
      <p class="path">路径：<code>/admin/task/pending</code></p>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.placeholder-page {
  padding: $spacing-medium;
}
h2 {
  margin: 0 0 $spacing-small;
  font-size: $font-size-title;
  color: $brand-color;
}
p {
  margin: $spacing-small 0;
  color: $text-regular;
}
.path {
  font-size: $font-size-small;
  color: $text-secondary;
  code {
    padding: 2px 6px;
    background: $bg-base;
    border-radius: $border-radius-small;
    color: $brand-color;
  }
}
</style>
