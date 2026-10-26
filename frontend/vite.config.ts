import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_PROXY_TARGET || 'http://localhost:8080'
  const proxy = {
    '/api': { target, changeOrigin: true },
    '/doc.html': { target, changeOrigin: true }
  }
  return {
    plugins: [vue()],
    resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
    server: { port: 5173, host: true, proxy },
    preview: { port: 4173, host: true, proxy },
    optimizeDeps: { exclude: ['@mkkellogg/gaussian-splats-3d'] },
    build: {
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          manualChunks(id: string) {
            if (id.includes('node_modules')) {
              if (id.includes('echarts') || id.includes('zrender')) return 'echarts'
              if (id.includes('three') || id.includes('gaussian-splats')) return 'three'
              if (id.includes('element-plus')) return 'element-plus'
            }
          }
        }
      }
    }
  }
})
