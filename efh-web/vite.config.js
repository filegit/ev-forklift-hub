import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
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
})
