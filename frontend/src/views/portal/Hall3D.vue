<template>
  <div class="hall">
    <div class="stage">
      <SplatViewer v-if="cur" :key="cur.id" :url="cur.modelUrl" :camera-position="parseVec(cur.cameraPosition)" :camera-target="parseVec(cur.cameraTarget)" :format="cur.format" />
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
        <button v-for="m in models" :key="m.id" class="sw-item" :class="{ on: cur?.id === m.id }" @click="cur = m">
          <div class="sw-thumb"><BronzeImage :src="coverOf(m)" :alt="m.name" ratio="1 / 1" /></div>
          <div class="sw-txt">
            <b class="ellipsis">{{ titleOf(m) }}</b>
            <span>{{ m.modelType === 'SCENE' ? '展厅场景' : '单体文物' }} · {{ (m.format || '').toUpperCase() }} · {{ fmtNum(m.gaussianCount) }} 高斯</span>
          </div>
        </button>
      </div>
    </aside>

    <!-- 右侧：信息卡 -->
    <aside v-if="cur" class="info-card" :class="{ hidden: hideInfo }">
      <button class="close" @click="hideInfo = !hideInfo"><el-icon><component :is="hideInfo ? 'InfoFilled' : 'Close'" /></el-icon></button>
      <template v-if="!hideInfo">
        <div class="eyebrow">{{ art?.artifactNo || 'SCENE' }}</div>
        <h3>{{ titleOf(cur) }}</h3>
        <p class="clamp-3">{{ art?.description || '展厅级 3DGS 场景模型，用于数字孪生漫游与路线导览。' }}</p>
        <div class="m-grid">
          <div><label>PSNR</label><b>{{ cur.psnr ?? '—' }}</b></div>
          <div><label>SSIM</label><b>{{ cur.ssim ?? '—' }}</b></div>
          <div><label>LPIPS</label><b>{{ cur.lpips ?? '—' }}</b></div>
          <div><label>训练迭代</label><b>{{ fmtNum(cur.trainIterations) }}</b></div>
        </div>
        <div class="ic-actions">
          <router-link v-if="cur.artifactId" :to="`/artifacts/${cur.artifactId}`" class="btn-gold btn-sm">文物详情</router-link>
          <router-link :to="{ path: '/guide', query: { q: `介绍一下${titleOf(cur)}` } }" class="btn-ghost btn-sm">问导览</router-link>
        </div>
      </template>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SplatViewer from '@/components/SplatViewer.vue'
import BronzeImage from '@/components/BronzeImage.vue'
import SunLogo from '@/components/SunLogo.vue'
import { artifactApi, dictApi } from '@/api'
import type { Artifact, Model3d } from '@/api/types'
import { fmtNum, parseVec } from '@/utils/format'

const route = useRoute()
const models = ref<Model3d[]>([])
const arts = ref<Record<number, Artifact>>({})
const cur = ref<Model3d>()
const collapsed = ref(false)
const hideInfo = ref(false)
const art = computed(() => (cur.value?.artifactId ? arts.value[cur.value.artifactId] : undefined))

function titleOf(m: Model3d) { return m.artifactId ? arts.value[m.artifactId]?.name || m.name : m.name.replace(/ ?3DGS.*$/, '') }
function coverOf(m: Model3d) { return m.artifactId ? arts.value[m.artifactId]?.coverUrl : '/static/images/artifacts/shentan_detail.jpg' }

onMounted(async () => {
  const [ms, page] = await Promise.all([dictApi.models({ status: 1 }), artifactApi.search({ size: 100 })])
  page.records.forEach((a) => (arts.value[a.id] = a))
  // 同一文物只保留默认模型
  const seen = new Set<number>()
  models.value = ms
    .sort((a, b) => (b.isDefault || 0) - (a.isDefault || 0))
    .filter((m) => { if (!m.artifactId) return true; if (seen.has(m.artifactId)) return false; seen.add(m.artifactId); return true })
    .sort((a, b) => (a.artifactId || 999) - (b.artifactId || 999))
  const want = Number(route.query.artifact)
  cur.value = models.value.find((m) => m.artifactId === want) || models.value[0]
  if (window.innerWidth < 800) { collapsed.value = true; hideInfo.value = true }
})
</script>

<style scoped>
.hall { position: relative; height: 100vh; padding-top: var(--header-h); overflow: hidden; background: #050706; }
.stage { position: absolute; inset: var(--header-h) 0 0 0; }
.stage-empty { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14px; color: var(--text-3); letter-spacing: .2em; }
.eyebrow { font-size: 11px; letter-spacing: .4em; color: var(--gold); }
.switcher { position: absolute; z-index: 10; top: calc(var(--header-h) + 18px); left: 18px; bottom: 18px; width: 300px; display: flex; flex-direction: column;
  background: rgba(9, 12, 10, .72); backdrop-filter: blur(18px) saturate(1.2); border: 1px solid var(--line); border-radius: 18px; box-shadow: var(--shadow-1); transition: width .4s var(--ease); overflow: hidden; bottom: auto; max-height: calc(100% - var(--header-h) - 36px); }
.switcher.collapsed { width: 64px; }
.stage :deep(.sv-badges) { left: 50%; transform: translateX(-50%); top: 18px; }
.switcher.collapsed .sw-head > div, .switcher.collapsed .sw-list { display: none; }
.sw-head { display: flex; align-items: center; justify-content: space-between; padding: 18px 16px 14px 20px; border-bottom: 1px solid var(--line); gap: 8px; }
.sw-head h2 { font-size: 20px; letter-spacing: .14em; color: var(--gold-light); }
.icon-btn { width: 32px; height: 32px; flex: none; display: grid; place-items: center; border-radius: 10px; border: 1px solid var(--line); background: rgba(201,164,92,.06); color: var(--gold-light); cursor: pointer; }
.sw-list { flex: 1; overflow: auto; padding: 10px; display: flex; flex-direction: column; gap: 6px; }
.sw-item { display: flex; gap: 12px; align-items: center; padding: 8px; border-radius: 12px; border: 1px solid transparent; background: none; cursor: pointer; text-align: left; color: var(--text-1); transition: all .25s; }
.sw-item:hover { background: rgba(201,164,92,.06); }
.sw-item.on { background: linear-gradient(90deg, rgba(201,164,92,.16), rgba(201,164,92,.03)); border-color: var(--line-strong); }
.sw-thumb { width: 52px; flex: none; border-radius: 10px; overflow: hidden; border: 1px solid var(--line); }
.sw-txt { min-width: 0; display: flex; flex-direction: column; }
.sw-txt b { font-family: var(--font-serif); font-size: 15px; letter-spacing: .06em; }
.sw-item.on .sw-txt b { color: var(--gold-light); }
.sw-txt span { font-size: 11.5px; color: var(--text-3); }
.info-card { position: absolute; z-index: 10; top: calc(var(--header-h) + 18px); right: 18px; width: 320px; padding: 22px; background: rgba(9, 12, 10, .72); backdrop-filter: blur(18px); border: 1px solid var(--line); border-radius: 18px; box-shadow: var(--shadow-1); }
.info-card.hidden { width: auto; padding: 6px; }
.info-card.hidden .close { position: static; }
.close { position: absolute; top: 12px; right: 12px; width: 28px; height: 28px; border-radius: 8px; border: 1px solid var(--line); background: transparent; color: var(--text-3); cursor: pointer; display: grid; place-items: center; }
.info-card h3 { font-size: 24px; letter-spacing: .12em; margin: 6px 0 10px; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
.info-card p { color: var(--text-2); font-size: 13px; line-height: 1.8; }
.m-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin: 16px 0; }
.m-grid div { padding: 8px 10px; border-radius: 10px; background: rgba(201,164,92,.05); border: 1px solid var(--line); }
.m-grid label { display: block; font-size: 11px; color: var(--text-3); }
.m-grid b { font-family: var(--font-serif); color: var(--gold-light); font-size: 17px; }
.ic-actions { display: flex; gap: 10px; }
@media (max-width: 800px) { .switcher { width: calc(100% - 36px); bottom: auto; max-height: 50vh; } .info-card { top: auto; bottom: 18px; } }
</style>
