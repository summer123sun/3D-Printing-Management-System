<script setup lang="ts">
/**
 * CursorEffect - 成员端专属"点击涟漪"动效（v2.8）
 *
 * v2.8 修正：用户反馈"鼠标正常样式就行了，选择的时候有动画效果"
 *   - 去掉自定义光标（恢复系统默认箭头）
 *   - 只保留点击涟漪：在点击位置扩散随机色圆环，"湖面涟漪"感
 *
 * 性能：transform + requestAnimationFrame + 限制同时存在数量
 * 兼容：移动端 / 触摸设备不显示（用 @media (hover: hover) 判断）
 */
import { onMounted, onBeforeUnmount, ref } from 'vue'

interface Ripple {
  id: number
  x: number
  y: number
  color: string
  startTime: number
}
const ripples = ref<Ripple[]>([])
let rippleId = 0

// 检测是否支持精细指针（移动端触摸设备不显示涟漪）
const isFinePointer = typeof window !== 'undefined' && window.matchMedia('(pointer: fine)').matches

// 点击涟漪颜色（品牌色系：暖金 + 薄荷青 + 深海蓝）
const RIPPLE_COLORS = ['#F2A93B', '#00D4AA', '#00A88A', '#FFD700', '#7BA7D9']

const onMouseDown = (e: MouseEvent) => {
  if (!isFinePointer) return
  // 排除右键和中键
  if (e.button !== 0) return
  // 主点击 1 个涟漪，~35% 概率再追加 1 个形成"双波"
  const count = Math.random() > 0.65 ? 2 : 1
  for (let i = 0; i < count; i++) {
    ripples.value.push({
      id: ++rippleId,
      x: e.clientX,
      y: e.clientY,
      color: RIPPLE_COLORS[Math.floor(Math.random() * RIPPLE_COLORS.length)] ?? '#F2A93B',
      startTime: Date.now() + i * 90,
    })
  }
  // 限制同时存在的涟漪数量（防止长会话内存累积）
  if (ripples.value.length > 12) ripples.value = ripples.value.slice(-8)
}

// 清理超过 1.1s 的涟漪
let cleanupTimer: number | null = null
const startCleanupLoop = () => {
  cleanupTimer = window.setInterval(() => {
    const now = Date.now()
    ripples.value = ripples.value.filter((r) => now - r.startTime < 1200)
  }, 300)
}

onMounted(() => {
  if (!isFinePointer) return
  document.addEventListener('mousedown', onMouseDown)
  startCleanupLoop()
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onMouseDown)
  if (cleanupTimer !== null) clearInterval(cleanupTimer)
})
</script>

<template>
  <Teleport to="body">
    <div class="cursor-ripple-layer" aria-hidden="true">
      <span
        v-for="r in ripples"
        :key="r.id"
        class="cursor-ripple"
        :style="{
          left: r.x + 'px',
          top: r.y + 'px',
          borderColor: r.color,
          animationDelay: Math.max(0, r.startTime - Date.now()) + 'ms',
        }"
      />
    </div>
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
  width: 20px;
  height: 20px;
  margin-left: -10px;
  margin-top: -10px;
  border: 2.5px solid #F2A93B;
  border-radius: 50%;
  opacity: 0.85;
  animation: cursor-ripple-out 1.05s cubic-bezier(0.22, 1, 0.36, 1) forwards;
  will-change: transform, opacity;
  box-sizing: border-box;
}
@keyframes cursor-ripple-out {
  0%   { transform: scale(0.5); opacity: 0.95; border-width: 3px; }
  60%  { opacity: 0.5; }
  100% { transform: scale(9);   opacity: 0;    border-width: 1px; }
}
</style>
