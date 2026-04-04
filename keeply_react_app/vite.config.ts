import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import { attachNotificationProxyFallback } from './vite-notification-proxy-fallback.ts'

// Dev proxy routes browser calls on :5173 to your local microservices (avoids CORS during `npm run dev`).
// https://vite.dev/config/server-options.html#server-proxy
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  /** Same names as Spring `application.yml`; optional lines in `.env` / shell. */
  const microservicePorts = {
    auth: env.AUTH_SERVICE_PORT || process.env.AUTH_SERVICE_PORT || '8081',
    notification: env.NOTIFICATION_SERVICE_PORT || process.env.NOTIFICATION_SERVICE_PORT || '8082',
    asset: env.ASSET_SERVICE_PORT || process.env.ASSET_SERVICE_PORT || '8085',
    helpdesk: env.HELPDESK_SERVICE_PORT || process.env.HELPDESK_SERVICE_PORT || '8084',
  }

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api/auth': { target: `http://localhost:${microservicePorts.auth}`, changeOrigin: true },
        '/api/asset': { target: `http://localhost:${microservicePorts.asset}`, changeOrigin: true },
        '/api/notifications': {
          target: `http://localhost:${microservicePorts.notification}`,
          changeOrigin: true,
          configure(proxy) {
            attachNotificationProxyFallback(proxy)
          },
        },
        '/api/helpdesk': { target: `http://localhost:${microservicePorts.helpdesk}`, changeOrigin: true },
        '/uploads': { target: `http://localhost:${microservicePorts.auth}`, changeOrigin: true },
      },
    },
  }
})
