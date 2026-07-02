<script setup lang="ts">
/**
 * HeroBanner - 成员端欢迎/标题区
 *
 * 用法：
 *   <HeroBanner
 *     title="欢迎回来，张明"
 *     subtitle="今天有 2 个任务等待你的关注"
 *     illustration="hero-home"
 *     :is-newbie="true"
 *   />
 */
import { computed } from 'vue'
import heroHome from '@/assets/member/hero-home.png'
import ctaApply from '@/assets/member/cta-apply.png'
import projectHeroDefault from '@/assets/member/project-hero-default.png'

interface Props {
  /** 主标题 */
  title: string
  /** 副标题 */
  subtitle?: string
  /** 插图资源 key，可选 'hero-home' / 'cta-apply' / 'project-hero' */
  illustration?: 'hero-home' | 'cta-apply' | 'project-hero'
  /** 是否给新成员显示额外的引导 tip */
  isNewbie?: boolean
  /** 新成员 tip 文案 */
  newbieTip?: string
  /** 渐变主题色，默认深海蓝 */
  theme?: 'primary' | 'accent' | 'gold'
}

const props = withDefaults(defineProps<Props>(), {
  subtitle: '',
  illustration: 'hero-home',
  isNewbie: false,
  newbieTip: '作为新成员，先去逛逛打印任务和作品库吧！有任何问题找社长/技术骨干。',
  theme: 'primary',
})

const illustrationMap: Record<string, string> = {
  'hero-home': heroHome,
  'cta-apply': ctaApply,
  'project-hero': projectHeroDefault,
}

const currentIllustration = computed(() => illustrationMap[props.illustration] || heroHome)

const gradientStyle = computed(() => {
  switch (props.theme) {
    case 'accent':
      return 'linear-gradient(135deg, #0A2540 0%, #00A88A 100%)'
    case 'gold':
      return 'linear-gradient(135deg, #0A2540 0%, #CCB000 100%)'
    default:
      return 'linear-gradient(135deg, #0A2540 0%, #1E3A5F 50%, #00A88A 100%)'
  }
})
</script>

<template>
  <div class="hero-banner" :style="{ background: gradientStyle }">
    <div class="hero-text">
      <h1 class="hero-title">{{ title }}</h1>
      <p v-if="subtitle" class="hero-subtitle">{{ subtitle }}</p>
      <div v-if="isNewbie" class="hero-newbie-tip">
        <span class="tip-icon">💡</span>
        <span class="tip-text">{{ newbieTip }}</span>
      </div>
      <slot name="actions" />
    </div>
    <div class="hero-illustration">
      <img :src="currentIllustration" :alt="title" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.hero-banner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-large;
  border-radius: 20px;
  padding: 40px 48px;
  color: #FFFFFF;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(10, 37, 64, 0.18);
  min-height: 240px;

  // 装饰光晕
  &::before {
    content: '';
    position: absolute;
    top: -40px;
    right: -40px;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(255, 215, 0, 0.18) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
  &::after {
    content: '';
    position: absolute;
    bottom: -30px;
    left: -30px;
    width: 160px;
    height: 160px;
    background: radial-gradient(circle, rgba(0, 212, 170, 0.15) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

.hero-text {
  flex: 1;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-base;
}

.hero-title {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.5px;
  color: #FFFFFF;
}

.hero-subtitle {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.92;
  color: #FFFFFF;
  max-width: 520px;
}

.hero-newbie-tip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: $spacing-small;
  padding: 8px 14px;
  background: rgba(255, 215, 0, 0.18);
  border: 1px solid rgba(255, 215, 0, 0.35);
  border-radius: 12px;
  font-size: 13px;
  color: #FFF9D6;
  max-width: max-content;
  backdrop-filter: blur(8px);
  .tip-icon { font-size: 16px; }
  .tip-text { color: #FFF9D6; }
}

.hero-illustration {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  width: 280px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
    // 轻微漂浮动画
    animation: hero-float 4s ease-in-out infinite;
  }
}

@keyframes hero-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@include mobile {
  .hero-banner {
    flex-direction: column-reverse;
    padding: $spacing-large;
    text-align: center;
  }
  .hero-title { font-size: 24px; }
  .hero-illustration { width: 200px; height: 140px; }
  .hero-newbie-tip { max-width: 100%; }
}
</style>
