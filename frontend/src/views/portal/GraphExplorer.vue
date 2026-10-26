<template>
  <div class="gx">
    <aside class="gx-side panel">
      <div class="eyebrow">KNOWLEDGE GRAPH</div>
      <h2>三星堆知识图谱</h2>
      <p class="muted intro">{{ stats.entities }} 个实体 · {{ stats.relations }} 条关系，点击节点查看详情，拖拽、滚轮缩放。</p>

      <div class="blk">
        <div class="blk-t"><el-icon><Search /></el-icon>实体检索</div>
        <el-select v-model="searchId" filterable remote :remote-method="doSearch" :loading="searching" placeholder="输入实体名称，如：金杖" clearable style="width: 100%" @change="onPick">
          <el-option v-for="o in searchOpts" :key="o.id" :label="o.name" :value="o.id">
            <span>{{ o.name }}</span><span class="opt-type">{{ ENTITY_TYPE[o.entityType] }}</span>
          </el-option>
        </el-select>
      </div>

      <div class="blk">
        <div class="blk-t"><el-icon><Connection /></el-icon>最短关联路径</div>
        <el-select v-model="fromId" filterable remote :remote-method="(k: string) => doSearch(k, 'from')" placeholder="起点实体" style="width: 100%">
          <el-option v-for="o in fromOpts" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <div class="swap"><el-icon @click="swap"><Sort /></el-icon></div>
        <el-select v-model="toId" filterable remote :remote-method="(k: string) => doSearch(k, 'to')" placeholder="终点实体" style="width: 100%">
          <el-option v-for="o in toOpts" :key="o.id" :label="o.name" :value="o.id" />
        </el-select>
        <el-button type="primary" style="width: 100%; margin-top: 10px" :disabled="!fromId || !toId" :loading="pathLoading" @click="findPath">查找路径</el-button>
        <div v-if="pathNodes.length" class="path-res">
          <template v-for="(n, i) in pathNodes" :key="n.id">
            <span class="pn">{{ n.name }}</span>
            <em v-if="i < pathNodes.length - 1">{{ pathRel(i) }}</em>
          </template>
        </div>
      </div>

      <div class="blk">
        <div class="blk-t"><el-icon><Filter /></el-icon>实体类型</div>
        <div class="types">
          <button v-for="t in typeList" :key="t.code" :class="{ on: types.includes(t.code) }" @click="toggleType(t.code)">
            <i :style="{ background: ENTITY_COLORS[t.code] }"></i>{{ t.name }}
          </button>
        </div>
      </div>

      <div class="mode">
        <span>当前视图：<b>{{ modeLabel }}</b></span>
        <el-button v-if="mode !== 'full'" size="small" @click="loadFull">返回全图</el-button>
      </div>
    </aside>

    <section class="gx-main panel">
      <KgGraph :graph="graph" height="100%" :highlight="highlight" :center-id="centerId" :repulsion="mode === 'full' ? 1100 : 420" :zoom="mode === 'full' ? 0.72 : 0.95" @node-click="selectNode" class="gx-graph" />
    </section>

    <transition name="slide">
      <aside v-if="sel" class="gx-detail panel">
        <button class="x" @click="sel = null"><el-icon><Close /></el-icon></button>
        <span class="chip" :style="{ color: ENTITY_COLORS[sel.entityType], borderColor: ENTITY_COLORS[sel.entityType] + '55' }">{{ ENTITY_TYPE[sel.entityType] || sel.entityType }}</span>
        <h3>{{ sel.name }}</h3>
        <p>{{ sel.description || '暂无描述' }}</p>
        <div class="nb">
          <div class="nb-t">直接关联（{{ neighborLinks.length }}）</div>
          <div v-for="l in neighborLinks.slice(0, 12)" :key="l.key" class="nb-i" @click="selectById(l.otherId)">
            <em>{{ l.rel }}</em><span>{{ l.other }}</span>
          </div>
        </div>
        <div class="d-actions">
          <el-button type="primary" size="small" @click="expand(sel.id)"><el-icon><Aim /></el-icon>展开 2 跳邻居</el-button>
          <el-button v-if="sel.refTable === 'artifact'" size="small" @click="$router.push(`/artifacts/${sel.refId}`)">文物详情</el-button>
          <el-button size="small" @click="$router.push({ path: '/guide', query: { q: `${sel.name}有什么特点？` } })">问导览</el-button>
        </div>
      </aside>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import KgGraph from '@/components/KgGraph.vue'
import { kgApi } from '@/api'
import type { GraphNode, GraphVO } from '@/api/types'
import { ENTITY_COLORS, ENTITY_TYPE } from '@/utils/format'

const route = useRoute()
const graph = ref<GraphVO | null>(null)
const fullGraph = ref<GraphVO | null>(null)
const mode = ref<'full' | 'neighbors' | 'path'>('full')
const highlight = ref<string[]>([])
const centerId = ref<string | null>(null)
const sel = ref<GraphNode | null>(null)
const types = ref<string[]>([])
const typeList = Object.entries(ENTITY_TYPE).map(([code, name]) => ({ code, name }))
const stats = ref({ entities: 0, relations: 0 })

const searchId = ref<string>()
const searchOpts = ref<GraphNode[]>([])
const fromOpts = ref<GraphNode[]>([])
const toOpts = ref<GraphNode[]>([])
const searching = ref(false)
const fromId = ref<string>()
const toId = ref<string>()
const pathLoading = ref(false)
const pathNodes = ref<GraphNode[]>([])
const pathGraph = ref<GraphVO | null>(null)

const modeLabel = computed(() => ({ full: types.value.length ? '按类型筛选' : '全量图谱', neighbors: `“${sel.value?.name || ''}”邻居子图`, path: '最短路径' })[mode.value])

const neighborLinks = computed(() => {
  const g = graph.value
  const s = sel.value
  if (!g || !s) return []
  return g.links
    .filter((l) => l.source === s.id || l.target === s.id)
    .map((l) => {
      const otherId = l.source === s.id ? l.target : l.source
      const other = g.nodes.find((n) => n.id === otherId)?.name || otherId
      return { key: (l.id || '') + l.source + l.target, rel: l.source === s.id ? l.name : `被${l.name}`, other, otherId }
    })
})

async function loadFull() {
  mode.value = 'full'
  highlight.value = []
  centerId.value = null
  pathNodes.value = []
  graph.value = await kgApi.graph(types.value.length ? types.value.join(',') : undefined, 300)
  if (!types.value.length) fullGraph.value = graph.value
}
function toggleType(c: string) {
  const i = types.value.indexOf(c)
  if (i >= 0) types.value.splice(i, 1)
  else types.value.push(c)
  loadFull()
}
async function doSearch(k: string, target: 'main' | 'from' | 'to' = 'main') {
  if (!k) return
  searching.value = true
  try {
    const r = await kgApi.search(k, 15)
    if (target === 'from') fromOpts.value = r
    else if (target === 'to') toOpts.value = r
    else searchOpts.value = r
  } finally {
    searching.value = false
  }
}
async function expand(id: string) {
  mode.value = 'neighbors'
  pathNodes.value = []
  highlight.value = []
  centerId.value = id
  graph.value = await kgApi.neighbors(id, 2, 80)
  sel.value = graph.value.nodes.find((n) => n.id === id) || sel.value
}
function onPick(id?: string) {
  if (!id) return
  const n = searchOpts.value.find((o) => o.id === id)
  if (n) sel.value = n
  expand(id)
}
function selectNode(n: GraphNode) { sel.value = n; centerId.value = n.id }
function selectById(id: string) {
  const n = graph.value?.nodes.find((x) => x.id === id)
  if (n) selectNode(n)
}
function swap() { const f = fromId.value; const fo = fromOpts.value; fromId.value = toId.value; fromOpts.value = toOpts.value; toId.value = f; toOpts.value = fo }
async function findPath() {
  if (!fromId.value || !toId.value) return
  pathLoading.value = true
  try {
    const g = await kgApi.path(fromId.value, toId.value, 6)
    if (!g.nodes.length) { ElMessage.info('在 6 跳内未找到关联路径'); return }
    pathGraph.value = g
    // 按跳数排列路径节点
    pathNodes.value = [...g.nodes].sort((a, b) => (a.hop ?? 0) - (b.hop ?? 0))
    mode.value = 'path'
    centerId.value = String(fromId.value)
    highlight.value = g.nodes.map((n) => n.id)
    // 在全图背景上高亮路径
    const base = fullGraph.value || g
    const ids = new Set(base.nodes.map((n) => n.id))
    graph.value = {
      categories: base.categories,
      nodes: [...base.nodes, ...g.nodes.filter((n) => !ids.has(n.id))],
      links: [...base.links.filter((l) => !g.links.some((p) => (p.source === l.source && p.target === l.target) || (p.source === l.target && p.target === l.source))), ...g.links]
    }
  } finally {
    pathLoading.value = false
  }
}
function pathRel(i: number) {
  const a = pathNodes.value[i].id
  const b = pathNodes.value[i + 1].id
  const l = pathGraph.value?.links.find((x) => (x.source === a && x.target === b) || (x.source === b && x.target === a))
  return l?.name || '—'
}

onMounted(async () => {
  kgApi.statistics().then((s) => {
    stats.value.entities = s.entityTypes.reduce((x, y) => x + y.value, 0)
    stats.value.relations = s.relationTypes.reduce((x, y) => x + y.value, 0)
  })
  await loadFull()
  const focus = route.query.focus as string | undefined
  if (focus) {
    const n = graph.value?.nodes.find((x) => x.id === focus)
    if (n) sel.value = n
    expand(focus)
  }
  // 预置路径示例：青铜神树 → 山海经（若存在）
  const nodes = fullGraph.value?.nodes || []
  const a = nodes.find((n) => n.name === '青铜神树')
  const b = nodes.find((n) => n.entityType === 'LITERATURE') || nodes.find((n) => n.entityType === 'PERSON')
  if (a && b) { fromOpts.value = [a]; toOpts.value = [b]; fromId.value = a.id; toId.value = b.id }
})
</script>

<style scoped>
.gx { display: grid; grid-template-columns: 300px 1fr; gap: 16px; height: 100vh; padding: calc(var(--header-h) + 16px) 16px 16px; position: relative; }
.gx-side { padding: 20px; overflow: auto; }
.eyebrow { font-size: 11px; letter-spacing: .4em; color: var(--gold); }
.gx-side h2 { font-size: 22px; letter-spacing: .14em; margin: 4px 0 6px; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
.intro { font-size: 12.5px; }
.blk { margin-top: 20px; padding-top: 16px; border-top: 1px solid var(--line); }
.blk-t { display: flex; align-items: center; gap: 6px; color: var(--gold-light); font-family: var(--font-serif); letter-spacing: .12em; margin-bottom: 10px; font-size: 14px; }
.opt-type { float: right; color: var(--text-3); font-size: 12px; }
.swap { text-align: center; color: var(--gold); margin: 4px 0; cursor: pointer; }
.path-res { margin-top: 12px; display: flex; flex-wrap: wrap; align-items: center; gap: 4px; font-size: 12.5px; }
.pn { padding: 2px 8px; border-radius: 6px; background: rgba(201,164,92,.12); color: var(--gold-pale); border: 1px solid var(--line-strong); }
.path-res em { font-style: normal; color: var(--text-3); font-size: 11px; }
.path-res em::before { content: '—'; margin-right: 2px; } .path-res em::after { content: '→'; margin-left: 2px; }
.types { display: flex; flex-wrap: wrap; gap: 6px; }
.types button { display: inline-flex; align-items: center; gap: 6px; height: 28px; padding: 0 10px; border-radius: 999px; border: 1px solid var(--line); background: transparent; color: var(--text-3); cursor: pointer; font-size: 12px; transition: all .2s; }
.types button i { width: 8px; height: 8px; border-radius: 50%; }
.types button.on { color: var(--text-1); border-color: var(--line-strong); background: rgba(201,164,92,.1); }
.mode { margin-top: 20px; padding-top: 14px; border-top: 1px solid var(--line); display: flex; align-items: center; justify-content: space-between; font-size: 12.5px; color: var(--text-3); gap: 8px; }
.mode b { color: var(--gold-light); font-weight: 500; }
.gx-main { padding: 0; background: radial-gradient(circle at 50% 45%, rgba(36,57,45,.35), rgba(8,11,9,.95) 75%); min-height: 0; }
.gx-graph { height: 100%; }
.gx-detail { position: absolute; top: calc(var(--header-h) + 32px); right: 32px; width: 330px; max-height: calc(100vh - var(--header-h) - 64px); overflow: auto; padding: 22px; z-index: 5;
  background: rgba(12, 16, 13, .9); backdrop-filter: blur(16px); }
.x { position: absolute; right: 12px; top: 12px; background: none; border: 1px solid var(--line); border-radius: 8px; color: var(--text-3); width: 28px; height: 28px; cursor: pointer; display: grid; place-items: center; }
.gx-detail h3 { font-size: 24px; margin: 10px 0; letter-spacing: .1em; color: var(--gold-light); }
.gx-detail p { color: var(--text-2); font-size: 13.5px; line-height: 1.85; }
.nb { margin-top: 16px; }
.nb-t { color: var(--text-3); font-size: 12px; letter-spacing: .15em; margin-bottom: 8px; }
.nb-i { display: flex; gap: 8px; align-items: center; padding: 7px 10px; border-radius: 8px; cursor: pointer; font-size: 13px; transition: background .2s; }
.nb-i:hover { background: rgba(201,164,92,.08); }
.nb-i em { font-style: normal; color: var(--gold); font-size: 12px; min-width: 64px; }
.d-actions { margin-top: 16px; display: flex; flex-wrap: wrap; gap: 8px; }
.d-actions .el-button + .el-button { margin-left: 0; }
.slide-enter-active, .slide-leave-active { transition: all .4s var(--ease); }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateX(30px); }
@media (max-width: 900px) { .gx { grid-template-columns: 1fr; height: auto; } .gx-main { height: 70vh; } .gx-detail { position: fixed; left: 16px; right: 16px; width: auto; top: auto; bottom: 16px; max-height: 50vh; } }
</style>
