<template>
  <div class="detail page" v-loading="loading">
    <div class="container" v-if="d">
      <el-breadcrumb separator="/" class="crumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/artifacts' }">文物展厅</el-breadcrumb-item>
        <el-breadcrumb-item>{{ a.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="top">
        <div class="gallery">
          <div class="main-img panel corner-frame">
            <BronzeImage :src="curImg" :alt="a.name" ratio="4 / 3" />
            <span class="img-type chip">{{ IMG_TYPE[curType] || '实拍' }}</span>
          </div>
          <div class="thumbs">
            <button v-for="(im, i) in images" :key="im.id" class="thumb" :class="{ on: curIdx === i }" @click="curIdx = i">
              <BronzeImage :src="im.imageUrl" :alt="im.title" ratio="4 / 3" />
            </button>
          </div>
        </div>

        <div class="info">
          <div class="no">{{ a.artifactNo }}<span v-if="a.isTreasure" class="chip treasure"><el-icon><Star /></el-icon>镇馆之宝</span></div>
          <h1 class="name">{{ a.name }}</h1>
          <div v-if="a.alias" class="alias">又称：{{ a.alias.split(',').join(' · ') }}</div>
          <div class="chips">
            <span class="chip" v-if="a.categoryName">{{ a.categoryName }}</span>
            <span class="chip" v-if="a.materialName">{{ a.materialName }}</span>
            <span class="chip" v-if="a.grade">{{ a.grade }}文物</span>
            <span class="chip jade">{{ ARTIFACT_STATUS[a.status ?? 1] }}</span>
          </div>
          <div class="facts">
            <div><label>出土地点</label><b>{{ a.pitName || '—' }}</b></div>
            <div><label>年代</label><b>{{ a.periodName || '—' }}</b></div>
            <div><label>尺寸</label><b>{{ a.dimensions || '—' }}</b></div>
            <div><label>出土年份</label><b>{{ a.excavationYear || '—' }}</b></div>
            <div><label>陈列展厅</label><b>{{ a.hallName || '—' }}</b></div>
            <div><label>观众评分</label><b><el-rate :model-value="d.avgRating || 0" disabled size="small" /> <small>{{ d.avgRating || '—' }} ({{ d.reviewCount }})</small></b></div>
          </div>
          <p class="desc clamp-3">{{ a.description }}</p>
          <div class="actions">
            <button class="btn-gold" @click="tab = 'model'; scrollTabs()"><el-icon><Box /></el-icon>3D 观赏</button>
            <button class="btn-ghost" :class="{ faved: d.favorited }" @click="toggleFav">
              <el-icon><component :is="d.favorited ? 'StarFilled' : 'Star'" /></el-icon>{{ d.favorited ? '已收藏' : '收藏' }} · {{ a.favoriteCount ?? 0 }}
            </button>
            <router-link :to="{ path: '/guide', query: { q: `介绍一下${a.name}` } }" class="btn-ghost"><el-icon><ChatDotRound /></el-icon>问导览</router-link>
          </div>
          <div class="views"><el-icon><View /></el-icon>{{ fmtNum(a.viewCount) }} 次浏览</div>
        </div>
      </section>

      <section ref="tabsEl" class="tabs-wrap panel">
        <el-tabs v-model="tab">
          <el-tab-pane label="文物解读" name="intro">
            <div class="intro">
              <div class="block">
                <h3><i></i>器物描述</h3>
                <p>{{ a.description }}</p>
              </div>
              <div class="block">
                <h3><i></i>文化价值</h3>
                <p>{{ a.culturalValue || '—' }}</p>
              </div>
              <div class="block guide">
                <h3><i></i>语音导览讲解词
                  <button class="speak" @click="speak"><el-icon><component :is="speaking ? 'VideoPause' : 'Headset'" /></el-icon>{{ speaking ? '停止' : '播放讲解' }}</button>
                </h3>
                <blockquote>{{ a.guideText || '—' }}</blockquote>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="3D 模型" name="model">
            <div class="model-pane" v-if="tab === 'model'">
              <div class="viewer-box">
                <SplatViewer :url="curModel?.modelUrl" :camera-position="parseVec(curModel?.cameraPosition)" :camera-target="parseVec(curModel?.cameraTarget)" :format="curModel?.format" />
              </div>
              <aside class="model-meta">
                <template v-if="d.models.length">
                  <el-select v-if="d.models.length > 1" v-model="modelIdx" style="width: 100%; margin-bottom: 14px">
                    <el-option v-for="(m, i) in d.models" :key="m.id" :label="`${m.name}（${m.format}）`" :value="i" />
                  </el-select>
                  <h4>{{ curModel?.name }}</h4>
                  <div class="metric-grid">
                    <div><label>高斯基元</label><b>{{ fmtNum(curModel?.gaussianCount) }}</b></div>
                    <div><label>PSNR</label><b>{{ curModel?.psnr ?? '—' }}<small> dB</small></b></div>
                    <div><label>SSIM</label><b>{{ curModel?.ssim ?? '—' }}</b></div>
                    <div><label>LPIPS</label><b>{{ curModel?.lpips ?? '—' }}</b></div>
                    <div><label>训练迭代</label><b>{{ fmtNum(curModel?.trainIterations) }}</b></div>
                    <div><label>文件大小</label><b>{{ fmtBytes(curModel?.fileSize) }}</b></div>
                  </div>
                  <dl>
                    <dt>采集设备</dt><dd>{{ curModel?.captureDevice || '—' }}</dd>
                    <dt>训练视图</dt><dd>{{ curModel?.imageCount ?? '—' }} 张</dd>
                    <dt>位姿估计</dt><dd>{{ curModel?.sfmTool || '—' }}</dd>
                    <dt>训练框架</dt><dd>{{ curModel?.trainedBy || '—' }}</dd>
                    <dt>球谐阶数</dt><dd>SH {{ curModel?.shDegree ?? '—' }}</dd>
                    <dt>模型地址</dt><dd class="mono">{{ curModel?.modelUrl }}</dd>
                  </dl>
                </template>
                <el-empty v-else description="该文物暂无 3DGS 模型，展示示例场景" :image-size="70" />
              </aside>
            </div>
          </el-tab-pane>

          <el-tab-pane label="知识关联" name="graph">
            <div class="graph-pane" v-if="tab === 'graph'">
              <KgGraph :graph="d.graph" height="480px" :center-id="centerId" :repulsion="620" @node-click="onNode" />
              <div class="triples">
                <h4>一跳关联 · {{ d.graph?.links?.length || 0 }} 条</h4>
                <div v-for="l in d.graph?.links || []" :key="l.id || l.source + l.target" class="triple">
                  <span>{{ nodeName(l.source) }}</span><em>{{ l.name }}</em><span>{{ nodeName(l.target) }}</span>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`文献与修复 (${d.literature.length + d.restorations.length})`" name="lit">
            <div class="lit-pane">
              <div>
                <h3 class="sub-h">相关文献</h3>
                <div v-for="l in d.literature" :key="l.id" class="lit">
                  <span class="chip">{{ LIT_TYPE[l.litType || ''] || l.litType }}</span>
                  <div>
                    <b>{{ l.title }}</b>
                    <div class="muted">{{ [l.authors, l.source, l.publishYear].filter(Boolean).join(' · ') }}</div>
                    <p v-if="l.abstractText" class="clamp-2">{{ l.abstractText }}</p>
                  </div>
                </div>
                <el-empty v-if="!d.literature.length" :image-size="60" description="暂无关联文献" />
              </div>
              <div>
                <h3 class="sub-h">修复记录</h3>
                <el-timeline v-if="d.restorations.length">
                  <el-timeline-item v-for="r in d.restorations" :key="r.id" :timestamp="`${r.startDate || ''} ~ ${r.endDate || '至今'}`" color="#c9a45c">
                    <b>{{ r.restorer }}</b><span class="chip jade" style="margin-left: 8px">{{ RESTORE_STATUS[r.status] || r.status }}</span>
                    <p class="muted">病害：{{ r.conditionBefore }}</p>
                    <p>方法：{{ r.method }}</p>
                  </el-timeline-item>
                </el-timeline>
                <el-empty v-else :image-size="60" description="暂无修复记录" />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`观众评价 (${d.reviewCount})`" name="review">
            <ReviewPanel target-type="ARTIFACT" :target-id="a.id" @changed="reloadQuiet" />
          </el-tab-pane>
        </el-tabs>
      </section>

      <section v-if="d.relatedArtifacts?.length" class="related">
        <SectionHeading eyebrow="Related" title="相关文物" align="left" />
        <div class="rel-grid"><ArtifactCard v-for="r in d.relatedArtifacts.slice(0, 4)" :key="r.id" :a="r" /></div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BronzeImage from '@/components/BronzeImage.vue'
import SplatViewer from '@/components/SplatViewer.vue'
import KgGraph from '@/components/KgGraph.vue'
import ArtifactCard from '@/components/ArtifactCard.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import ReviewPanel from '@/components/ReviewPanel.vue'
import { artifactApi, favoriteApi } from '@/api'
import type { ArtifactDetail, GraphNode } from '@/api/types'
import { ARTIFACT_STATUS, fmtBytes, fmtNum, parseVec } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'

const IMG_TYPE: Record<string, string> = { PHOTO: '实拍', DETAIL: '细节', LINE: '线图', TRAIN: '3DGS 训练视图' }
const LIT_TYPE: Record<string, string> = { BOOK: '专著', PAPER: '论文', ANCIENT: '古籍', REPORT: '考古报告', GUIDE: '导览文稿' }
const RESTORE_STATUS: Record<string, string> = { PLANNED: '计划', ONGOING: '进行中', DONE: '已完成' }

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const d = ref<ArtifactDetail>()
const loading = ref(true)
const curIdx = ref(0)
const modelIdx = ref(0)
const tab = ref('intro')
const tabsEl = ref<HTMLElement>()
const speaking = ref(false)

const a = computed(() => d.value!.artifact)
const images = computed(() => (d.value?.images?.length ? d.value.images : d.value ? [{ id: 0, artifactId: a.value.id, imageUrl: a.value.coverUrl || '', imageType: 'PHOTO' }] : []))
const curImg = computed(() => images.value[curIdx.value]?.imageUrl)
const curType = computed(() => images.value[curIdx.value]?.imageType || 'PHOTO')
const curModel = computed(() => d.value?.models?.[modelIdx.value])
const centerId = computed(() => d.value?.graph?.nodes.find((n) => n.refTable === 'artifact' && n.refId === a.value.id)?.id)

function nodeName(id: string) { return d.value?.graph?.nodes.find((n) => n.id === id)?.name || id }
function onNode(n: GraphNode) {
  if (n.refTable === 'artifact' && n.refId && n.refId !== a.value.id) router.push(`/artifacts/${n.refId}`)
  else router.push({ path: '/graph', query: { focus: n.id } })
}
function scrollTabs() { setTimeout(() => tabsEl.value?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 50) }

async function load() {
  loading.value = true
  try {
    d.value = await artifactApi.detail(route.params.id as string)
    if (route.query.tab) tab.value = String(route.query.tab)
  } finally {
    loading.value = false
  }
}
async function reloadQuiet() { d.value = await artifactApi.detail(route.params.id as string) }
async function toggleFav() {
  if (!auth.isLogin) { router.push({ path: '/login', query: { redirect: route.fullPath } }); return }
  const r = await favoriteApi.toggle('ARTIFACT', a.value.id)
  d.value!.favorited = r.favorited
  a.value.favoriteCount = (a.value.favoriteCount || 0) + (r.favorited ? 1 : -1)
  ElMessage.success(r.favorited ? '已加入收藏' : '已取消收藏')
}
function speak() {
  if (!('speechSynthesis' in window)) { ElMessage.info('当前浏览器不支持语音播报'); return }
  if (speaking.value) { speechSynthesis.cancel(); speaking.value = false; return }
  const u = new SpeechSynthesisUtterance(a.value.guideText || a.value.description || '')
  u.lang = 'zh-CN'; u.rate = 0.95
  u.onend = () => (speaking.value = false)
  speaking.value = true
  speechSynthesis.speak(u)
}
onMounted(load)
onBeforeUnmount(() => { if ('speechSynthesis' in window) speechSynthesis.cancel() })
</script>

<style scoped>
.crumb { margin-bottom: 22px; }
.crumb :deep(.el-breadcrumb__inner) { color: var(--text-3); }
.crumb :deep(.el-breadcrumb__inner.is-link:hover) { color: var(--gold-light); }
.top { display: grid; grid-template-columns: 1.1fr 1fr; gap: 44px; align-items: start; }
.main-img { border-radius: var(--radius-lg); }
.img-type { position: absolute; right: 16px; bottom: 16px; background: rgba(8,10,9,.7); backdrop-filter: blur(6px); z-index: 3; }
.thumbs { display: flex; gap: 12px; margin-top: 14px; }
.thumb { width: 104px; padding: 0; border: 1px solid var(--line); border-radius: 10px; overflow: hidden; cursor: pointer; background: none; opacity: .6; transition: all .3s; }
.thumb.on, .thumb:hover { opacity: 1; border-color: var(--gold); box-shadow: 0 0 0 3px rgba(201,164,92,.15); }
.info { padding-top: 6px; }
.no { display: flex; align-items: center; gap: 12px; color: var(--gold); letter-spacing: .3em; font-size: 12px; font-family: ui-monospace, monospace; }
.treasure { letter-spacing: .1em; font-family: var(--font-sans); }
.name { font-size: clamp(34px, 4vw, 52px); font-weight: 900; letter-spacing: .14em; margin: 10px 0 6px; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; line-height: 1.25; }
.alias { color: var(--text-3); font-size: 13px; }
.chips { display: flex; gap: 8px; flex-wrap: wrap; margin: 16px 0 22px; }
.facts { display: grid; grid-template-columns: repeat(2, 1fr); border-top: 1px solid var(--line); border-left: 1px solid var(--line); border-radius: 12px; overflow: hidden; }
.facts > div { padding: 12px 16px; border-right: 1px solid var(--line); border-bottom: 1px solid var(--line); background: rgba(201,164,92,.02); }
.facts > div > label { display: block; color: var(--text-3); font-size: 12px; letter-spacing: .15em; }
.facts > div > b { color: var(--text-1); font-weight: 500; font-size: 14.5px; display: flex; align-items: center; gap: 6px; }
.facts small { color: var(--text-3); }
.desc { color: var(--text-2); margin: 20px 0 0; line-height: 1.9; }
.actions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 26px; }
.faved { color: var(--gold-pale); background: rgba(201,164,92,.18); }
.views { margin-top: 16px; color: var(--text-4); font-size: 12px; display: flex; align-items: center; gap: 6px; }
.tabs-wrap { margin-top: 56px; padding: 10px 28px 28px; scroll-margin-top: calc(var(--header-h) + 16px); }
.intro { display: grid; grid-template-columns: 1fr 1fr; gap: 28px; padding-top: 8px; }
.block h3 { font-size: 18px; color: var(--gold-light); letter-spacing: .14em; display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.block h3 > i { width: 4px; height: 18px; background: var(--grad-gold); border-radius: 2px; }
.block p { color: var(--text-2); line-height: 2; }
.block.guide { grid-column: span 2; }
blockquote { margin: 0; padding: 18px 24px; border-left: 3px solid var(--gold); background: linear-gradient(90deg, rgba(201,164,92,.08), transparent); font-family: var(--font-serif); font-size: 16px; line-height: 2.1; color: var(--text-1); border-radius: 0 12px 12px 0; }
.speak { margin-left: auto; display: inline-flex; align-items: center; gap: 6px; background: rgba(201,164,92,.08); border: 1px solid var(--line-strong); color: var(--gold-light); border-radius: 999px; padding: 4px 14px; cursor: pointer; font-size: 13px; font-family: var(--font-sans); letter-spacing: .05em; }
.model-pane { display: grid; grid-template-columns: 1fr 320px; gap: 20px; }
.viewer-box { height: 540px; border-radius: 16px; overflow: hidden; border: 1px solid var(--line); }
.model-meta h4 { font-size: 17px; color: var(--gold-light); margin-bottom: 12px; letter-spacing: .06em; }
.metric-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.metric-grid div { padding: 10px 12px; border-radius: 10px; background: rgba(201,164,92,.05); border: 1px solid var(--line); }
.metric-grid label { display: block; font-size: 11px; color: var(--text-3); letter-spacing: .12em; }
.metric-grid b { font-family: var(--font-serif); font-size: 19px; color: var(--gold-light); }
.metric-grid small { font-size: 11px; color: var(--text-3); }
dl { display: grid; grid-template-columns: 76px 1fr; gap: 8px 10px; margin: 18px 0 0; font-size: 13px; }
dt { color: var(--text-3); } dd { margin: 0; color: var(--text-2); word-break: break-all; }
.mono { font-family: ui-monospace, monospace; font-size: 12px; }
.graph-pane { display: grid; grid-template-columns: 1fr 300px; gap: 20px; }
.triples { max-height: 480px; overflow: auto; padding-right: 6px; }
.triples h4 { color: var(--gold-light); margin-bottom: 10px; font-size: 15px; }
.triple { display: flex; align-items: center; gap: 6px; padding: 8px 10px; border-radius: 8px; font-size: 13px; background: rgba(201,164,92,.03); margin-bottom: 6px; border: 1px solid rgba(201,164,92,.08); }
.triple em { font-style: normal; color: var(--gold); font-size: 12px; padding: 0 6px; border-left: 1px solid var(--line); border-right: 1px solid var(--line); white-space: nowrap; }
.triple span { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.lit-pane { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; }
.sub-h { font-size: 17px; color: var(--gold-light); margin-bottom: 14px; letter-spacing: .12em; }
.lit { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px dashed var(--line); }
.lit b { color: var(--text-1); }
.lit p { color: var(--text-3); font-size: 13px; margin-top: 4px; }
.related { margin-top: 64px; }
.rel-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
@media (max-width: 1000px) {
  .top, .model-pane, .graph-pane, .lit-pane, .intro { grid-template-columns: 1fr; }
  .block.guide { grid-column: span 1; }
  .rel-grid { grid-template-columns: repeat(2, 1fr); }
  .viewer-box { height: 400px; }
}
</style>
