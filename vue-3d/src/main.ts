import { createApp } from 'vue'
import { createPinia } from 'pinia'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

// 全局样式（注意：要在 router 之前，让首次渲染就有样式）
import '@/styles/index.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// 全局注册 Element Plus 图标（解决 el-upload 等组件内置图标的警告）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')