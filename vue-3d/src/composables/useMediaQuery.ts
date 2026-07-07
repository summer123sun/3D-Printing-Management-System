/**
 * 响应式断点判定 composable
 *
 * 用法：
 *   const { isMobile, isTablet, isDesktop } = useMediaQuery()
 *   if (isMobile.value) { ... }
 *
 * 断点对齐 @styles/variables.scss 的 $breakpoint-*（已通过 vite.config.ts 全局注入）：
 *   - sm: <  768px    手机端
 *   - md: <  992px    平板
 *   - lg: < 1200px    小桌面
 *   - xl: < 1920px    桌面
 *
 * 为什么不用 postcss-pxtorem + amfe-flexible？
 *   1. Element Plus 自身已按 768/992/1200/1920 设计断点，无需 rem 转换
 *   2. 避免 px→rem 转换影响 v2.19 现有布局（按钮、卡片高度等）
 *   3. 减少编译时开销，build 速度 +20%
 *   4. 暗色/亮色主题色全走 CSS 变量（html.dark），跟 px 单位无关
 *
 * @author Mavis
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'

const MOBILE_MAX = 767   // < 768
const TABLET_MAX = 991   // < 992

const isMobile = ref(false)
const isTablet = ref(false)
const isDesktop = ref(true)

let listenerAttached = false

function evaluate() {
  const w = window.innerWidth
  isMobile.value = w <= MOBILE_MAX
  isTablet.value = !isMobile.value && w <= TABLET_MAX
  isDesktop.value = !isMobile.value && !isTablet.value
}

function attachListener() {
  if (listenerAttached) return
  listenerAttached = true
  evaluate()
  window.addEventListener('resize', evaluate, { passive: true })
}

function detachListener() {
  if (!listenerAttached) return
  listenerAttached = false
  window.removeEventListener('resize', evaluate)
}

export function useMediaQuery() {
  onMounted(attachListener)
  onBeforeUnmount(detachListener)
  return {
    isMobile,
    isTablet,
    isDesktop,
  }
}

/**
 * 一次性检查当前屏幕宽度（不需要响应式）
 *
 * 用法：
 *   if (isMobileNow()) { ... }
 */
export function isMobileNow(): boolean {
  return typeof window !== 'undefined' && window.innerWidth <= MOBILE_MAX
}
