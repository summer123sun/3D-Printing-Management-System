<script setup lang="ts">
// 内容区：路由切换 + 不同页面不同动效
// transition name 从 route.meta.transition 读，默认 'page'（fade-in-up）
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const transitionName = computed(() => (route.meta?.transition as string) || 'page')
</script>

<template>
  <router-view v-slot="{ Component, route: r }">
    <transition :name="transitionName" mode="out-in" appear>
      <component :is="Component" :key="r.fullPath" />
    </transition>
  </router-view>
</template>

<style lang="scss" scoped>
// 这里只放 transition 的 fallback（具体动画在 styles/index.scss 全局定义）
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
