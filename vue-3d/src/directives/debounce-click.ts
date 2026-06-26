/**
 * 全局指令：防重复点击
 *
 * 用法：
 *   <el-button v-debounce-click:500="handleSubmit">提交</el-button>
 *   <el-button v-debounce-click="handleSubmit">提交</el-button>  <!-- 默认 800ms -->
 *
 * 原理：在指定时间窗口内只触发一次回调，第二次点击被忽略
 * 适用场景：表单提交、删除确认、支付等任何"怕双击"的操作
 *
 * 注意：仍然建议结合按钮的 :loading 状态一起使用（双重保护）
 */
import type { App, Directive } from 'vue'

interface El extends HTMLElement {
  _debounceTimer?: number
  _lastClickTime?: number
}

const DEFAULT_DELAY = 800

const directive: Directive<El, ((e: Event) => void) | undefined> = {
  mounted(el, binding) {
    const delay = typeof binding.arg === 'string'
      ? (parseInt(binding.arg) || DEFAULT_DELAY)
      : DEFAULT_DELAY
    const handler = binding.value

    if (typeof handler !== 'function') return

    const clickHandler = (e: Event) => {
      const now = Date.now()
      const last = el._lastClickTime ?? 0

      // 时间窗口内重复点击：忽略
      if (now - last < delay) {
        e.stopPropagation()
        e.preventDefault()
        return
      }

      el._lastClickTime = now
      handler(e)
    }

    el.addEventListener('click', clickHandler, { capture: true })
    // 保存原始 handler 引用，方便 unmounted 移除
    ;(el as El & { _clickHandler?: typeof clickHandler })._clickHandler = clickHandler
  },

  unmounted(el) {
    const handler = (el as El & { _clickHandler?: (e: Event) => void })._clickHandler
    if (handler) {
      el.removeEventListener('click', handler, { capture: true } as any)
    }
    delete el._lastClickTime
    delete el._debounceTimer
  },
}

export function setupDebounceClickDirective(app: App) {
  app.directive('debounce-click', directive)
}
