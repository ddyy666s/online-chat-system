import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5173,                 
    proxy: {
      // 配置代理：将以 /api 开头的请求转发到后端
      '/api': {
        target: 'http://localhost:8080', 
        changeOrigin: true,              // 支持跨域
        rewrite: (path) => path.replace(/^\/api/, '') 
      }
    }
  }
})