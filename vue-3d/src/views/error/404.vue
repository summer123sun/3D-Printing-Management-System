<script setup lang="ts">
import { useRouter } from 'vue-router'
import { DocumentRemove } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<template>
  <div class="error-page">
    <el-icon :size="80" color="#909399"><DocumentRemove /></el-icon>
    <h1>404</h1>
    <p class="error-desc">抱歉，你访问的页面不存在</p>
    <el-button type="primary" @click="router.push('/')">返回首页</el-button>
  </div>
</template>

<style lang="scss" scoped>
.error-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-base;
  background: var(--bg-page);
  h1 {
    font-size: 72px;
    color: var(--text-primary);
    margin: 0;
  }
  .error-desc {
    color: var(--text-secondary);
    margin: 0;
  }
}
</style>