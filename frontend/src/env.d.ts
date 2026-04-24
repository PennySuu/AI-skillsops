/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 前端请求使用的 API 前缀或完整根 URL */
  readonly VITE_API_BASE_URL: string
  /** development 下 Vite dev server 代理目标（后端 origin） */
  readonly VITE_PROXY_TARGET?: string
  /** 静态资源 base，用于 CDN 部署 */
  readonly VITE_APP_BASE: string
  /** 是否生成构建 sourcemap（production 默认 false） */
  readonly VITE_SOURCEMAP?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
