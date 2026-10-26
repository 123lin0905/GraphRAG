<template>
  <div class="hall">
    <div class="stage">
      <ModelViewer v-if="cur" :key="cur.key + ':' + (curModel?.id ?? '')" :model="curModel" :demo="cur.kind === 'demo'" :fit-width="fitWidth" />
      <div v-else class="stage-empty"><SunLogo :size="64" spin /><span>正在载入数字展厅…</span></div>
    </div>

    <!-- 左侧：模型切换 -->
    <aside class="switcher" :class="{ collapsed }">
      <div class="sw-head">
        <div>
          <div class="eyebrow">DIGITAL HALL</div>
          <h2>3D 数字展厅</h2>
        </div>
        <button class="icon-btn" @click="collapsed = !collapsed"><el-icon><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon></button>
      </div>
      <div class="sw-list">
        <template v-for="g in groups" :key="g.kind">
          <div v-if="g.items.length" class="sw-group">
            <span>{{ g.title }}</span><em>{{ g.items.length }}</em>
          </div>
          <button v-for="e in g.items" :key="e.key" class="sw-item" :class="[{ on: cur?.key === e.key }, e.kind]" @click="select(e)">
            <div class="sw-thumb"><BronzeImage :src="e.cover" :alt="e.title" ratio="1 / 1" /></div>
            <div class="sw-txt">
              <b class="ellipsis">{{ e.title }}</b>
              <span class="ellipsis">{{ e.sub }}</span>
              <i v-if="e.kind === 'real'" class="tag real">真实扫描</i>
              <i v-else-if="e.kind === 'splat'" class="tag splat">3DGS</i>
              <i v-else-if="e.kind === 'pending'" class="tag pending">模型采集中</i>
              <i v-else class="tag demo">技术演示 · 非文物</i>
            </div>
          </button>
        </template>
      </div>
      <button class="sw-foot" @click="showCredits = true">
        <el-icon><Document /></el-icon>模型来源与授权（{{ credits.length }}）
      </button>
    </aside>

    <!-- 右侧：信息卡 -->
    <aside v-if="cur" class="info-card" :class="{ hidden: hideInfo }">
      <button class="close" @click="hideInfo = !hideInfo"><el-icon><component :is="hideInfo ? 'InfoFilled' : 'Close'" /></el-icon></button>
      <template v-if="!hideInfo">
        <div class="eyebrow">{{ art?.artifactNo || (cur.kind === 'demo' ? '3DGS DEMO' : 'SCENE') }}</div>
        <h3>{{ cur.title }}</h3>

        <!-- 真实扫描 -->
        <template v-if="cur.kind === 'real' && curModel">
          <p class="clamp-3">{{ art?.description }}</p>
          <el-select v-if="cur.models.length > 1" v-model="modelIdx" size="small" class="alt-select">
            <el-option v-for="(m, i) in cur.models" :key="m.id" :label="m.name" :value="i" />
          </el-select>
          <div class="m-grid">
            <div><label>模型类型</label><b>真实扫描</b></div>
            <div><label>格式</label><b>{{ (curModel.format || '').toUpperCase() }} 网格</b></div>
            <div><label>文件大小</label><b>{{ fmtBytes(curModel.fileSize) }}</b></div>
            <div><label>授权</label><b>{{ curModel.license || '—' }}</b></div>
          </div>
          <dl class="src">
            <dt>采集方式</dt><dd>{{ curModel.captureDevice || '—' }}</dd>
            <dt>模型作者</dt><dd>{{ curModel.author || '—' }}</dd>
            <dt>来源</dt><dd><a v-if="curModel.sourceUrl" :href="curModel.sourceUrl" target="_blank" rel="noopener">Sketchfab 原页面 ↗</a><span v-else>—</span></dd>
          </dl>
        </template>

        <!-- 3DGS 模型（文件已上线 / 采集中） -->
        <template v-else-if="cur.kind === 'splat' || cur.kind === 'pending'">
          <p class="clamp-3">{{ art?.description || '展厅级 3DGS 场景模型，用于数字孪生漫游与路线导览。' }}</p>
          <div v-if="cur.kind === 'pending'" class="notice">
            <el-icon><Camera /></el-icon>
            <span>该{{ cur.artifactId ? '文物' : '场景' }}的三维模型仍在采集与训练中，暂不提供可交互模型；本系统不会用无关的示例模型代替。</span>
          </div>
          <div v-else class="m-grid">
            <div><label>PSNR</label><b>{{ curModel?.psnr ?? '—' }}</b></div>
            <div><label>SSIM</label><b>{{ curModel?.ssim ?? '—' }}</b></div>
            <div><label>LPIPS</label><b>{{ curModel?.lpips ?? '—' }}</b></div>
            <div><label>高斯基元</label><b>{{ fmtNum(curModel?.gaussianCount) }}</b></div>
          </div>
        </template>

        <!-- 技术演示 -->
        <template v-else>
          <p>这是一个公开的 3D 高斯泼溅示例场景（<b>并非三星堆文物</b>），仅用于演示本系统 3DGS 查看器的实时渲染能力。文物的 3DGS 模型完成采集训练后将替换为真实数据。</p>
          <div class="notice"><el-icon><Warning /></el-icon><span>示例数据来源：{{ demoSource }}</span></div>
        </template>

        <div class="ic-actions">
          <router-link v-if="cur.artifactId" :to="`/artifacts/${cur.artifactId}`" class="btn-gold btn-sm">文物详情</router-link>
          <router-link v-if="cur.kind !== 'demo'" :to="{ path: '/guide', query: { q: `介绍一下${cur.title}` } }" class="btn-ghost btn-sm">问导览</router-link>
        </div>
      </template>
    </aside>

    <!-- 模型来源与授权 -->
    <el-dialog v-model="showCredits" title="模型来源与授权" width="760px" class="credit-dialog" append-to-body>
      <p class="credit-intro">
        本展厅中标注“真实扫描”的三维模型均来自 Sketchfab 平台的公开模型（经 Objaverse 数据集镜像获取），按
        <a href="https://creativecommons.org/licenses/by/4.0/deed.zh-hans" target="_blank" rel="noopener">知识共享 署名 4.0 国际许可协议（CC BY 4.0）</a>
        使用，未对几何形状作修改，仅调整了展示光照。版权归原作者所有，特此署名致谢。
      </p>
      <el-table :data="credits" size="small" class="credit-table">
        <el-table-column label="文物 / 模型" min-width="210">
          <template #default="{ row }"><b>{{ row.artifact }}</b><div class="muted">{{ row.name }}</div></template>
        </el-table-column>
        <el-table-column label="作者" prop="author" min-width="170" />
        <el-table-column label="来源" min-width="120">
          <template #default="{ row }"><a :href="row.url" target="_blank" rel="noopener">Sketchfab ↗</a></template>
        </el-table-column>
        <el-table-column label="授权" prop="license" width="100" />
      </el-table>
      <p class="credit-intro muted">“3DGS 渲染技术演示”使用的公开示例场景：{{ demoSource }}，与三星堆文物无关。</p>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import ModelViewer from '@/components/ModelViewer.vue'
import BronzeImage from '@/components/BronzeImage.vue'
import SunLogo from '@/components/SunLogo.vue'
import { artifactApi, dictApi } from '@/api'
import type { Artifact, Model3d } from '@/api/types'
import { fmtBytes, fmtNum, isMeshModel, sortModels } from '@/utils/format'

type Kind = 'real' | 'splat' | 'pending' | 'demo'
interface Entry { key: string; kind: Kind; title: string; sub: string; cover?: string; artifactId?: number; models: Model3d[] }

const route = useRoute()
const arts = ref<Record<number, Artifact>>({})
const entries = ref<Entry[]>([])
const cur = ref<Entry>()
const modelIdx = ref(0)
const collapsed = ref(false)
const hideInfo = ref(false)
const showCredits = ref(false)
/** 左右浮层占去约 45% 宽度，取景时只按中间区域计算 */
const fitWidth = window.innerWidth >= 1100 ? 0.5 : 1
const allModels = ref<Model3d[]>([])
const demoSource = 'Hugging Face「cakewalk/splat-data」train.splat（Tanks and Temples 数据集场景）'

const art = computed(() => (cur.value?.artifactId ? arts.value[cur.value.artifactId] : undefined))
const curModel = computed(() => cur.value?.models[modelIdx.value])
const groups = computed(() => [
  { kind: 'real', title: '真实三维扫描', items: entries.value.filter((e) => e.kind === 'real' || e.kind === 'splat') },
  { kind: 'pending', title: '模型采集中', items: entries.value.filter((e) => e.kind === 'pending') },
  { kind: 'demo', title: '技术演示', items: entries.value.filter((e) => e.kind === 'demo') }
])
const credits = computed(() =>
  allModels.value
    .filter((m) => isMeshModel(m) && m.sourceUrl)
    .map((m) => ({ artifact: (m.artifactId && arts.value[m.artifactId]?.name) || m.name, name: m.name, author: m.author, url: m.sourceUrl, license: m.license }))
)

function select(e: Entry) { cur.value = e; modelIdx.value = 0 }
watch(cur, () => { modelIdx.value = 0 })

/** 模型文件是否真实存在（占位地址在静态服务器上会回退为 index.html） */
async function exists(url?: string) {
  if (!url) return false
  if (/^https?:\/\//.test(url) && !url.startsWith(location.origin)) return true
  try {
    const r = await fetch(url, { method: 'HEAD' })
    return r.ok && !(r.headers.get('content-type') || '').includes('text/html')
  } catch {
    return false
  }
}

onMounted(async () => {
  const [ms, page] = await Promise.all([dictApi.models(), artifactApi.search({ size: 100 })])
  page.records.forEach((a) => (arts.value[a.id] = a))
  const usable = ms.filter((m) => m.status !== 2)
  allModels.value = usable
  const byArt = new Map<string, Model3d[]>()
  usable.forEach((m) => {
    const k = m.artifactId ? `a${m.artifactId}` : `m${m.id}`
    byArt.set(k, [...(byArt.get(k) || []), m])
  })
  const list: Entry[] = []
  for (const [key, group] of byArt) {
    const aid = group[0].artifactId
    const a = aid ? arts.value[aid] : undefined
    const title = a?.name || group[0].name.replace(/ ?3DGS.*$/, '')
    const real = sortModels(group.filter(isMeshModel))
    if (real.length) {
      list.push({ key, kind: 'real', title, artifactId: aid, models: real, cover: real[0].thumbnailUrl || a?.coverUrl,
        sub: `真实扫描 · ${(real[0].format || '').toUpperCase()}${real.length > 1 ? ` · ${real.length} 个扫描版本` : ''}` })
      continue
    }
    const splats = sortModels(group)
    const ok = await exists(splats[0].modelUrl)
    list.push({ key, kind: ok ? 'splat' : 'pending', title, artifactId: aid, models: splats,
      cover: a?.coverUrl || '/static/images/artifacts/shentan_detail.jpg',
      sub: ok ? `3DGS · ${(splats[0].format || '').toUpperCase()}` : `${group[0].modelType === 'SCENE' ? '展厅场景' : '单体文物'} · 3DGS 模型采集中` })
  }
  list.sort((x, y) => ['real', 'splat', 'pending'].indexOf(x.kind) - ['real', 'splat', 'pending'].indexOf(y.kind) || (x.artifactId || 999) - (y.artifactId || 999))
  list.push({ key: 'demo', kind: 'demo', title: '3DGS 渲染技术演示', sub: '公开示例场景 · 非三星堆文物', models: [], cover: '/static/images/hero.jpg' })
  entries.value = list
  const want = Number(route.query.artifact)
  cur.value = list.find((e) => e.artifactId === want) || list[0]
  if (window.innerWidth < 800) { collapsed.value = true; hideInfo.value = true }
})
</script>

<style scoped>
.hall { position: relative; height: 100vh; padding-top: var(--header-h); overflow: hidden; background: #050706; }
.stage { position: absolute; inset: var(--header-h) 0 0 0; }
.stage-empty { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14px; color: var(--text-3); letter-spacing: .2em; }
.eyebrow { font-size: 11px; letter-spacing: .4em; color: var(--gold); }
.switcher { position: absolute; z-index: 10; top: calc(var(--header-h) + 18px); left: 18px; width: 300px; display: flex; flex-direction: column;
  background: rgba(9, 12, 10, .74); backdrop-filter: blur(18px) saturate(1.2); border: 1px solid var(--line); border-radius: 18px; box-shadow: var(--shadow-1); transition: width .4s var(--ease); overflow: hidden; max-height: calc(100% - var(--header-h) - 36px); }
.switcher.collapsed { width: 64px; }
.stage :deep(.sv-badges), .stage :deep(.gv-badges) { left: 50%; transform: translateX(-50%); top: 18px; }
.stage :deep(.gv-tools) { top: auto; bottom: 18px; right: 50%; transform: translateX(50%); }
.stage :deep(.gv-tips) { display: none; }
.stage :deep(.gv-credit) { right: 24px; bottom: 24px; max-width: 420px; }
.switcher.collapsed .sw-head > div, .switcher.collapsed .sw-list, .switcher.collapsed .sw-foot { display: none; }
.sw-head { display: flex; align-items: center; justify-content: space-between; padding: 18px 16px 14px 20px; border-bottom: 1px solid var(--line); gap: 8px; }
.sw-head h2 { font-size: 20px; letter-spacing: .14em; color: var(--gold-light); }
.icon-btn { width: 32px; height: 32px; flex: none; display: grid; place-items: center; border-radius: 10px; border: 1px solid var(--line); background: rgba(201,164,92,.06); color: var(--gold-light); cursor: pointer; }
.sw-list { flex: 1; overflow: auto; padding: 6px 10px 10px; display: flex; flex-direction: column; gap: 4px; }
.sw-group { display: flex; align-items: center; justify-content: space-between; padding: 10px 6px 4px; font-size: 11.5px; letter-spacing: .2em; color: var(--text-3); }
.sw-group em { font-style: normal; color: var(--gold); letter-spacing: 0; }
.sw-item { display: flex; gap: 12px; align-items: center; padding: 7px 8px; border-radius: 12px; border: 1px solid transparent; background: none; cursor: pointer; text-align: left; color: var(--text-1); transition: all .25s; }
.sw-item:hover { background: rgba(201,164,92,.06); }
.sw-item.on { background: linear-gradient(90deg, rgba(201,164,92,.16), rgba(201,164,92,.03)); border-color: var(--line-strong); }
.sw-item.pending .sw-thumb { filter: grayscale(.85) brightness(.7); }
.sw-thumb { width: 52px; flex: none; border-radius: 10px; overflow: hidden; border: 1px solid var(--line); }
.sw-txt { min-width: 0; display: flex; flex-direction: column; gap: 1px; align-items: flex-start; }
.sw-txt b { font-family: var(--font-serif); font-size: 15px; letter-spacing: .06em; max-width: 200px; }
.sw-item.on .sw-txt b { color: var(--gold-light); }
.sw-txt span { font-size: 11.5px; color: var(--text-3); max-width: 200px; }
.tag { font-style: normal; font-size: 10.5px; line-height: 1; padding: 3px 7px; border-radius: 999px; margin-top: 3px; letter-spacing: .05em; }
.tag.real { color: #1a140a; background: var(--gold); }
.tag.splat { color: #9fd4bd; border: 1px solid rgba(111,174,149,.35); }
.tag.pending { color: var(--text-3); border: 1px dashed rgba(201,164,92,.35); }
.tag.demo { color: #e59a86; border: 1px solid rgba(181,82,59,.4); }
.sw-foot { display: flex; align-items: center; justify-content: center; gap: 6px; padding: 11px; border: none; border-top: 1px solid var(--line); background: rgba(201,164,92,.05); color: var(--gold-light); cursor: pointer; font-size: 12.5px; letter-spacing: .08em; }
.sw-foot:hover { background: rgba(201,164,92,.12); }
.info-card { position: absolute; z-index: 10; top: calc(var(--header-h) + 18px); right: 18px; width: 320px; padding: 22px; background: rgba(9, 12, 10, .74); backdrop-filter: blur(18px); border: 1px solid var(--line); border-radius: 18px; box-shadow: var(--shadow-1); max-height: calc(100% - var(--header-h) - 36px); overflow: auto; }
.info-card.hidden { width: auto; padding: 6px; }
.info-card.hidden .close { position: static; }
.close { position: absolute; top: 12px; right: 12px; width: 28px; height: 28px; border-radius: 8px; border: 1px solid var(--line); background: transparent; color: var(--text-3); cursor: pointer; display: grid; place-items: center; }
.info-card h3 { font-size: 24px; letter-spacing: .12em; margin: 6px 0 10px; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
.info-card p { color: var(--text-2); font-size: 13px; line-height: 1.8; }
.alt-select { width: 100%; margin-top: 12px; }
.m-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin: 14px 0; }
.m-grid div { padding: 8px 10px; border-radius: 10px; background: rgba(201,164,92,.05); border: 1px solid var(--line); }
.m-grid label { display: block; font-size: 11px; color: var(--text-3); }
.m-grid b { font-family: var(--font-serif); color: var(--gold-light); font-size: 15px; }
.src { display: grid; grid-template-columns: 64px 1fr; gap: 6px 10px; font-size: 12.5px; margin: 0 0 16px; }
.src dt { color: var(--text-3); }
.src dd { margin: 0; color: var(--text-2); word-break: break-all; }
.src a, .credit-intro a, .credit-table a { color: var(--gold-light); }
.notice { display: flex; gap: 8px; align-items: flex-start; margin: 14px 0; padding: 10px 12px; border-radius: 10px; font-size: 12.5px; line-height: 1.7; color: var(--text-2);
  border: 1px dashed rgba(201,164,92,.35); background: rgba(201,164,92,.05); }
.notice .el-icon { color: var(--gold); margin-top: 3px; flex: none; }
.ic-actions { display: flex; gap: 10px; }
.credit-intro { font-size: 13px; line-height: 1.8; color: var(--text-2); margin: 0 0 12px; }
.credit-intro.muted { margin-top: 12px; color: var(--text-3); font-size: 12px; }
@media (max-width: 800px) { .switcher { width: calc(100% - 36px); max-height: 50vh; } .info-card { top: auto; bottom: 18px; } .stage :deep(.gv-credit) { right: 12px; } }
</style>
