<template>
  <div class="sv" :class="{ ready: state === 'ready' }">
    <div ref="host" class="sv-host"></div>

    <!-- 加载 / 占位状态 -->
    <transition name="fade">
      <div v-if="state !== 'ready'" class="sv-overlay">
        <div class="sv-ring">
          <svg viewBox="0 0 120 120" class="ring-svg">
            <circle cx="60" cy="60" r="52" class="track" />
            <circle cx="60" cy="60" r="52" class="bar" :style="{ strokeDashoffset: 327 - (327 * shownProgress) / 100 }" />
          </svg>
          <SunLogo :size="54" spin />
        </div>
        <div class="sv-title serif">{{ stateTitle }}</div>
        <div class="sv-sub">{{ stateSub }}</div>
        <button v-if="state === 'error'" class="btn-ghost btn-sm" @click="load()">重新加载</button>
      </div>
    </transition>

    <!-- 信息条 -->
    <div class="sv-badges">
      <span v-if="usingSample" class="chip red" title="该文物的 3DGS 模型为占位地址，当前加载公开示例模型用于演示">
        <el-icon><Warning /></el-icon>示例模型（原模型为占位地址）
      </span>
      <span v-else-if="state === 'ready'" class="chip jade"><el-icon><CircleCheck /></el-icon>3DGS 实时渲染</span>
      <span v-if="format" class="chip">{{ format.toUpperCase() }}</span>
    </div>
    <div v-if="state === 'ready' && showTips" class="sv-tips">
      <span><b>左键</b> 旋转</span><span><b>右键</b> 平移</span><span><b>滚轮</b> 缩放</span>
      <button class="tip-btn" :class="{ on: autoRotate }" @click="toggleRotate"><el-icon><Refresh /></el-icon>自动旋转</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import SunLogo from './SunLogo.vue'
import { APP_CONFIG } from '@/config'

const props = withDefaults(
  defineProps<{
    url?: string | null
    cameraPosition?: number[]
    cameraTarget?: number[]
    format?: string
    showTips?: boolean
    autoStart?: boolean
  }>(),
  { showTips: true, autoStart: true }
)

type State = 'idle' | 'checking' | 'loading' | 'ready' | 'error' | 'nowebgl'
const host = ref<HTMLDivElement>()
const state = ref<State>('idle')
const progress = ref(0)
const usingSample = ref(false)
const autoRotate = ref(false)
const errMsg = ref('')
let viewer: any = null
let loadToken = 0

const shownProgress = computed(() => (state.value === 'loading' ? Math.max(4, Math.min(100, progress.value)) : state.value === 'checking' ? 4 : 0))
const stateTitle = computed(() => {
  switch (state.value) {
    case 'checking': return '正在定位模型文件'
    case 'loading': return `模型加载中 ${Math.round(progress.value)}%`
    case 'error': return '模型暂不可用'
    case 'nowebgl': return '当前环境不支持 WebGL'
    default: return '3DGS 模型占位'
  }
})
const stateSub = computed(() => {
  switch (state.value) {
    case 'checking': return '校验 model_3d.model_url 是否可访问…'
    case 'loading': return usingSample.value ? '原模型为占位地址，正在加载公开示例高斯泼溅场景' : '正在流式解析高斯基元并排序'
    case 'error': return errMsg.value || '网络异常或模型格式不受支持'
    case 'nowebgl': return '请使用支持 WebGL2 的现代浏览器（Chrome / Edge / Safari）'
    default: return '等待加载'
  }
})

function webglOk() {
  try {
    const c = document.createElement('canvas')
    return !!(c.getContext('webgl2') || c.getContext('webgl'))
  } catch {
    return false
  }
}

async function reachable(url: string): Promise<boolean> {
  if (/^https?:\/\//.test(url) && !url.startsWith(location.origin)) return true // 跨域地址交给加载器判断
  try {
    const ctrl = new AbortController()
    const t = setTimeout(() => ctrl.abort(), 5000)
    const r = await fetch(url, { method: 'HEAD', signal: ctrl.signal })
    clearTimeout(t)
    const ct = r.headers.get('content-type') || ''
    return r.ok && !ct.includes('text/html')
  } catch {
    return false
  }
}

function formatOf(GS: any, url: string) {
  const u = url.split('?')[0].toLowerCase()
  if (u.endsWith('.ksplat')) return GS.SceneFormat.KSplat
  if (u.endsWith('.ply')) return GS.SceneFormat.Ply
  if (u.endsWith('.spz')) return GS.SceneFormat.Spz
  return GS.SceneFormat.Splat
}

async function disposeViewer() {
  const v = viewer
  viewer = null
  if (v) {
    try { await v.dispose() } catch { /* ignore */ }
  }
  if (host.value) host.value.innerHTML = ''
}

async function load() {
  const token = ++loadToken
  await disposeViewer()
  progress.value = 0
  usingSample.value = false
  errMsg.value = ''
  state.value = 'checking'
  let url = props.url || ''
  if (!url || !(await reachable(url))) {
    url = APP_CONFIG.sampleSplatUrl
    usingSample.value = true
  }
  if (token !== loadToken) return
  if (!webglOk()) { state.value = 'nowebgl'; return }
  try {
    const GS: any = await import('@mkkellogg/gaussian-splats-3d')
    if (token !== loadToken || !host.value) return
    const cam = usingSample.value
      ? ((window as any).__SXD_SAMPLE_CAMERA || APP_CONFIG.sampleCamera)
      : { position: props.cameraPosition || [0, 0, 3], lookAt: props.cameraTarget || [0, 0, 0], up: [0, -1, 0] }
    viewer = new GS.Viewer({
      rootElement: host.value,
      cameraUp: cam.up,
      initialCameraPosition: cam.position,
      initialCameraLookAt: cam.lookAt,
      sharedMemoryForWorkers: false,
      gpuAcceleratedSort: false,
      useBuiltInControls: true,
      selfDrivenMode: true,
      dynamicScene: false,
      sphericalHarmonicsDegree: 0,
      logLevel: GS.LogLevel?.None ?? 0
    })
    state.value = 'loading'
    await viewer.addSplatScene(url, {
      showLoadingUI: false,
      progressiveLoad: false,
      splatAlphaRemovalThreshold: 5,
      format: formatOf(GS, url),
      onProgress: (p: number) => { if (token === loadToken) progress.value = p }
    })
    if (token !== loadToken) return
    viewer.start()
    if (viewer.controls) viewer.controls.autoRotateSpeed = 0.8
    state.value = 'ready'
  } catch (e: any) {
    if (token !== loadToken) return
    console.warn('[SplatViewer] load failed', e)
    errMsg.value = e?.message ? String(e.message).slice(0, 80) : ''
    state.value = 'error'
  }
}

function toggleRotate() {
  autoRotate.value = !autoRotate.value
  if (viewer?.controls) viewer.controls.autoRotate = autoRotate.value
}

watch(() => props.url, () => load())
onMounted(() => { if (props.autoStart) load() })
onBeforeUnmount(() => { loadToken++; disposeViewer() })
defineExpose({ reload: load })
</script>

<style scoped>
.sv { position: relative; width: 100%; height: 100%; min-height: 320px; overflow: hidden; border-radius: inherit;
  background: radial-gradient(ellipse at 50% 45%, #1b261f 0%, #0b0f0d 55%, #050706 100%); }
.sv::before { content: ''; position: absolute; inset: 0; pointer-events: none; z-index: 1;
  background: radial-gradient(ellipse at 50% 120%, rgba(201,164,92,.16), transparent 60%); }
.sv-host { position: absolute; inset: 0; }
.sv-host :deep(canvas) { display: block; width: 100% !important; height: 100% !important; outline: none; }
.sv-overlay { position: absolute; inset: 0; z-index: 3; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; text-align: center; padding: 20px;
  background-image: repeating-radial-gradient(circle at 50% 50%, rgba(201,164,92,.04) 0 1px, transparent 1px 38px); }
.sv-ring { position: relative; width: 120px; height: 120px; display: grid; place-items: center; margin-bottom: 8px; }
.sv-ring .ring-svg { position: absolute; inset: 0; transform: rotate(-90deg); }
.sv-ring .track { fill: none; stroke: rgba(201,164,92,.12); stroke-width: 3; }
.sv-ring .bar { fill: none; stroke: var(--gold); stroke-width: 3; stroke-linecap: round; stroke-dasharray: 327; transition: stroke-dashoffset .4s var(--ease); filter: drop-shadow(0 0 6px rgba(201,164,92,.7)); }
.sv-title { font-size: 18px; color: var(--gold-light); letter-spacing: .15em; }
.sv-sub { color: var(--text-3); font-size: 13px; max-width: 360px; }
.sv-badges { position: absolute; top: 14px; left: 14px; z-index: 4; display: flex; gap: 8px; flex-wrap: wrap; }
.sv-badges .chip { backdrop-filter: blur(8px); background-color: rgba(10,14,12,.6); }
.sv-tips { position: absolute; z-index: 4; bottom: 14px; left: 50%; transform: translateX(-50%); display: flex; gap: 14px; align-items: center;
  padding: 8px 16px; border-radius: 999px; background: rgba(10,14,12,.65); border: 1px solid var(--line); backdrop-filter: blur(10px); color: var(--text-3); font-size: 12px; white-space: nowrap; }
.sv-tips b { color: var(--gold-light); font-weight: 500; margin-right: 3px; }
.tip-btn { display: inline-flex; align-items: center; gap: 4px; background: none; border: 1px solid var(--line); color: var(--text-2); border-radius: 999px; padding: 2px 10px; cursor: pointer; font-size: 12px; }
.tip-btn.on { color: #1a140a; background: var(--gold); border-color: var(--gold); }
.fade-enter-active, .fade-leave-active { transition: opacity .6s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
