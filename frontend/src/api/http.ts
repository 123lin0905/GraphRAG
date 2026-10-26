import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { APP_CONFIG } from '@/config'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  timestamp?: number
}
export interface PageResult<T> {
  total: number
  current: number
  size: number
  records: T[]
}

const instance = axios.create({ baseURL: APP_CONFIG.apiBase, timeout: 30000 })

export function getToken(): string | null {
  return localStorage.getItem(APP_CONFIG.tokenKey)
}

instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

let redirecting = false
function handleUnauthorized() {
  localStorage.removeItem(APP_CONFIG.tokenKey)
  localStorage.removeItem(APP_CONFIG.userKey)
  if (redirecting) return
  redirecting = true
  const here = location.pathname + location.search
  if (!location.pathname.startsWith('/login')) {
    location.href = `/login?redirect=${encodeURIComponent(here)}`
  }
  setTimeout(() => (redirecting = false), 1500)
}

instance.interceptors.response.use(
  (resp) => {
    const body = resp.data as ApiResult<unknown>
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return resp
      if (body.code === 401) handleUnauthorized()
      const quiet = (resp.config as AxiosRequestConfig & { quiet?: boolean }).quiet
      if (!quiet) ElMessage.error(body.message || '请求失败')
      const err = new Error(body.message) as Error & { handled?: boolean; code?: number }
      err.handled = true
      err.code = body.code
      return Promise.reject(err)
    }
    return resp
  },
  (error) => {
    const status = error?.response?.status
    const msg = error?.response?.data?.message
    const quiet = error?.config?.quiet
    if (status === 401) {
      handleUnauthorized()
      if (!quiet) ElMessage.warning('请先登录')
    } else if (!quiet) {
      ElMessage.error(msg || (status === 403 ? '没有访问权限' : '网络异常，请稍后重试'))
    }
    if (error && typeof error === 'object') error.handled = true
    return Promise.reject(error)
  }
)

export type ReqConfig = AxiosRequestConfig & { quiet?: boolean }

export const http = {
  async get<T>(url: string, params?: object, config?: ReqConfig): Promise<T> {
    const r = await instance.get<ApiResult<T>>(url, { params, ...config })
    return r.data.data
  },
  async post<T>(url: string, data?: unknown, config?: ReqConfig): Promise<T> {
    const r = await instance.post<ApiResult<T>>(url, data, config)
    return r.data.data
  },
  async put<T>(url: string, data?: unknown, config?: ReqConfig): Promise<T> {
    const r = await instance.put<ApiResult<T>>(url, data, config)
    return r.data.data
  },
  async del<T>(url: string, config?: ReqConfig): Promise<T> {
    const r = await instance.delete<ApiResult<T>>(url, config)
    return r.data.data
  }
}

/** 解析 Spring SseEmitter 的流（POST + fetch），逐事件回调 */
export async function streamSse(
  url: string,
  body: unknown,
  onEvent: (event: string, data: any) => void,
  signal?: AbortSignal
): Promise<void> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json', Accept: 'text/event-stream' }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`
  const resp = await fetch(APP_CONFIG.apiBase + url, { method: 'POST', headers, body: JSON.stringify(body), signal })
  if (!resp.ok || !resp.body) throw new Error(`HTTP ${resp.status}`)
  const reader = resp.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  for (;;) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    buffer = buffer.replace(/\r\n/g, '\n')
    let idx: number
    while ((idx = buffer.indexOf('\n\n')) >= 0) {
      const chunk = buffer.slice(0, idx)
      buffer = buffer.slice(idx + 2)
      let event = 'message'
      const dataLines: string[] = []
      for (const line of chunk.split('\n')) {
        if (line.startsWith('event:')) event = line.slice(6).trim()
        else if (line.startsWith('data:')) dataLines.push(line.slice(5))
      }
      if (!dataLines.length) continue
      const raw = dataLines.join('\n')
      let data: any = raw
      try {
        data = JSON.parse(raw)
      } catch {
        /* 非 JSON 数据原样返回 */
      }
      onEvent(event, data)
    }
  }
}
