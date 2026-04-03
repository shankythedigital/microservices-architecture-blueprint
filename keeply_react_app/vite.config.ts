import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { attachNotificationProxyFallback } from './vite-notification-proxy-fallback.ts'

// Dev proxy routes browser calls on :5173 to your local microservices (avoids CORS during `npm run dev`).
// https://vite.dev/config/server-options.html#server-proxy
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api/auth': { target: 'http://localhost:8081', changeOrigin: true },
      '/api/asset': { target: 'http://localhost:8085', changeOrigin: true },
      '/api/notifications': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        configure(proxy) {
          attachNotificationProxyFallback(proxy)
        },
      },
      '/api/helpdesk': { target: 'http://localhost:8084', changeOrigin: true },
    },
  },
})
