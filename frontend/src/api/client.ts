import axios, { type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types/api'

const timeoutMs = 10_000

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: timeoutMs,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.request.use((config) => {
  config.headers.set('X-Request-ID', crypto.randomUUID())
  return config
})

request.interceptors.response.use(
  <T>(response: AxiosResponse<ApiResponse<T>>) => response,
  async (error) => {
    const status = error?.response?.status as number | undefined
    if (status === 401) {
      const currentPath = `${window.location.pathname}${window.location.search}`
      const encoded = encodeURIComponent(currentPath)
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign(`/login?returnUrl=${encoded}`)
      }
    }
    return Promise.reject(error)
  },
)

export async function unwrapApiResponse<T>(response: AxiosResponse<ApiResponse<T>>): Promise<T> {
  if (!response.data.success) {
    throw new Error(response.data.message || response.data.code)
  }
  return response.data.data
}

export { request, timeoutMs }
