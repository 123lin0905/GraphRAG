<template>
  <div class="dash">
    <div class="d-head">
      <div class="d-title">
        <SunLogo :size="46" spin />
        <div>
          <h1 class="serif">三星堆数字孪生 · 运营数据大屏</h1>
          <p>DIGITAL TWIN OPERATION CENTER · 文物 / 3DGS / 知识图谱 / 智能问答 / 观众服务</p>
        </div>
      </div>
      <div class="clock">
        <b class="serif">{{ clock.time }}</b>
        <span>{{ clock.date }}</span>
        <el-button size="small" circle @click="loadAll" :loading="loading"><el-icon><Refresh /></el-icon></el-button>
      </div>
    </div>

    <div class="kpis">
      <KpiCard label="馆藏文物" :value="ov?.artifactCount" unit="件" icon="Trophy" color="#E0B866" :sub="`镇馆之宝 ${ov?.treasureCount ?? '—'} 件`" />
      <KpiCard label="3DGS 模型" :value="ov?.modelCount" unit="个" icon="Box" color="#7FB8A1" :sub="`高斯基元 ${fmtNum(ov?.gaussianTotal)}`" />
      <KpiCard label="图谱实体" :value="ov?.entityCount" unit="个" icon="Share" color="#A48FD0" :sub="`关系 ${ov?.relationCount ?? '—'} 条 · 文献 ${ov?.literatureCount ?? '—'} 篇`" />
      <KpiCard label="近30天线下客流" :value="ov?.visitors30d" icon="UserFilled" color="#D79A5B" format="wan" :sub="`注册用户 ${ov?.userCount ?? '—'} 人`" />
      <KpiCard label="智能问答" :value="ov?.qaQuestionCount" unit="次" icon="ChatDotRound" color="#8DA4D8" :sub="`会话 ${ov?.qaSessionCount ?? '—'} 个`" />
      <KpiCard label="有效预约" :value="ov?.reservationCount" unit="单" icon="Tickets" color="#C9694F" :sub="`今日预约 ${ov?.todayReservationVisitors ?? 0} 人`" />
      <KpiCard label="观众评分" :value="ov?.avgRating" icon="StarFilled" color="#F0D9A0" :decimals="1" sub="满分 5.0" />
      <KpiCard label="导览路线" :value="ov?.routeCount" unit="条" icon="Guide" color="#9BBF6A" :sub="`祭祀坑 ${ov?.pitCount ?? '—'} 个`" />
    </div>

    <div class="row r1">
      <div class="card span2">
        <div class="c-h"><span><i></i>客流与访问趋势</span>
          <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
            <el-radio-button :value="7">7天</el-radio-button><el-radio-button :value="14">14天</el-radio-button><el-radio-button :value="30">30天</el-radio-button>
          </el-radio-group>
        </div>
        <EChart :option="trendOpt" height="330px" />
      </div>
      <div class="card">
        <div class="c-h"><span><i></i>文物分类构成</span></div>
        <EChart :option="categoryOpt" height="330px" />
      </div>
    </div>

    <div class="row r3">
      <div class="card">
        <div class="c-h"><span><i></i>祭祀坑出土分布</span></div>
        <EChart :option="pitOpt" height="290px" />
      </div>
      <div class="card">
        <div class="c-h"><span><i></i>材质分布</span></div>
        <EChart :option="materialOpt" height="290px" />
      </div>
      <div class="card">
        <div class="c-h"><span><i></i>热门文物 TOP10</span></div>
        <EChart :option="hotOpt" height="290px" />
      </div>
    </div>

    <div class="row r3">
      <div class="card">
        <div class="c-h"><span><i></i>智能问答趋势（近14天）</span><em class="tag">平均耗时 {{ qa?.performance?.avgLatencyMs ?? '—' }} ms</em></div>
        <EChart :option="qaOpt" height="270px" />
      </div>
      <div class="card">
        <div class="c-h"><span><i></i>回答模式 · 大模型 vs 兜底</span></div>
        <EChart :option="qaModeOpt" height="270px" />
      </div>
      <div class="card">
        <div class="c-h"><span><i></i>知识图谱实体类型</span></div>
        <EChart :option="kgOpt" height="270px" />
      </div>
    </div>

    <div class="row r2">
      <div class="card">
        <div class="c-h"><span><i></i>3DGS 重建质量（PSNR × SSIM，气泡=高斯数）</span></div>
        <EChart :option="qualityOpt" height="300px" />
      </div>
      <div class="card">
        <div class="c-h"><span><i></i>观众服务</span></div>
        <div class="svc">
          <EChart :option="resOpt" height="300px" class="svc-a" />
          <EChart :option="routeOpt" height="300px" class="svc-b" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import EChart from '@/components/EChart.vue'
import KpiCard from '@/components/KpiCard.vue'
import SunLogo from '@/components/SunLogo.vue'
import { dashboardApi } from '@/api'
import type { NameValue, Overview } from '@/api/types'
import { fmtNum } from '@/utils/format'

const PALETTE = ['#E0B866', '#7FB8A1', '#C9694F', '#A48FD0', '#8DA4D8', '#D79A5B', '#E58FA0', '#9BBF6A', '#79C3D6', '#F0D9A0']
const AXIS = { axisLine: { lineStyle: { color: 'rgba(201,164,92,.25)' } }, axisLabel: { color: '#8a8473', fontSize: 11 }, splitLine: { lineStyle: { color: 'rgba(201,164,92,.07)', type: 'dashed' } }, axisTick: { show: false } }
const TOOLTIP = { backgroundColor: 'rgba(14,19,16,.95)', borderColor: 'rgba(201,164,92,.35)', textStyle: { color: '#efe8d8', fontSize: 12 }, extraCssText: 'border-radius:10px;box-shadow:0 10px 30px rgba(0,0,0,.5);' }
const LEGEND = { textStyle: { color: '#bdb4a2', fontSize: 11 }, itemWidth: 10, itemHeight: 10, icon: 'roundRect' }
const gold = (a = 1) => new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: `rgba(243,227,187,${a})` }, { offset: 1, color: `rgba(156,122,60,${a})` }])

const loading = ref(false)
const ov = ref<Overview>()
const dist = ref<Record<string, NameValue[]>>({})
const subCats = ref<NameValue[]>([])
const trend = ref<{ dates: string[]; series: Record<string, number[]> }>({ dates: [], series: {} })
const trendDays = ref(30)
const qa = ref<any>()
const models = ref<any>()
const services = ref<any>()
const clock = reactive({ time: '', date: '' })
let timer: number | undefined

function tick() {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  clock.time = `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
  clock.date = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${'日一二三四五六'[d.getDay()]}`
}

const trendOpt = computed(() => {
  const names = Object.keys(trend.value.series)
  const left = names.filter((n) => n !== '智能问答' && n !== '预约单数')
  const colors = ['#E0B866', '#7FB8A1', '#A48FD0', '#8DA4D8', '#C9694F']
  return {
    color: colors, tooltip: { ...TOOLTIP, trigger: 'axis' }, legend: { ...LEGEND, top: 0, right: 0 },
    grid: { left: 50, right: 50, top: 40, bottom: 28 },
    xAxis: { type: 'category', data: trend.value.dates.map((d) => d.slice(5)), boundaryGap: false, ...AXIS, splitLine: { show: false } },
    yAxis: [{ type: 'value', ...AXIS, name: '人次', nameTextStyle: { color: '#8a8473' } }, { type: 'value', ...AXIS, splitLine: { show: false }, name: '问答/预约', nameTextStyle: { color: '#8a8473' } }],
    series: names.map((n, i) => {
      const onRight = !left.includes(n)
      return {
        name: n, type: n === '预约单数' ? 'bar' : 'line', yAxisIndex: onRight ? 1 : 0, smooth: true, showSymbol: false, data: trend.value.series[n],
        barWidth: '38%', itemStyle: n === '预约单数' ? { borderRadius: [3, 3, 0, 0], opacity: 0.35 } : undefined,
        lineStyle: { width: i === 0 ? 3 : 2, type: n === '智能问答' ? 'dashed' : 'solid', shadowBlur: i === 0 ? 12 : 0, shadowColor: 'rgba(224,184,102,.5)' },
        areaStyle: !onRight && i < 2 ? { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: colors[i] + '55' }, { offset: 1, color: colors[i] + '00' }]) } : undefined
      }
    })
  }
})
const categoryOpt = computed(() => ({
  color: PALETTE, tooltip: { ...TOOLTIP, trigger: 'item', formatter: '{b}：{c} 件（{d}%）' },
  legend: { ...LEGEND, bottom: 0, left: 'center', type: 'scroll', pageTextStyle: { color: '#8a8473' } },
  series: [
    { type: 'pie', radius: ['18%', '64%'], center: ['50%', '45%'], roseType: 'radius', itemStyle: { borderRadius: 6, borderColor: '#0d110f', borderWidth: 2 },
      label: { color: '#bdb4a2', fontSize: 11, formatter: '{b}\n{c}' }, labelLine: { lineStyle: { color: 'rgba(201,164,92,.4)' } }, data: subCats.value.length ? subCats.value : dist.value.category || [] },
    { type: 'pie', radius: ['74%', '75.5%'], center: ['50%', '45%'], silent: true, label: { show: false }, data: [{ value: 1, itemStyle: { color: 'rgba(201,164,92,.35)' } }] }
  ]
}))
const pitOpt = computed(() => {
  const d = [...(dist.value.pit || [])]
  return {
    tooltip: { ...TOOLTIP, trigger: 'axis' }, grid: { left: 40, right: 16, top: 20, bottom: 50 },
    xAxis: { type: 'category', data: d.map((x) => x.name.replace('祭祀坑', '坑')), ...AXIS, axisLabel: { ...AXIS.axisLabel, rotate: 30 } },
    yAxis: { type: 'value', minInterval: 1, ...AXIS },
    series: [
      { type: 'bar', data: d.map((x) => x.value), barWidth: 16, itemStyle: { color: gold(), borderRadius: [8, 8, 0, 0], shadowBlur: 10, shadowColor: 'rgba(224,184,102,.35)' },
        label: { show: true, position: 'top', color: '#E8CB8A', fontSize: 11 } },
      { type: 'pictorialBar', data: d.map((x) => x.value), symbol: 'diamond', symbolSize: [16, 8], symbolPosition: 'end', symbolOffset: [0, -4], z: 3, itemStyle: { color: '#F3E3BB' } }
    ]
  }
})
const materialOpt = computed(() => {
  const d = dist.value.material || []
  const total = d.reduce((a, b) => a + b.value, 0)
  return {
    color: ['#7FB8A1', '#E0B866', '#D79A5B', '#A48FD0', '#8DA4D8'], tooltip: { ...TOOLTIP, trigger: 'item' },
    legend: { ...LEGEND, orient: 'vertical', right: 8, top: 'middle' },
    title: { text: String(total), subtext: '件文物', left: '34%', top: '38%', textAlign: 'center', textStyle: { color: '#E8CB8A', fontSize: 30, fontFamily: 'Noto Serif SC, serif', fontWeight: 900 }, subtextStyle: { color: '#8a8473' } },
    series: [{ type: 'pie', radius: ['52%', '72%'], center: ['35%', '50%'], padAngle: 3, itemStyle: { borderRadius: 8 }, label: { show: false }, data: d }]
  }
})
const hotOpt = computed(() => {
  const d = [...(models.value?.hotArtifacts || [])].reverse()
  return {
    tooltip: { ...TOOLTIP, trigger: 'axis' }, grid: { left: 110, right: 40, top: 6, bottom: 16 },
    xAxis: { type: 'value', ...AXIS, axisLabel: { show: false } },
    yAxis: { type: 'category', data: d.map((x: NameValue) => x.name), ...AXIS, axisLabel: { color: '#bdb4a2', fontSize: 11, width: 100, overflow: 'truncate' } },
    series: [{ type: 'bar', data: d.map((x: NameValue) => x.value), barWidth: 10, showBackground: true, backgroundStyle: { color: 'rgba(201,164,92,.05)', borderRadius: 5 },
      itemStyle: { borderRadius: 5, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: 'rgba(127,184,161,.3)' }, { offset: 1, color: '#E0B866' }]) },
      label: { show: true, position: 'right', color: '#bdb4a2', fontSize: 11 } }]
  }
})
const qaOpt = computed(() => ({
  tooltip: { ...TOOLTIP, trigger: 'axis' }, grid: { left: 36, right: 16, top: 20, bottom: 26 },
  xAxis: { type: 'category', data: (qa.value?.dates || []).map((d: string) => d.slice(5)), ...AXIS },
  yAxis: { type: 'value', minInterval: 1, ...AXIS },
  series: [{ name: '提问数', type: 'bar', barWidth: '46%', data: qa.value?.dailyQuestions || [], showBackground: true, backgroundStyle: { color: 'rgba(141,164,216,.05)', borderRadius: 4 },
    itemStyle: { borderRadius: [4, 4, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#B9C8EE' }, { offset: 1, color: 'rgba(141,164,216,.25)' }]) } }]
}))
const qaModeOpt = computed(() => {
  const d: NameValue[] = qa.value?.modeDistribution || []
  const total = d.reduce((a, b) => a + b.value, 0) || 1
  const llm = d.find((x) => x.name.includes('大模型'))?.value || 0
  return {
    series: [
      { type: 'gauge', startAngle: 220, endAngle: -40, min: 0, max: 100, radius: '88%', center: ['50%', '56%'],
        progress: { show: true, width: 14, itemStyle: { color: gold() } }, axisLine: { lineStyle: { width: 14, color: [[1, 'rgba(201,164,92,.1)']] } },
        pointer: { show: false }, axisTick: { show: false }, splitLine: { length: 8, distance: -22, lineStyle: { color: 'rgba(201,164,92,.35)' } }, axisLabel: { distance: -8, color: '#5f5b50', fontSize: 10 },
        anchor: { show: false }, title: { offsetCenter: [0, '32%'], color: '#8a8473', fontSize: 12 },
        detail: { valueAnimation: true, offsetCenter: [0, '-4%'], formatter: '{value}%', color: '#E8CB8A', fontSize: 32, fontFamily: 'Noto Serif SC, serif', fontWeight: 900 },
        data: [{ value: Math.round((llm / total) * 100), name: `大模型生成占比 · 兜底 ${total - llm} 次` }] }
    ]
  }
})
const kgOpt = computed(() => {
  const d: NameValue[] = services.value?.kgEntityTypes || []
  if (!d.length) return {}
  return {
    color: PALETTE, tooltip: { ...TOOLTIP, trigger: 'item' },
    radar: { indicator: d.map((x) => ({ name: x.name, max: Math.max(...d.map((y) => y.value), 1) })), radius: '66%', center: ['50%', '54%'],
      axisName: { color: '#bdb4a2', fontSize: 11 }, splitLine: { lineStyle: { color: 'rgba(201,164,92,.15)' } }, splitArea: { areaStyle: { color: ['rgba(201,164,92,.02)', 'rgba(201,164,92,.05)'] } }, axisLine: { lineStyle: { color: 'rgba(201,164,92,.15)' } } },
    series: [{ type: 'radar', data: [{ value: d.map((x) => x.value), name: '实体数量', areaStyle: { color: 'rgba(164,143,208,.3)' }, lineStyle: { color: '#A48FD0', width: 2 }, itemStyle: { color: '#A48FD0' }, symbolSize: 5 }] }]
  }
})
const qualityOpt = computed(() => {
  const d = models.value?.quality || []
  return {
    tooltip: { ...TOOLTIP, formatter: (p: any) => `<b style="color:#E8CB8A">${p.data[3]}</b><br/>PSNR ${p.data[0]} dB · SSIM ${p.data[1]}<br/>高斯数 ${fmtNum(p.data[2])} · ${p.data[4]}` },
    grid: { left: 50, right: 30, top: 24, bottom: 36 },
    xAxis: { type: 'value', name: 'PSNR(dB)', nameLocation: 'middle', nameGap: 24, nameTextStyle: { color: '#8a8473' }, scale: true, ...AXIS },
    yAxis: { type: 'value', name: 'SSIM', nameTextStyle: { color: '#8a8473' }, scale: true, ...AXIS },
    series: [{ type: 'scatter', data: d.map((x: any) => [x.psnr, x.ssim, x.gaussianCount, x.name.replace(' 3DGS 模型', ''), x.format]),
      symbolSize: (v: number[]) => Math.max(14, Math.sqrt(v[2] / 1000) * 1.6),
      itemStyle: { color: new echarts.graphic.RadialGradient(0.4, 0.3, 1, [{ offset: 0, color: '#F3E3BB' }, { offset: 1, color: 'rgba(201,164,92,.35)' }]), borderColor: '#E0B866', shadowBlur: 14, shadowColor: 'rgba(224,184,102,.4)' },
      label: { show: true, formatter: (p: any) => p.data[3], position: 'top', color: '#bdb4a2', fontSize: 10 }, labelLayout: { hideOverlap: true } }]
  }
})
const resOpt = computed(() => ({
  color: ['#7FB8A1', '#E0B866', '#8a8473', '#C9694F', '#8DA4D8'], tooltip: { ...TOOLTIP, trigger: 'item' },
  title: { text: '预约状态', left: 'center', top: 4, textStyle: { color: '#bdb4a2', fontSize: 12, fontWeight: 400 } },
  legend: { ...LEGEND, bottom: 0, left: 'center' },
  series: [{ type: 'pie', radius: ['40%', '62%'], center: ['50%', '50%'], itemStyle: { borderRadius: 6, borderColor: '#0d110f', borderWidth: 2 }, label: { color: '#bdb4a2', formatter: '{c}' }, data: services.value?.reservationStatus || [] }]
}))
const routeOpt = computed(() => {
  const d: NameValue[] = services.value?.topRoutes || []
  return {
    title: { text: '路线使用热度', left: 'center', top: 4, textStyle: { color: '#bdb4a2', fontSize: 12, fontWeight: 400 } },
    tooltip: { ...TOOLTIP, trigger: 'item' },
    series: [{ type: 'funnel', left: '8%', right: '8%', top: 34, bottom: 10, sort: 'descending', gap: 3, minSize: '20%',
      label: { position: 'inside', color: '#fff8e6', fontSize: 11, formatter: '{b}  {c}', textBorderColor: 'rgba(0,0,0,.6)', textBorderWidth: 2 },
      itemStyle: { borderWidth: 0 },
      data: d.map((x, i) => ({ ...x, itemStyle: { color: `rgba(${224 - i * 14},${184 - i * 10},${102 + i * 6},${1 - i * 0.12})` } })) }]
  }
})

async function loadTrend() { trend.value = await dashboardApi.visitTrend(trendDays.value) }
async function loadAll() {
  loading.value = true
  try {
    const [o, d, q, m, s, c] = await Promise.all([dashboardApi.overview(), dashboardApi.distributionAll(), dashboardApi.qa(14), dashboardApi.models(), dashboardApi.services(), dashboardApi.distribution('category'), loadTrend()])
    ov.value = o; dist.value = d; qa.value = q; models.value = m; services.value = s; subCats.value = c
  } finally {
    loading.value = false
  }
}
onMounted(() => { tick(); timer = window.setInterval(tick, 1000); loadAll() })
onBeforeUnmount(() => clearInterval(timer))
</script>

<style scoped>
.dash { position: relative; }
.d-head { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 18px 22px; margin-bottom: 18px; border-radius: 16px; border: 1px solid var(--line); flex-wrap: wrap;
  background: radial-gradient(500px 120px at 0% 0%, rgba(201,164,92,.16), transparent 70%), radial-gradient(600px 160px at 100% 100%, rgba(61,92,74,.35), transparent 70%), rgba(13,17,15,.8); position: relative; overflow: hidden; }
.d-head::after { content: ''; position: absolute; left: 0; right: 0; bottom: 0; height: 1px; background: linear-gradient(90deg, transparent, var(--gold), transparent); }
.d-title { display: flex; align-items: center; gap: 14px; }
.d-title h1 { font-size: 24px; letter-spacing: .16em; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; font-weight: 900; }
.d-title p { color: var(--text-4); font-size: 11px; letter-spacing: .18em; margin-top: 2px; }
.clock { display: flex; align-items: center; gap: 14px; }
.clock b { font-size: 28px; color: var(--gold-light); letter-spacing: .08em; font-variant-numeric: tabular-nums; }
.clock span { color: var(--text-3); font-size: 13px; }
.kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 14px; }
.row { display: grid; gap: 14px; margin-bottom: 14px; }
.r1 { grid-template-columns: 2fr 1fr; }
.r2 { grid-template-columns: 1fr 1fr; }
.r3 { grid-template-columns: repeat(3, 1fr); }
.card { position: relative; padding: 16px 18px 10px; border-radius: 14px; border: 1px solid var(--line); background: linear-gradient(160deg, rgba(31,42,34,.45), rgba(12,16,13,.92) 55%); min-width: 0; }
.card::before, .card::after { content: ''; position: absolute; width: 12px; height: 12px; border-color: var(--gold); opacity: .7; }
.card::before { top: -1px; left: -1px; border-top: 2px solid; border-left: 2px solid; border-top-left-radius: 14px; }
.card::after { bottom: -1px; right: -1px; border-bottom: 2px solid; border-right: 2px solid; border-bottom-right-radius: 14px; }
.c-h { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 6px; }
.c-h span { display: flex; align-items: center; gap: 8px; font-family: var(--font-serif); color: var(--text-1); letter-spacing: .1em; font-size: 15px; font-weight: 600; }
.c-h i { width: 4px; height: 14px; border-radius: 2px; background: var(--grad-gold); }
.tag { font-style: normal; font-size: 12px; color: var(--gold-light); padding: 2px 10px; border-radius: 999px; background: rgba(201,164,92,.08); border: 1px solid var(--line); }
.svc { display: grid; grid-template-columns: 1fr 1.3fr; }
@media (max-width: 1280px) { .kpis { grid-template-columns: repeat(2, 1fr); } .r3 { grid-template-columns: 1fr 1fr; } }
@media (max-width: 900px) { .r1, .r2, .r3 { grid-template-columns: 1fr; } .kpis { grid-template-columns: 1fr; } .svc { grid-template-columns: 1fr; } }
</style>
