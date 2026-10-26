export function fmtNum(n?: number | null): string {
  if (n === undefined || n === null) return '-'
  if (n >= 1e8) return (n / 1e8).toFixed(2) + '亿'
  if (n >= 1e4) return (n / 1e4).toFixed(1) + '万'
  return n.toLocaleString('zh-CN')
}
export function fmtBytes(b?: number | null): string {
  if (!b) return '-'
  const u = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let v = b
  while (v >= 1024 && i < u.length - 1) { v /= 1024; i++ }
  return v.toFixed(i ? 1 : 0) + ' ' + u[i]
}
export function parseVec(s?: string | null): number[] | undefined {
  if (!s) return undefined
  try {
    const v = JSON.parse(s)
    return Array.isArray(v) && v.length === 3 ? v.map(Number) : undefined
  } catch {
    return undefined
  }
}
export function parseJsonArray(s?: string | null): any[] {
  if (!s) return []
  try {
    const v = JSON.parse(s)
    return Array.isArray(v) ? v : []
  } catch {
    return []
  }
}
export function today(offset = 0): string {
  const d = new Date(Date.now() + offset * 86400000)
  const p = (x: number) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

export const RESERVATION_STATUS: Record<string, { label: string; type: 'success' | 'warning' | 'info' | 'danger' | 'primary' }> = {
  PENDING: { label: '待确认', type: 'warning' },
  CONFIRMED: { label: '已确认', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' },
  COMPLETED: { label: '已核销', type: 'primary' }
}
export const ROUTE_TYPE: Record<string, string> = { CLASSIC: '经典路线', THEME: '主题路线', QUICK: '快速路线', FAMILY: '亲子路线' }
export const ENTITY_TYPE: Record<string, string> = {
  ARTIFACT: '文物', PIT: '祭祀坑', MATERIAL: '材质', PERIOD: '年代', CULTURE: '文化', THEME: '主题', SITE: '遗址',
  MUSEUM: '博物馆', PERSON: '人物', HALL: '展厅', LITERATURE: '文献', TECH: '工艺'
}
/** 知识图谱实体类型配色（与青铜主题协调的低饱和色） */
export const ENTITY_COLORS: Record<string, string> = {
  ARTIFACT: '#E0B866', PIT: '#C9694F', MATERIAL: '#7FB8A1', PERIOD: '#A48FD0', CULTURE: '#D79A5B', THEME: '#E58FA0',
  SITE: '#8DA4D8', MUSEUM: '#F0D9A0', PERSON: '#79C3D6', HALL: '#9BBF6A', LITERATURE: '#C7B48D', TECH: '#B7A6E0'
}
export const ARTIFACT_STATUS: Record<number, string> = { 0: '库房', 1: '在展', 2: '修复中' }
