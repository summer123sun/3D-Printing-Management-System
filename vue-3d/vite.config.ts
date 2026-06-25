import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载 .env[.mode] 中的环境变量
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [
      vue(),
      vueDevTools(),
      // 自动导入 Vue/Vue Router/Pinia/Element Plus 的 API
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia'],
        resolvers: [ElementPlusResolver()],
        dts: 'src/auto-imports.d.ts',
        eslintrc: { enabled: false },
      }),
      // 自动注册 Element Plus 组件
      Components({
        resolvers: [ElementPlusResolver()],
        dts: 'src/components.d.ts',
      }),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          // 全局注入 SCSS 变量（注意：仅作用于 SCSS 预处理器，组件内 <style lang="scss"> 可直接用变量）
          additionalData: `@import "@/styles/variables.scss";`,
        },
      },
    },
    server: {
      host: '0.0.0.0',
      port: 5173,
      open: false,
      proxy: {
        // 所有 /api/* 请求代理到后端 Spring Boot
        '/api': {
          target: env.VITE_API_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          // 后端不需要 /api 前缀时去掉
          // rewrite: (path) => path.replace(/^\/api/, ''),
        },
        // 静态资源（STL/图片等）
        '/uploads': {
          target: env.VITE_API_TARGET || 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
    build: {
      target: 'es2020',
      outDir: 'dist',
      // 把 echarts/wangeditor 这种大依赖拆出来，减小首屏
      rollupOptions: {
        output: {
          manualChunks(id: string) {
            if (id.includes('node_modules/element-plus') || id.includes('node_modules/@element-plus')) {
              return 'element-plus'
            }
            if (id.includes('node_modules/echarts')) {
              return 'echarts'
            }
            if (id.includes('node_modules/@wangeditor')) {
              return 'wangeditor'
            }
            return undefined
          },
        },
      },
    },
  }
})