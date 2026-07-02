import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElNotification } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

// 全局样式（注意：要在 router 之前，让首次渲染就有样式）
import '@/styles/index.scss'

// Element Plus 主题色覆盖（v2 紫系品牌色）
import { setElementPlusTheme } from '@/styles/element-theme'
setElementPlusTheme()

// 全局指令
import { setupDebounceClickDirective } from '@/directives/debounce-click'

// 认证 store（用于全局一次性初始化 storage 同步）
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 全局注册 Element Plus 图标（解决 el-upload 等组件内置图标的警告）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册全局自定义指令
setupDebounceClickDirective(app)

// ✅ v2.2 修复（用户反馈）：项目详情 / 其他页面偶发整页空白
//    根因：Vue 3 组件渲染时若 computed / setup 抛 NPE，会被 Vue 静默吞掉，
//         整个组件 v-if 全部不渲染 → 整页空白，console 不报错（dev 模式才会 warn）
//    修复：注册全局 errorHandler，捕获后打 console.error（带 stack + 组件名），
//         并在 prod 弹一条 ElNotification 让用户能看到提示（避免"什么都没发生"的体验）
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue 全局错误]', info, err)
  if (import.meta.env.PROD) {
    ElNotification({
      title: '页面渲染出错',
      message: (err as Error)?.message || '未知错误，请刷新页面重试',
      type: 'error',
      duration: 0,           // 不自动关，让用户看清楚
      showClose: true,
    })
  }
}

// ✅ v2.2 修复（用户反馈）：SPA 懒加载 chunk 失败导致整页空白
//    触发场景：Cloudflare Pages 部署瞬间、CDN 边缘节点缓存未更新、用户从旧版 SPA 内
//             router.push 跳到一个新部署才出现的 chunk 时偶发 Failed to fetch dynamically imported module
//    修复：router.onError 捕获后自动 window.location.reload()（强制刷新即可正常加载新 chunk）
router.onError((err, to) => {
  const msg = (err as Error)?.message || ''
  if (msg.includes('Failed to fetch dynamically imported module') ||
      msg.includes('Loading chunk') ||
      msg.includes('Loading CSS chunk')) {
    console.warn('[路由] chunk 加载失败，自动刷新:', to?.fullPath, err)
    window.location.reload()
  } else {
    console.error('[路由] 其他错误:', err)
  }
})

app.mount('#app')

// ✅ v2.2 修复（用户反馈）：同浏览器多 Tab 不同账号登录导致权限混乱
//    启动后注册 storage 事件监听，token 变化时自动同步 store.user
useAuthStore().setupStorageSync()

// ============================================
// 全局 ElNotification 交互增强
// 点击通知空白处（非关闭键 X）→ 立即关闭，符合用户习惯
// ============================================
document.addEventListener('click', (e: MouseEvent) => {
  const target = e.target as HTMLElement | null
  if (!target) return
  const notif = target.closest('.el-notification') as HTMLElement | null
  if (!notif) return
  // 点关闭键 X 时让 EP 自己处理
  if (target.closest('.el-notification__closeBtn')) return
  notif.remove()
})