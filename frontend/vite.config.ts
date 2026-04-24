import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

/**
 * development：启用 `server.proxy`，将浏览器对 API 的请求转发到本地后端。
 * production：`base` 与可选 CDN 由 `VITE_APP_BASE` 控制；`sourcemap` 默认关闭以减小体积，需要排错时设 `VITE_SOURCEMAP=true`。
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const isDev = mode === 'development'
  const useSourcemap = env.VITE_SOURCEMAP === 'true'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: isDev
      ? {
          port: 5173,
          proxy: {
            [env.VITE_API_BASE_URL || '/v1']: {
              target: env.VITE_PROXY_TARGET || 'http://127.0.0.1:8080',
              changeOrigin: true,
            },
          },
        }
      : undefined,
    base: env.VITE_APP_BASE || '/',
    build: {
      sourcemap: isDev ? true : useSourcemap,
    },
  }
})
