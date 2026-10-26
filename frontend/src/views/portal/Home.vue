<template>
  <div class="home">
    <!-- ============ HERO ============ -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-vignette"></div>
      <div class="dust"><i v-for="i in 28" :key="i" :style="dustStyle(i)"></i></div>
      <div class="hero-inner">
        <div class="hero-eyebrow"><span></span>古蜀文明 · 三星堆遗址 · 数字孪生</div>
        <h1 class="hero-title">
          <span class="line"><em v-for="(c, i) in '沉睡三千年'" :key="'a' + i" :style="{ animationDelay: 0.25 + i * 0.12 + 's' }">{{ c }}</em></span>
          <span class="line l2"><em v-for="(c, i) in '一醒惊天下'" :key="'b' + i" :style="{ animationDelay: 0.95 + i * 0.12 + 's' }">{{ c }}</em></span>
        </h1>
        <p class="hero-sub">以 <b>3D 高斯泼溅</b> 重建青铜神韵，以 <b>知识图谱 + GraphRAG</b> 讲述古蜀故事 ——<br />在指尖与千年文明对话。</p>
        <div class="hero-cta">
          <router-link to="/hall3d" class="btn-gold"><el-icon><VideoPlay /></el-icon>进入 3D 数字展厅</router-link>
          <router-link to="/guide" class="btn-ghost"><el-icon><ChatLineRound /></el-icon>问问 AI 导览员</router-link>
        </div>
        <div class="hero-stats">
          <div v-for="s in stats" :key="s.label" class="stat">
            <div class="v serif">{{ s.value }}<small>{{ s.unit }}</small></div>
            <div class="l">{{ s.label }}</div>
          </div>
        </div>
      </div>
      <a href="#treasures" class="scroll-hint"><span>向下探索</span><i></i></a>
    </section>

    <!-- ============ 镇馆之宝 ============ -->
    <section id="treasures" class="sec">
      <div class="container">
        <SectionHeading eyebrow="Treasures" title="镇馆之宝" sub="青铜铸神，黄金为礼 —— 每一件都是古蜀文明的精神图腾" />
        <div class="treasure-grid">
          <router-link v-for="(a, i) in treasures" :key="a.id" :to="`/artifacts/${a.id}`" class="tcard" :class="'t' + i">
            <BronzeImage :src="a.coverUrl" :alt="a.name" ratio="auto" class="timg" />
            <div class="tshade"></div>
            <div class="tinfo">
              <div class="tno">{{ a.artifactNo }} · {{ a.pitName }}</div>
              <h3>{{ a.name }}</h3>
              <p class="clamp-2">{{ a.description }}</p>
              <span class="tgo">查看详情 <el-icon><Right /></el-icon></span>
            </div>
          </router-link>
          <template v-if="!treasures.length">
            <div v-for="i in 6" :key="i" class="tcard skeleton" :class="'t' + (i - 1)"></div>
          </template>
        </div>
        <div class="center-btn"><router-link to="/artifacts" class="btn-ghost">浏览全部文物 <el-icon><Right /></el-icon></router-link></div>
      </div>
    </section>

    <!-- ============ 核心能力 ============ -->
    <section class="sec sec-alt">
      <div class="container">
        <SectionHeading eyebrow="Technology" title="三重技术 · 重现古蜀" sub="数字孪生、知识图谱与大模型融合的智慧导览" />
        <div class="feat-grid">
          <router-link v-for="f in features" :key="f.title" :to="f.to" class="feat panel corner-frame">
            <div class="feat-icon"><el-icon :size="30"><component :is="f.icon" /></el-icon></div>
            <div class="feat-en">{{ f.en }}</div>
            <h3>{{ f.title }}</h3>
            <p>{{ f.desc }}</p>
            <ul><li v-for="p in f.points" :key="p">{{ p }}</li></ul>
            <span class="feat-go">{{ f.cta }} <el-icon><Right /></el-icon></span>
          </router-link>
        </div>
      </div>
    </section>

    <!-- ============ 图谱预览 ============ -->
    <section class="sec">
      <div class="container kg-band">
        <div class="kg-text">
          <SectionHeading eyebrow="Knowledge Graph" title="万物关联的古蜀图谱" align="left" />
          <p>将文物、祭祀坑、材质、年代、文化、主题、人物与文献抽取为 <b>{{ overview?.entityCount ?? '—' }}</b> 个实体与 <b>{{ overview?.relationCount ?? '—' }}</b> 条关系，
            构建三星堆领域知识图谱。智能导览在回答问题时沿图谱多跳检索，让每一句讲解都有据可查。</p>
          <div class="kg-tags">
            <span v-for="t in kgTypes" :key="t.name" class="chip">{{ t.name }} · {{ t.value }}</span>
          </div>
          <router-link to="/graph" class="btn-gold" style="margin-top: 26px">探索知识图谱 <el-icon><Right /></el-icon></router-link>
        </div>
        <div class="kg-viz panel">
          <KgGraph :graph="graph" height="460px" :mini="true" :repulsion="320" @node-click="(n) => $router.push({ path: '/graph', query: { focus: n.id } })" />
        </div>
      </div>
    </section>

    <!-- ============ 路线 + 公告 ============ -->
    <section class="sec sec-alt">
      <div class="container">
        <SectionHeading eyebrow="Guided Tours" title="精选导览路线" sub="按兴趣与时间，为你规划一条与古蜀相遇的路" />
        <div class="route-row">
          <router-link v-for="r in routes.slice(0, 4)" :key="r.id" :to="`/routes/${r.id}`" class="rcard">
            <BronzeImage :src="r.coverUrl" :alt="r.name" ratio="16 / 10" />
            <div class="rbody">
              <span class="chip">{{ ROUTE_TYPE[r.routeType] || r.routeType }}</span>
              <h4>{{ r.name }}</h4>
              <div class="rmeta"><span><el-icon><Timer /></el-icon>{{ r.durationMin }} 分钟</span><span><el-icon><Location /></el-icon>{{ r.distanceM }} 米</span></div>
            </div>
          </router-link>
        </div>
        <div class="news">
          <div class="news-col panel panel-pad">
            <h3 class="news-h"><el-icon><Bell /></el-icon>馆务公告</h3>
            <div v-for="n in announcements.slice(0, 4)" :key="n.id" class="news-item" @click="openNews(n)">
              <span v-if="n.isTop" class="chip red">置顶</span>
              <span class="ellipsis t">{{ n.title }}</span>
              <span class="d">{{ (n.publishTime || '').slice(5, 10) }}</span>
            </div>
            <el-empty v-if="!announcements.length" :image-size="60" description="暂无公告" />
          </div>
          <div class="news-col panel panel-pad">
            <h3 class="news-h"><el-icon><Calendar /></el-icon>展览活动</h3>
            <div v-for="e in events.slice(0, 4)" :key="e.id" class="news-item">
              <span class="chip jade">{{ EVENT_TYPE[e.eventType] || e.eventType }}</span>
              <span class="ellipsis t">{{ e.title }}</span>
              <span class="d">{{ (e.startTime || '').slice(5, 10) }}</span>
            </div>
            <el-empty v-if="!events.length" :image-size="60" description="暂无活动" />
          </div>
        </div>
      </div>
    </section>

    <!-- ============ CTA ============ -->
    <section class="cta">
      <div class="container cta-inner">
        <div>
          <h2 class="serif">亲临三星堆，赴一场三千年之约</h2>
          <p>在线预约入馆时段，选择导览路线与人工讲解，扫码核销即可入馆。</p>
        </div>
        <router-link to="/reservation" class="btn-gold"><el-icon><Tickets /></el-icon>立即预约参观</router-link>
      </div>
    </section>

    <el-dialog v-model="newsVisible" :title="newsItem?.title" width="560px">
      <div class="md" style="white-space: pre-wrap">{{ newsItem?.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import SectionHeading from '@/components/SectionHeading.vue'
import BronzeImage from '@/components/BronzeImage.vue'
import KgGraph from '@/components/KgGraph.vue'
import { artifactApi, dashboardApi, dictApi, kgApi, routeApi } from '@/api'
import type { Announcement, Artifact, ExhibitionEvent, GraphVO, NameValue, Overview, Route } from '@/api/types'
import { fmtNum, ROUTE_TYPE } from '@/utils/format'

const EVENT_TYPE: Record<string, string> = { EXHIBITION: '展览', LECTURE: '讲座', WORKSHOP: '研学', ONLINE: '线上' }
const treasures = ref<Artifact[]>([])
const overview = ref<Overview>()
const graph = ref<GraphVO | null>(null)
const kgTypes = ref<NameValue[]>([])
const routes = ref<Route[]>([])
const announcements = ref<Announcement[]>([])
const events = ref<ExhibitionEvent[]>([])
const newsVisible = ref(false)
const newsItem = ref<Announcement>()

const stats = computed(() => [
  { label: '馆藏精品文物', value: overview.value?.artifactCount ?? '—', unit: '件' },
  { label: '3DGS 数字模型', value: overview.value?.modelCount ?? '—', unit: '个' },
  { label: '高斯基元总量', value: overview.value ? fmtNum(overview.value.gaussianTotal).replace(/[万亿]/, '') : '—', unit: overview.value && overview.value.gaussianTotal >= 1e8 ? '亿' : '万' },
  { label: '图谱实体 / 关系', value: overview.value ? `${overview.value.entityCount}/${overview.value.relationCount}` : '—', unit: '' }
])

const features = [
  { icon: 'Box', en: '3D Gaussian Splatting', title: '高斯泼溅数字孪生', to: '/hall3d', cta: '进入 3D 展厅',
    desc: '多视角影像经 COLMAP 位姿估计与 3DGS 训练，以百万级高斯基元实时还原青铜纹饰与金箔光泽。',
    points: ['浏览器端实时渲染', 'PSNR / SSIM 质量指标', '展厅—展柜—文物空间层级'] },
  { icon: 'Share', en: 'Knowledge Graph', title: '三星堆领域知识图谱', to: '/graph', cta: '探索图谱',
    desc: '以“文物—祭祀坑—材质—年代—文化—主题”为骨架，支持 n 跳邻居、最短路径与实体检索。',
    points: ['力导向交互可视化', '最短关联路径', '实体详情联动文物'] },
  { icon: 'MagicStick', en: 'GraphRAG · LLM', title: 'GraphRAG 智能导览', to: '/guide', cta: '开始对话',
    desc: '实体链接 + 子图检索 + 文献片段作为上下文，驱动大模型生成可溯源的讲解，流式输出。',
    points: ['SSE 流式打字效果', '引用实体与三元组可追溯', '离线兜底保证可用'] }
]

function dustStyle(i: number) {
  const r = (n: number) => ((Math.sin(i * 9301 + n * 49297) + 1) / 2)
  return {
    left: `${r(1) * 100}%`, top: `${r(2) * 100}%`,
    width: `${1 + r(3) * 2.5}px`, height: `${1 + r(3) * 2.5}px`,
    animationDuration: `${8 + r(4) * 14}s`, animationDelay: `${-r(5) * 10}s`, opacity: 0.3 + r(6) * 0.6
  }
}
function openNews(n: Announcement) { newsItem.value = n; newsVisible.value = true }

onMounted(async () => {
  artifactApi.search({ isTreasure: 1, size: 6, orderBy: 'hot' }).then((p) => (treasures.value = p.records)).catch(() => undefined)
  dashboardApi.overview().then((o) => (overview.value = o)).catch(() => undefined)
  kgApi.graph(undefined, 60).then((g) => (graph.value = g)).catch(() => undefined)
  kgApi.statistics().then((s) => (kgTypes.value = s.entityTypes.slice(0, 8))).catch(() => undefined)
  routeApi.list().then((r) => (routes.value = r)).catch(() => undefined)
  dictApi.announcements().then((a) => (announcements.value = a.sort((x, y) => (y.isTop || 0) - (x.isTop || 0)))).catch(() => undefined)
  dictApi.events().then((e) => (events.value = e)).catch(() => undefined)
})
</script>

<style scoped>
/* HERO */
.hero { position: relative; min-height: 100vh; display: flex; align-items: center; overflow: hidden; background: #050605; }
.hero-bg { position: absolute; inset: 0; background: url('/static/images/hero.jpg') center right / cover no-repeat; transform-origin: 70% 40%; animation: heroZoom 24s ease-in-out infinite alternate; }
@keyframes heroZoom { from { transform: scale(1.02); } to { transform: scale(1.1); } }
.hero-vignette { position: absolute; inset: 0; background:
  linear-gradient(90deg, rgba(5,6,5,.96) 0%, rgba(5,6,5,.82) 32%, rgba(5,6,5,.25) 62%, rgba(5,6,5,.2) 100%),
  linear-gradient(180deg, rgba(5,6,5,.5) 0%, transparent 25%, transparent 70%, var(--bg-1) 100%); }
.dust { position: absolute; inset: 0; pointer-events: none; }
.dust i { position: absolute; border-radius: 50%; background: #f3e3bb; box-shadow: 0 0 6px #e8cb8a; animation: float linear infinite; }
@keyframes float { 0% { transform: translate(0, 0); } 50% { transform: translate(20px, -40px); } 100% { transform: translate(0, -80px); opacity: 0; } }
.hero-inner { position: relative; z-index: 2; width: min(1280px, calc(100% - 48px)); margin: 0 auto; padding-top: var(--header-h); }
.hero-eyebrow { display: flex; align-items: center; gap: 14px; color: var(--gold); letter-spacing: .4em; font-size: 13px; animation: fadeUp 1s var(--ease) both; }
.hero-eyebrow span { width: 48px; height: 1px; background: linear-gradient(90deg, transparent, var(--gold)); }
.hero-title { margin: 22px 0 18px; font-weight: 900; font-size: clamp(48px, 7.4vw, 104px); line-height: 1.12; letter-spacing: .14em; }
.hero-title .line { display: block; }
.hero-title .l2 { padding-left: 1.1em; }
.hero-title em { font-style: normal; display: inline-block; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent;
  filter: drop-shadow(0 4px 30px rgba(201,164,92,.25)); animation: charIn 1.1s var(--ease) both; }
@keyframes charIn { from { opacity: 0; transform: translateY(40px) scale(.9); filter: blur(10px); } to { opacity: 1; transform: none; filter: drop-shadow(0 4px 30px rgba(201,164,92,.25)); } }
.hero-sub { color: var(--text-2); font-size: clamp(15px, 1.3vw, 18px); line-height: 1.9; letter-spacing: .06em; animation: fadeUp 1s 1.6s var(--ease) both; }
.hero-sub b { color: var(--gold-light); font-weight: 600; }
.hero-cta { display: flex; gap: 16px; margin-top: 34px; flex-wrap: wrap; animation: fadeUp 1s 1.8s var(--ease) both; }
.hero-stats { display: flex; gap: 0; margin-top: 64px; animation: fadeUp 1s 2s var(--ease) both; flex-wrap: wrap; }
.stat { padding: 0 34px; border-left: 1px solid var(--line-strong); }
.stat:first-child { padding-left: 0; border-left: 0; }
.stat .v { font-size: 34px; font-weight: 900; color: var(--gold-light); letter-spacing: .04em; line-height: 1.2; }
.stat .v small { font-size: 14px; margin-left: 4px; color: var(--gold); font-weight: 600; }
.stat .l { color: var(--text-3); font-size: 13px; letter-spacing: .15em; margin-top: 4px; }
.scroll-hint { position: absolute; bottom: 28px; left: 50%; transform: translateX(-50%); z-index: 2; display: flex; flex-direction: column; align-items: center; gap: 8px; color: var(--text-3); font-size: 12px; letter-spacing: .3em; }
.scroll-hint i { width: 1px; height: 44px; background: linear-gradient(var(--gold), transparent); animation: drop 2s ease-in-out infinite; transform-origin: top; }
@keyframes drop { 0% { transform: scaleY(0); } 50% { transform: scaleY(1); } 100% { transform: scaleY(1); opacity: 0; } }

.sec { padding: 110px 0; position: relative; }
.sec-alt { background: linear-gradient(180deg, transparent, rgba(20, 32, 25, .35) 30%, rgba(20, 32, 25, .35) 70%, transparent); }

/* Treasures */
.treasure-grid { display: grid; grid-template-columns: repeat(4, 1fr); grid-auto-rows: 250px; gap: 18px; }
.tcard { position: relative; border-radius: var(--radius); overflow: hidden; border: 1px solid var(--line); background: #0d110f; color: var(--text-1); }
.tcard.t0 { grid-column: span 2; grid-row: span 2; }
.tcard.t1 { grid-row: span 2; }
.tcard.t4, .tcard.t5 { grid-column: span 2; }
.timg { position: absolute !important; inset: 0; height: 100%; }
.timg :deep(img) { transition: transform 1.2s var(--ease), opacity .6s; }
.tcard:hover .timg :deep(img) { transform: scale(1.07); }
.tshade { position: absolute; inset: 0; background: linear-gradient(180deg, transparent 35%, rgba(6,8,7,.92)); transition: background .5s; }
.tinfo { position: absolute; left: 0; right: 0; bottom: 0; padding: 22px; transform: translateY(42px); transition: transform .6s var(--ease); }
.tcard:hover .tinfo { transform: none; }
.tno { font-size: 11px; color: var(--gold); letter-spacing: .2em; }
.tinfo h3 { font-size: 22px; margin: 4px 0 6px; letter-spacing: .12em; }
.t0 .tinfo h3 { font-size: 32px; }
.tinfo p { color: var(--text-2); font-size: 13px; opacity: 0; transition: opacity .5s .1s; }
.tcard:hover .tinfo p { opacity: 1; }
.tgo { display: inline-flex; align-items: center; gap: 4px; color: var(--gold-light); font-size: 13px; margin-top: 10px; letter-spacing: .15em; }
.tcard.skeleton { background: linear-gradient(100deg, #101511 30%, #18201a 50%, #101511 70%); background-size: 200% 100%; animation: sk 1.4s infinite; }
@keyframes sk { to { background-position: -200% 0; } }
.center-btn { text-align: center; margin-top: 40px; }

/* Features */
.feat-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 22px; }
.feat { padding: 34px 30px 28px; color: var(--text-1); display: flex; flex-direction: column; transition: transform .45s var(--ease), border-color .4s, box-shadow .4s; }
.feat:hover { transform: translateY(-6px); border-color: var(--line-strong); box-shadow: var(--shadow-gold); color: var(--text-1); }
.feat-icon { width: 62px; height: 62px; border-radius: 16px; display: grid; place-items: center; color: var(--gold-light); background: radial-gradient(circle at 30% 30%, rgba(201,164,92,.28), rgba(201,164,92,.05)); border: 1px solid var(--line-strong); }
.feat-en { margin-top: 22px; font-size: 11px; letter-spacing: .3em; color: var(--gold-deep); text-transform: uppercase; }
.feat h3 { font-size: 23px; margin: 6px 0 12px; letter-spacing: .1em; }
.feat p { color: var(--text-2); font-size: 14px; line-height: 1.85; }
.feat ul { list-style: none; padding: 0; margin: 16px 0 22px; display: flex; flex-direction: column; gap: 8px; }
.feat li { color: var(--text-3); font-size: 13px; padding-left: 16px; position: relative; }
.feat li::before { content: ''; position: absolute; left: 0; top: 9px; width: 6px; height: 6px; transform: rotate(45deg); background: var(--gold-deep); }
.feat-go { margin-top: auto; color: var(--gold); display: inline-flex; align-items: center; gap: 6px; letter-spacing: .15em; font-size: 14px; }

/* KG band */
.kg-band { display: grid; grid-template-columns: 5fr 7fr; gap: 48px; align-items: center; }
.kg-text p { color: var(--text-2); font-size: 15px; line-height: 2; }
.kg-text b { color: var(--gold-light); font-size: 20px; font-family: var(--font-serif); }
.kg-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 20px; }
.kg-viz { padding: 6px; background: radial-gradient(circle at 50% 50%, rgba(36,57,45,.4), rgba(11,14,12,.9) 70%); }

/* Routes */
.route-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }
.rcard { border-radius: var(--radius); overflow: hidden; border: 1px solid var(--line); background: #0f1411; color: var(--text-1); transition: transform .4s var(--ease), border-color .4s; }
.rcard:hover { transform: translateY(-5px); border-color: var(--line-strong); color: var(--text-1); }
.rbody { padding: 14px 16px 16px; }
.rbody h4 { font-size: 17px; margin: 10px 0 8px; letter-spacing: .08em; }
.rmeta { display: flex; gap: 16px; color: var(--text-3); font-size: 12.5px; }
.rmeta span { display: inline-flex; align-items: center; gap: 4px; }
.news { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-top: 26px; }
.news-h { display: flex; align-items: center; gap: 8px; font-size: 18px; color: var(--gold-light); margin-bottom: 10px; letter-spacing: .12em; }
.news-item { display: flex; align-items: center; gap: 10px; padding: 11px 0; border-bottom: 1px dashed rgba(201,164,92,.12); cursor: pointer; }
.news-item:last-child { border-bottom: 0; }
.news-item .t { flex: 1; color: var(--text-1); transition: color .2s; }
.news-item:hover .t { color: var(--gold-light); }
.news-item .d { color: var(--text-4); font-size: 12px; font-family: ui-monospace, monospace; }

/* CTA */
.cta { padding: 70px 0; background: linear-gradient(100deg, rgba(36,57,45,.5), rgba(201,164,92,.12)), url('/static/images/artifacts/shenshu_detail.jpg') center / cover; border-top: 1px solid var(--line); border-bottom: 1px solid var(--line); position: relative; }
.cta::before { content: ''; position: absolute; inset: 0; background: rgba(6,8,7,.72); }
.cta-inner { position: relative; display: flex; align-items: center; justify-content: space-between; gap: 24px; flex-wrap: wrap; }
.cta h2 { font-size: clamp(22px, 2.6vw, 32px); letter-spacing: .14em; color: var(--gold-pale); }
.cta p { color: var(--text-2); margin-top: 8px; }

@media (max-width: 1100px) {
  .treasure-grid { grid-template-columns: repeat(2, 1fr); grid-auto-rows: 230px; }
  .tcard.t0 { grid-column: span 2; } .tcard.t1 { grid-row: span 1; } .tcard.t4 { grid-column: span 1; }
  .feat-grid { grid-template-columns: 1fr; }
  .kg-band { grid-template-columns: 1fr; }
  .route-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .treasure-grid { grid-template-columns: 1fr; } .tcard.t0, .tcard.t4, .tcard.t5 { grid-column: span 1; grid-row: span 1; }
  .route-row, .news { grid-template-columns: 1fr; }
  .hero-stats { display: grid; grid-template-columns: 1fr 1fr; gap: 18px 0; margin-top: 40px; }
  .stat, .stat:first-child { padding: 0 14px; border-left: 1px solid var(--line-strong); } .stat .v { font-size: 24px; }
  .scroll-hint { display: none; }
  .hero-eyebrow { letter-spacing: .2em; font-size: 12px; } .hero-eyebrow span { width: 24px; }
  .hero-title .l2 { padding-left: .5em; }
  .sec { padding: 70px 0; }
}
</style>
