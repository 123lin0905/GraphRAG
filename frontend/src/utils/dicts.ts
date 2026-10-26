import { http } from '@/api/http'

export interface Option { label: string; value: any }
const cache = new Map<string, Promise<Option[]>>()

interface DictDef { url: string; label: (r: any) => string; params?: object }
const DEFS: Record<string, DictDef> = {
  categories: { url: '/categories/list', label: (r) => (r.parentId ? '　└ ' : '') + r.name },
  topCategories: { url: '/categories/list', label: (r) => r.name, params: { parentId: 0 } },
  pits: { url: '/pits/list', label: (r) => `${r.pitCode} ${r.name}` },
  materials: { url: '/materials/list', label: (r) => r.name },
  periods: { url: '/periods/list', label: (r) => r.name },
  halls: { url: '/halls/list', label: (r) => r.name },
  museums: { url: '/museums/list', label: (r) => r.name },
  showcases: { url: '/showcases/list', label: (r) => `${r.caseCode} ${r.name}` },
  artifacts: { url: '/artifacts/list', label: (r) => `${r.name}` },
  entities: { url: '/kg/entities/list', label: (r) => r.name },
  routes: { url: '/routes/list', label: (r) => r.name },
  roles: { url: '/roles/list', label: (r) => r.roleName }
}

export function loadDict(key: string, force = false): Promise<Option[]> {
  if (!force && cache.has(key)) return cache.get(key)!
  const def = DEFS[key]
  if (!def) return Promise.resolve([])
  const p = http
    .get<any[]>(def.url, def.params, { quiet: true })
    .then((rows) => {
      let list = rows
      if (key === 'categories') {
        // 父分类后紧跟子分类
        const tops = rows.filter((r) => !r.parentId)
        list = tops.flatMap((t) => [t, ...rows.filter((r) => r.parentId === t.id)])
        const rest = rows.filter((r) => !list.includes(r))
        list = list.concat(rest)
      }
      return list.map((r) => ({ label: def.label(r), value: r.id }))
    })
    .catch(() => {
      cache.delete(key)
      return []
    })
  cache.set(key, p)
  return p
}
export function clearDict(key?: string) {
  if (key) cache.delete(key)
  else cache.clear()
}
