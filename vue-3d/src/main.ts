import { createApp } from 'vue'
import { createPinia } from 'pinia'
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