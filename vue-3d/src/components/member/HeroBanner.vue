<script setup lang="ts">
/**
 * HeroBanner - 成员端页面标题区（v2.7 简化版）
 *
 * v2.7：取消插图。v2.5 的 hero-home / cta-apply / project-hero 插图全部下线，
 *       改用纯文字标题 + 副标题 + 可选 tip，去掉右侧插图区域。
 *       背景交给 AppLayout 的全屏 beijing.png，本组件只负责前景内容。
 *
 * 用法：
 *   <HeroBanner
 *     title="我的任务"
 *     subtitle="查看你的所有打印任务和进度"
 *     :is-newbie="false"
 *   />
 */

interface Props {
  /** 主标题 */
  title: string
  /** 副标题 */
  subtitle?: string
  /** 是否给新成员显示额外的引导 tip */
  isNewbie?: boolean
  /** 新成员 tip 文案 */
  newbieTip?: string
}

withDefaults(defineProps<Props>(), {
  subtitle: '',
  isNewbie: false,
  newbieTip: '作为新成员，先去逛逛打印任务和作品库吧！有任何问题找社长/技术骨干。',
})
</script>

<template>
  <div class="hero-banner">
    <div class="hero-content">
      <h1 class="hero-title">{{ title }}</h1>
      <p v-if="subtitle" class="hero-subtitle">{{ subtitle }}</p>
      <div v-if="isNewbie" class="hero-newbie-tip">
        <span class="tip-icon">💡</span>
        <span class="tip-text">{{ newbieTip }}</span>
      </div>
      <slot name="actions" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.hero-banner {
  padding: 36px $spacing-large 28px;
  color: #FFFFFF;
  text-align: center;
}
.hero-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.hero-title {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.8px;
  color: #FFFFFF;
  text-shadow: 0 4px 16px rgba(10, 37, 64, 0.35);
}
.hero-subtitle {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.92);
  text-shadow: 0 2px 6px rgba(10, 37, 64, 0.3);
  max-width: 600px;
}
.hero-newbie-tip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding: 8px 16px;
  background: rgba(255, 215, 0, 0.22);
  border: 1px solid rgba(255, 215, 0, 0.4);
  border-radius: 12px;
  font-size: 13px;
  color: #FFF9D6;
  backdrop-filter: blur(8px);
  .tip-icon { font-size: 16px; }
  .tip-text { color: #FFF9D6; }
}

@include mobile {
  .hero-banner { padding: 24px $spacing-medium 20px; }
  .hero-title { font-size: 26px; letter-spacing: -0.5px; }
  .hero-subtitle { font-size: 14px; }
}
</style>
