import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => ({
  plugins: [vue()],
  publicDir: mode === 'mobile' ? false : 'public',
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          element: ['element-plus'],
          icons: ['@element-plus/icons-vue'],
          axios: ['axios']
        }
      }
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/user': { target: 'http://localhost:8080', changeOrigin: true },
      '/community': { target: 'http://localhost:8080', changeOrigin: true },
      '/parts': { target: 'http://localhost:8080', changeOrigin: true },
      '/service': { target: 'http://localhost:8080', changeOrigin: true },
      '/knowledge': { target: 'http://localhost:8080', changeOrigin: true },
      '/agent': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
}))
