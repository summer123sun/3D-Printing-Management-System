<script setup lang="ts">
/**
 * CursorEffect - 成员端专属鼠标动效
 *
 * 组成：
 * 1. 自定义光标：小圆点（精准跟随鼠标）
 * 2. 跟随光环：大圆（lerp 缓动追随）
 * 3. 点击涟漪：点击位置扩散圆环（多个可同时存在）
 * 4. Hover 状态：在可点击元素上放大光标 + 改变颜色
 *
 * 性能：使用 transform + requestAnimationFrame + will-change
 * 兼容：移动端 / 触摸设备不显示（用 @media (hover: hover) 判断）
 */
import { onMounted, onBeforeUnmount, ref } from 'vue'

const dotX = ref(0)
const dotY = ref(0)
const ringX = ref(0)
const ringY = ref(0)
const isHovering = ref(false)
const isVisible = ref(false)
interface Ripple {
  id: number
  x: number
  y: number
  color: string
  startTime: number
}
const ripples = ref<Ripple[]>([])
let rippleId = 0

// 用于 lerp 平滑跟随
let targetX = 0
let targetY = 0
let rafId: number | null = null

// 检测是否支持 hover（移动端没有 hover，禁用自定义光标）
const isHoverCapable = typeof window !== 'undefined' && window.matchMedia('(hover: hover) and (pointer: fine)').matches

// 点击涟漪颜色（暖金 + 薄荷青，匹配品牌色）
const RIPPLE_COLORS = ['#F2A93B', '#00D4AA', '#00A88A', '#FFD700']

const onMouseMove = (e: MouseEvent) => {
  if (!isHoverCapable) return
  targetX = e.clientX
  targetY = e.clientY
  dotX.value = e.clientX
  dotY.value = e.clientY
  if (!isVisible.value) isVisible.value = true
}

const onMouseDown = (e: MouseEvent) => {
  if (!isHoverCapable) return
  // 生成 1-2 个涟漪（让点击有"水波"感）
  const count = Math.random() > 0.6 ? 2 : 1
  for (let i = 0; i < count; i++) {
    ripples.value.push({
      id: ++rippleId,
      x: e.clientX,
      y: e.clientY,
      color: RIPPLE_COLORS[Math.floor(Math.random() * RIPPLE_COLORS.length)] ?? '#F2A93B',
      startTime: Date.now() + i * 80,  // 多重涟漪错开时间
    })
  }
  // 限制同时存在的涟漪数量
  if (ripples.value.length > 12) ripples.value = ripples.value.slice(-8)
}

const onMouseOver = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target) return
  // 在可点击元素上 hover 时放大光标
  const clickable = target.closest('button, a, [role="button"], .clickable, .entry-card, .stat-card, .member-card, input, textarea, select, .el-button')
  isHovering.value = !!clickable
}

const animate = () => {
  // ring 缓动追随（lerp 系数 0.18，跟手但不抖动）
  ringX.value += (targetX - ringX.value) * 0.18
  ringY.value += (targetY - ringY.value) * 0.18
  // 清理超过 1.2s 的涟漪
  const now = Date.now()
  ripples.value = ripples.value.filter((r) => now - r.startTime < 1200)
  rafId = requestAnimationFrame(animate)
}

onMounted(() => {
  if (!isHoverCapable) return
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mousedown', onMouseDown)
  document.addEventListener('mouseover', onMouseOver)
  // 隐藏原生光标（仅在支持 hover 的设备上）
  document.documentElement.classList.add('custom-cursor-active')
  rafId = requestAnimationFrame(animate)
})

onBeforeUnmount(() => {
  if (!isHoverCapable) return
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mousedown', onMouseDown)
  document.removeEventListener('mouseover', onMouseOver)
  document.documentElement.classList.remove('custom-cursor-active')
  if (rafId !== null) cancelAnimationFrame(rafId)
})
</script>

<template>
  <Teleport to="body">
    <!-- 点击涟漪层 -->
    <div class="cursor-ripple-layer" aria-hidden="true">
      <span
        v-for="r in ripples"
        :key="r.id"
        class="cursor-ripple"
        :style="{
          left: r.x + 'px',
          top: r.y + 'px',
          borderColor: r.color,
          animationDelay: (r.startTime - Date.now()) + 'ms',
        }"
      />
    </div>
    <!-- 自定义光标 -->
    <div
      v-if="isHoverCapable && isVisible"
      class="cursor-dot"
      :class="{ 'is-hovering': isHovering }"
      :style="{ left: dotX + 'px', top: dotY + 'px' }"
      aria-hidden="true"
    />
    <div
      v-if="isHoverCapable && isVisible"
      class="cursor-ring"
      :class="{ 'is-hovering': isHovering }"
      :style="{ left: ringX + 'px', top: ringY + 'px' }"
      aria-hidden="true"
    />
  </Teleport>
</template>

<style lang="scss" scoped>
.cursor-ripple-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 99998;
}
.cursor-ripple {
  position: absolute;
  width: 16px;
  height: 16px;
  margin-left: -8px;
  margin-top: -8px;
  border: 2px solid #F2A93B;
  border-radius: 50%;
  opacity: 0.9;
  animation: cursor-ripple-out 1.0s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  will-change: transform, opacity;
}
@keyframes cursor-ripple-out {
  0%   { transform: scale(0.4); opacity: 0.95; }
  100% { transform: scale(8);   opacity: 0;    }
}

.cursor-dot {
  position: fixed;
  width: 6px;
  height: 6px;
  margin-left: -3px;
  margin-top: -3px;
  background: #F2A93B;
  border-radius: 50%;
  pointer-events: none;
  z-index: 99999;
  transform: translate3d(0, 0, 0);
  transition: width 0.18s ease, height 0.18s ease, margin 0.18s ease, background 0.18s ease;
  will-change: transform, width, height;
  mix-blend-mode: difference;  // 在浅色背景上变深，深色背景上变浅
  &.is-hovering {
    width: 4px;
    height: 4px;
    margin-left: -2px;
    margin-top: -2px;
    background: #00D4AA;
  }
}

.cursor-ring {
  position: fixed;
  width: 36px;
  height: 36px;
  margin-left: -18px;
  margin-top: -18px;
  border: 1.5px solid rgba(242, 169, 59, 0.55);
  background: transparent;
  border-radius: 50%;
  pointer-events: none;
  z-index: 99998;
  transform: translate3d(0, 0, 0);
  transition: width 0.22s ease, height 0.22s ease, margin 0.22s ease, border-color 0.22s ease, background 0.22s ease;
  will-change: transform, width, height;
  &.is-hovering {
    width: 60px;
    height: 60px;
    margin-left: -30px;
    margin-top: -30px;
    border-color: rgba(0, 212, 170, 0.65);
    background: rgba(0, 212, 170, 0.08);
  }
}
</style>

<style lang="scss">
/* 全局样式（非 scoped，因为要覆盖整个页面的 cursor 样式） */
html.custom-cursor-active,
html.custom-cursor-active * {
  cursor: none !important;
}
html.custom-cursor-active input,
html.custom-cursor-active textarea,
html.custom-cursor-active select {
  cursor: text !important;
}
</style>
