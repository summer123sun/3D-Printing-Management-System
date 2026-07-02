<script setup lang="ts">
/**
 * 内容区：路由切换
 * - 后台端：直接渲染（原有行为）
 * - 成员端：在 /home 外所有页面上方加"返回首页"按钮（替代原 sidebar 导航）
 *
 * v2.2 修复（用户反馈）：点击"查看"整页空白，刷新才正常
 * 之前用 <transition mode="out-in" appear> + :key="r.fullPath"：
 *   - mode="out-in" 让旧组件先 leave 完成（200ms）后新组件才 enter
 *   - appear 让首次挂载也跑 enter 动画
 *   - 某些情况下 enter 动画的 opacity:0 → 1 转换失败，元素卡在 opacity:0 → 整页白屏
 * 修法：去掉 transition（让组件直接替换，无动画）
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { Role } from '@/types/member'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 成员端：role >= 3
const isMemberLayout = computed(() => {
  const role = authStore.user?.role ?? Role.NEWBIE
  return role >= Role.MEMBER
})

// 是否需要显示"返回首页"按钮（成员端 + 非首页）
const showBackToHome = computed(() => {
  return isMemberLayout.value && route.path !== '/home'
})

const goHome = () => {
  router.push('/home')
}
</script>

<template>
  <div class="app-main-content">
    <!-- 成员端：所有非首页显示"返回首页" -->
    <div v-if="showBackToHome" class="back-to-home-row">
      <button class="back-to-home" @click="goHome">
        <el-icon :size="16"><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </button>
    </div>

    <router-view v-slot="{ Component, route: r }">
      <component :is="Component" :key="r.fullPath" />
    </router-view>
  </div>
</template>

<style lang="scss" scoped>
.app-main-content {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.back-to-home-row {
  // 成员端布局（无 sidebar）下：顶部居中容器，左右贴边
  display: flex;
  align-items: center;
  padding: $spacing-medium $spacing-large 0;

  @include mobile {
    padding: $spacing-small $spacing-small 0;
  }
}

.back-to-home {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-extra-light);
  border-radius: 999px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(10, 37, 64, 0.06);
  transition: all 0.2s ease;

  &:hover {
    background: var(--brand-color);
    color: #fff;
    border-color: var(--brand-color);
    transform: translateX(-2px);
    box-shadow: 0 4px 12px rgba(10, 37, 64, 0.15);
  }
  &:active {
    transform: translateX(-2px) scale(0.97);
  }
}
</style>
