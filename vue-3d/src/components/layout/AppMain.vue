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
    <!--
      ✅ v2.2 修复（用户反馈）：点击"查看"整页空白，刷新才正常
      之前用 <transition mode="out-in" appear> + :key="r.fullPath"：
        - mode="out-in" 让旧组件先 leave 完成（200ms）后新组件才 enter
        - appear 让首次挂载也跑 enter 动画
        - 某些情况下 enter 动画的 opacity:0 → 1 转换失败，元素卡在 opacity:0 → 整页白屏
        - 用户感受：路由切换时第一次总看不到，刷新后才行
      修法：去掉 mode="out-in" 和 appear（让组件直接替换，无动画 + 无 appear 状态）
        - 视觉上少一点动效，但稳如老狗，绝对不会卡住
        - 如果以后想加动效，每个页面用自己的 v-motion 或 GSAP，不在 AppMain 这层卡路由
    -->
    <component :is="Component" :key="r.fullPath" />
  </router-view>
</template>

<style lang="scss" scoped>
/* 保留样式占位（后续若要恢复动效可以参考） */
</style>
