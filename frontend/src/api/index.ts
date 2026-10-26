import { http, type PageResult } from './http'
import type * as T from './types'

export const authApi = {
  login: (username: string, password: string) => http.post<T.LoginVO>('/auth/login', { username, password }),
  register: (data: { username: string; password: string; nickname?: string; phone?: string; email?: string }) =>
    http.post<T.UserInfo>('/auth/register', data),
  me: () => http.get<T.UserInfo>('/auth/me', undefined, { quiet: true }),
  logout: () => http.post<void>('/auth/logout', undefined, { quiet: true }),
  changePassword: (oldPassword: string, newPassword: string) => http.put<void>('/auth/password', { oldPassword, newPassword })
}

export interface ArtifactQuery {
  current?: number
  size?: number
  keyword?: string
  categoryId?: number
  pitId?: number
  materialId?: number
  periodId?: number
  hallId?: number
  isTreasure?: number
  status?: number
  orderBy?: string
}

export const artifactApi = {
  search: (q: ArtifactQuery) => http.get<PageResult<T.Artifact>>('/artifacts/search', q),
  hot: (limit = 8) => http.get<T.Artifact[]>('/artifacts/hot', { limit }),
  detail: (id: number | string) => http.get<T.ArtifactDetail>(`/artifacts/${id}/detail`)
}

export const dictApi = {
  categories: () => http.get<any[]>('/categories/list'),
  pits: () => http.get<any[]>('/pits/list'),
  materials: () => http.get<any[]>('/materials/list'),
  periods: () => http.get<any[]>('/periods/list'),
  halls: () => http.get<any[]>('/halls/list'),
  models: (params?: object) => http.get<T.Model3d[]>('/models/list', params),
  announcements: () => http.get<T.Announcement[]>('/announcements/list', { status: 1 }),
  events: () => http.get<T.ExhibitionEvent[]>('/events/list')
}

export const kgApi = {
  graph: (types?: string, limit = 300) => http.get<T.GraphVO>('/kg/graph', { types, limit }),
  neighbors: (id: number | string, hops = 1, maxNodes = 60) =>
    http.get<T.GraphVO>(`/kg/neighbors/${id}`, { hops, maxNodes }),
  path: (fromId: number | string, toId: number | string, maxDepth = 6) =>
    http.get<T.GraphVO>('/kg/path', { fromId, toId, maxDepth }),
  search: (keyword: string, limit = 20) => http.get<T.GraphNode[]>('/kg/search', { keyword, limit }),
  statistics: () => http.get<{ entityTypes: T.NameValue[]; relationTypes: T.NameValue[] }>('/kg/statistics')
}

export const qaApi = {
  sessions: () => http.get<T.QaSession[]>('/qa/sessions', undefined, { quiet: true }),
  messages: (sid: number) => http.get<T.QaMessage[]>(`/qa/sessions/${sid}/messages`),
  deleteSession: (sid: number) => http.del<void>(`/qa/sessions/${sid}`),
  feedback: (mid: number, feedback: 1 | -1) => http.post<void>(`/qa/messages/${mid}/feedback`, { feedback }),
  llmStatus: () => http.get<{ configured: boolean; model: string; mode: string; baseUrl?: string }>('/qa/llm-status', undefined, { quiet: true }),
  retrieve: (question: string) => http.get<any>('/qa/retrieve', { question })
}

export const reviewApi = {
  target: (targetType: string, targetId: number, current = 1, size = 10) =>
    http.get<PageResult<T.Review>>('/reviews/target', { targetType, targetId, current, size }),
  summary: (targetType: string, targetId: number) =>
    http.get<{ count: number; avgRating: number; stars: Record<string, number> }>('/reviews/summary', { targetType, targetId }),
  publish: (data: { targetType: string; targetId: number; rating: number; content: string }) => http.post<T.Review>('/reviews', data),
  my: (current = 1, size = 20) => http.get<PageResult<T.Review>>('/reviews/my', { current, size }),
  like: (id: number) => http.post<void>(`/reviews/${id}/like`),
  remove: (id: number) => http.del<void>(`/reviews/${id}`)
}

export const favoriteApi = {
  toggle: (targetType: string, targetId: number) => http.post<{ favorited: boolean }>('/favorites/toggle', { targetType, targetId }),
  artifacts: (current = 1, size = 50) => http.get<PageResult<T.Artifact>>('/favorites/artifacts', { current, size })
}

export const routeApi = {
  list: () => http.get<T.Route[]>('/routes/list', { status: 1 }),
  detail: (id: number | string) => http.get<T.RouteDetail>(`/routes/${id}/detail`),
  recommend: (interests?: string, minutes?: number) => http.get<T.Route[]>('/routes/recommend', { interests, minutes }),
  start: (id: number) => http.post<void>(`/routes/${id}/start`, undefined, { quiet: true })
}

export const reservationApi = {
  slots: (date: string, museumId = 1) => http.get<T.Slot[]>('/reservations/slots', { date, museumId }),
  create: (data: object) => http.post<T.Reservation>('/reservations', data),
  my: (current = 1, size = 50) => http.get<PageResult<T.Reservation>>('/reservations/my', { current, size }),
  cancel: (id: number) => http.put<void>(`/reservations/${id}/cancel`),
  setStatus: (id: number, status: string) => http.put<void>(`/reservations/${id}/status`, undefined, { params: { status } }),
  checkIn: (reservationNo: string) => http.post<T.Reservation>('/reservations/check-in', undefined, { params: { reservationNo } })
}

export const dashboardApi = {
  overview: () => http.get<T.Overview>('/dashboard/overview'),
  distribution: (dimension: string) => http.get<T.NameValue[]>('/dashboard/artifact-distribution', { dimension }),
  distributionAll: () => http.get<Record<string, T.NameValue[]>>('/dashboard/artifact-distribution/all'),
  visitTrend: (days = 30) => http.get<{ dates: string[]; series: Record<string, number[]> }>('/dashboard/visit-trend', { days }),
  qa: (days = 14) => http.get<any>('/dashboard/qa', { days }),
  models: () => http.get<any>('/dashboard/models'),
  services: () => http.get<any>('/dashboard/services')
}

export const adminApi = {
  setUserStatus: (id: number, status: number) => http.put<void>(`/users/${id}/status`, undefined, { params: { status } }),
  assignRoles: (id: number, roleIds: number[]) => http.put<void>(`/users/${id}/roles`, { roleIds }),
  resetPassword: (id: number) => http.put<void>(`/users/${id}/reset-password`),
  auditReview: (id: number, status: number, reply?: string) =>
    http.put<void>(`/reviews/${id}/audit`, undefined, { params: { status, reply } }),
  configs: (group?: string) => http.get<any[]>('/configs', { group }),
  saveConfigs: (values: Record<string, string>) => http.put<void>('/configs', values),
  testLlm: () => http.post<any>('/configs/llm/test', undefined, { timeout: 60000 }),
  refreshKg: () => http.post<void>('/kg/refresh')
}
