<template>
  <div class="rd" v-loading="loading">
    <template v-if="d">
      <PageHeader :eyebrow="ROUTE_TYPE[d.route.routeType] || 'ROUTE'" :title="d.route.name" :sub="d.route.description" :bg="d.route.coverUrl">
        <div class="kpis">
          <div><b class="serif">{{ d.route.durationMin }}</b><span>分钟</span></div>
          <div><b class="serif">{{ d.route.distanceM }}</b><span>米</span></div>
          <div><b class="serif">{{ d.nodes.length }}</b><span>个讲解点</span></div>
          <div><b class="serif">{{ d.avgRating || '—' }}</b><span>评分</span></div>
          <button class="btn-gold" @click="start"><el-icon><VideoPlay /></el-icon>{{ started ? '重新开始' : '开始导览' }}</button>
        </div>
      </PageHeader>

      <div class="container body">
        <aside class="timeline panel">
          <div class="tl-h">导览节点</div>
          <div v-for="(n, i) in d.nodes" :key="n.id" class="tl-item" :class="{ on: i === cur, done: started && i < cur }" @click="cur = i">
            <div class="tl-dot"><span>{{ n.seq }}</span></div>
            <div class="tl-txt">
              <b>{{ n.title }}</b>
              <small>{{ NODE_TYPE[n.nodeType] }} · 停留 {{ n.stayMinutes }} 分钟</small>
            </div>
          </div>
        </aside>

        <section class="stage">
          <transition name="route-fade" mode="out-in">
            <div v-if="node" :key="node.id" class="card panel">
              <div class="card-img">
                <BronzeImage :src="node.artifactCover || (node.nodeType === 'HALL' ? d.route.coverUrl : undefined)" :alt="node.title" ratio="auto" class="ci" />
                <div class="step serif">第 {{ node.seq }} 站 / 共 {{ d.nodes.length }} 站</div>
              </div>
              <div class="card-body">
                <div class="tags"><span class="chip">{{ NODE_TYPE[node.nodeType] }}</span><span v-if="node.hallName" class="chip jade">{{ node.hallName }}</span><span class="chip"><el-icon><Timer /></el-icon>{{ node.stayMinutes }} 分钟</span></div>
                <h2>{{ node.title }}</h2>
                <blockquote>{{ node.narration }}</blockquote>
                <div class="acts">
                  <button class="btn-ghost btn-sm" @click="speak"><el-icon><component :is="speaking ? 'VideoPause' : 'Headset'" /></el-icon>{{ speaking ? '停止讲解' : '语音讲解' }}</button>
                  <router-link v-if="node.artifactId" :to="`/artifacts/${node.artifactId}`" class="btn-ghost btn-sm">文物详情</router-link>
                  <router-link v-if="node.artifactId && node.modelUrl" :to="{ path: '/hall3d', query: { artifact: node.artifactId } }" class="btn-ghost btn-sm"><el-icon><Box /></el-icon>3D 观赏</router-link>
                </div>
                <div class="nav">
                  <el-button :disabled="cur === 0" @click="go(-1)"><el-icon><ArrowLeft /></el-icon>上一站</el-button>
                  <el-progress :percentage="Math.round(((cur + 1) / d.nodes.length) * 100)" :stroke-width="4" :show-text="false" color="#c9a45c" style="flex: 1" />
                  <el-button type="primary" :disabled="cur === d.nodes.length - 1" @click="go(1)">下一站<el-icon><ArrowRight /></el-icon></el-button>
                </div>
              </div>
            </div>
          </transition>
          <div class="panel panel-pad reviews">
            <h3 class="rv-h">路线评价</h3>
            <ReviewPanel target-type="ROUTE" :target-id="d.route.id" />
          </div>
        </section>
      </div>
    </template>
  </div>
</template>
<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import BronzeImage from '@/components/BronzeImage.vue'
import ReviewPanel from '@/components/ReviewPanel.vue'
import { routeApi } from '@/api'
import type { RouteDetail } from '@/api/types'
import { ROUTE_TYPE } from '@/utils/format'

const NODE_TYPE: Record<string, string> = { ARTIFACT: '文物', HALL: '展厅', POI: '服务点' }
const route = useRoute()
const d = ref<RouteDetail>()
const loading = ref(true)
const cur = ref(0)
const started = ref(false)
const speaking = ref(false)
const node = computed(() => d.value?.nodes[cur.value])

function go(s: number) { stopSpeak(); cur.value = Math.min(Math.max(0, cur.value + s), (d.value?.nodes.length || 1) - 1) }
function start() { cur.value = 0; started.value = true; routeApi.start(d.value!.route.id); ElMessage.success('导览开始，祝你参观愉快'); speak() }
function stopSpeak() { if ('speechSynthesis' in window) speechSynthesis.cancel(); speaking.value = false }
function speak() {
  if (!('speechSynthesis' in window) || !node.value?.narration) return
  if (speaking.value) { stopSpeak(); return }
  const u = new SpeechSynthesisUtterance(node.value.narration)
  u.lang = 'zh-CN'
  u.onend = () => (speaking.value = false)
  speaking.value = true
  speechSynthesis.speak(u)
}
onMounted(async () => { try { d.value = await routeApi.detail(route.params.id as string) } finally { loading.value = false } })
onBeforeUnmount(stopSpeak)
</script>
<style scoped>
.rd { min-height: 100vh; }
.kpis { display: flex; align-items: center; gap: 34px; flex-wrap: wrap; }
.kpis div { display: flex; align-items: baseline; gap: 6px; }
.kpis b { font-size: 34px; color: var(--gold-light); font-weight: 900; }
.kpis span { color: var(--text-3); font-size: 13px; }
.body { display: grid; grid-template-columns: 320px 1fr; gap: 22px; padding: 34px 0 90px; align-items: start; }
.timeline { padding: 18px 14px; position: sticky; top: calc(var(--header-h) + 16px); }
.tl-h { font-family: var(--font-serif); color: var(--gold-light); letter-spacing: .2em; margin: 0 8px 12px; }
.tl-item { display: flex; gap: 12px; padding: 10px 8px; border-radius: 12px; cursor: pointer; position: relative; transition: background .25s; }
.tl-item:not(:last-child)::after { content: ''; position: absolute; left: 25px; top: 44px; bottom: -10px; width: 1px; background: var(--line-strong); }
.tl-item:hover { background: rgba(201,164,92,.05); }
.tl-item.on { background: rgba(201,164,92,.1); }
.tl-dot { flex: none; width: 34px; height: 34px; border-radius: 50%; display: grid; place-items: center; border: 1px solid var(--line-strong); color: var(--gold); font-family: var(--font-serif); background: var(--bg-2); position: relative; z-index: 1; }
.tl-item.on .tl-dot { background: var(--grad-gold); color: #1a140a; box-shadow: 0 0 0 5px rgba(201,164,92,.15); border-color: transparent; }
.tl-item.done .tl-dot { background: rgba(111,174,149,.2); color: #9fd4bd; border-color: rgba(111,174,149,.4); }
.tl-txt b { display: block; font-size: 14.5px; color: var(--text-1); font-weight: 500; }
.tl-item.on .tl-txt b { color: var(--gold-light); }
.tl-txt small { color: var(--text-3); }
.card { display: grid; grid-template-columns: 0.9fr 1.1fr; min-height: 460px; }
.card-img { position: relative; min-height: 320px; }
.ci { position: absolute !important; inset: 0; height: 100%; }
.step { position: absolute; left: 20px; bottom: 16px; padding: 6px 14px; border-radius: 999px; background: rgba(8,10,9,.7); backdrop-filter: blur(6px); color: var(--gold-light); letter-spacing: .15em; border: 1px solid var(--line); }
.card-body { padding: 24px 28px 26px; }
.tags { display: flex; gap: 8px; flex-wrap: wrap; }
.card-body h2 { font-size: 28px; letter-spacing: .14em; margin: 14px 0; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
blockquote { margin: 0; padding: 18px 22px; border-left: 3px solid var(--gold); background: linear-gradient(90deg, rgba(201,164,92,.08), transparent); font-family: var(--font-serif); font-size: 16px; line-height: 2.1; border-radius: 0 12px 12px 0; }
.acts { display: flex; gap: 10px; flex-wrap: wrap; margin-top: 18px; }
.nav { display: flex; align-items: center; gap: 18px; margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--line); }
.reviews { margin-top: 22px; }
.rv-h { font-size: 18px; color: var(--gold-light); letter-spacing: .14em; margin-bottom: 14px; }
@media (max-width: 900px) { .body, .card { grid-template-columns: 1fr; } .timeline { position: static; } }
</style>
