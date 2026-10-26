<template>
  <div ref="root" class="gv" :class="{ ready: state === 'ready', fs: isFullscreen }">
    <div ref="host" class="gv-host"></div>

    <!-- 加载 / 错误状态 -->
    <transition name="fade">
      <div v-if="state !== 'ready'" class="gv-overlay">
        <div class="gv-ring">
          <svg viewBox="0 0 120 120" class="ring-svg">
            <circle cx="60" cy="60" r="52" class="track" />
            <circle cx="60" cy="60" r="52" class="bar" :style="{ strokeDashoffset: 327 - (327 * shownProgress) / 100 }" />
          </svg>
          <SunLogo :size="54" spin />
        </div>
        <div class="gv-title serif">{{ stateTitle }}</div>
        <div class="gv-sub">{{ stateSub }}</div>
        <button v-if="state === 'error'" class="btn-ghost btn-sm" @click="load()">重新加载</button>
      </div>
    </transition>

    <!-- 角标 -->
    <div class="gv-badges">
      <span v-if="state === 'ready'" class="chip gold"><el-icon><CircleCheck /></el-icon>真实三维扫描</span>
      <span class="chip">{{ (format || 'glb').toUpperCase() }} 网格</span>
    </div>

    <!-- 工具栏 -->
    <div v-if="state === 'ready'" class="gv-tools">
      <button class="tool" :class="{ on: autoRotate }" title="自动旋转" @click="toggleRotate"><el-icon><Refresh /></el-icon><span>自动旋转</span></button>
      <button class="tool" title="复位视角" @click="resetView"><el-icon><Aim /></el-icon><span>复位</span></button>
      <button class="tool" :title="isFullscreen ? '退出全屏' : '全屏'" @click="toggleFullscreen"><el-icon><FullScreen /></el-icon><span>{{ isFullscreen ? '退出全屏' : '全屏' }}</span></button>
    </div>

    <div v-if="state === 'ready' && showTips" class="gv-tips">
      <span><b>左键</b> 旋转</span><span><b>右键</b> 平移</span><span><b>滚轮</b> 缩放</span>
    </div>

    <!-- 署名（CC BY 4.0 要求） -->
    <div v-if="author || sourceUrl" class="gv-credit">
      模型：<a v-if="sourceUrl" :href="sourceUrl" target="_blank" rel="noopener">{{ modelTitle || 'Sketchfab' }}</a><template v-else>{{ modelTitle }}</template>
      <template v-if="author"> · 作者 {{ author }}</template>
      <template v-if="license"> · <a href="https://creativecommons.org/licenses/by/4.0/deed.zh-hans" target="_blank" rel="noopener">{{ license }}</a></template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { RoomEnvironment } from 'three/examples/jsm/environments/RoomEnvironment.js'
import SunLogo from './SunLogo.vue'

const props = withDefaults(
  defineProps<{
    url?: string | null
    format?: string
    author?: string | null
    sourceUrl?: string | null
    license?: string | null
    modelTitle?: string | null
    showTips?: boolean
    autoRotateDefault?: boolean
    /** 取景时可用的横向比例（展厅两侧有浮层时 < 1） */
    fitWidth?: number
  }>(),
  { showTips: true, autoRotateDefault: true, fitWidth: 1 }
)
const emit = defineEmits<{ (e: 'status', s: string): void }>()

type State = 'idle' | 'loading' | 'ready' | 'error' | 'nowebgl'
const root = ref<HTMLDivElement>()
const host = ref<HTMLDivElement>()
const state = ref<State>('idle')
const progress = ref(0)
const errMsg = ref('')
const autoRotate = ref(props.autoRotateDefault)
const isFullscreen = ref(false)

let renderer: THREE.WebGLRenderer | null = null
let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let controls: OrbitControls | null = null
let envRT: THREE.WebGLRenderTarget | null = null
let model: THREE.Object3D | null = null
let raf = 0
let ro: ResizeObserver | null = null
let loadToken = 0
const home = { pos: new THREE.Vector3(), target: new THREE.Vector3() }

const shownProgress = computed(() => (state.value === 'loading' ? Math.max(4, Math.min(100, progress.value)) : 0))
const stateTitle = computed(() => {
  switch (state.value) {
    case 'loading': return `模型加载中 ${Math.round(progress.value)}%`
    case 'error': return '模型暂不可用'
    case 'nowebgl': return '当前环境不支持 WebGL'
    default: return '准备加载三维模型'
  }
})
const stateSub = computed(() => {
  switch (state.value) {
    case 'loading': return '正在下载并解析 GLB 网格与纹理'
    case 'error': return errMsg.value || '网络异常或模型文件损坏'
    case 'nowebgl': return '请使用支持 WebGL2 的现代浏览器（Chrome / Edge / Safari）'
    default: return ''
  }
})
watch(state, (s) => emit('status', s))

function webglOk() {
  try {
    const c = document.createElement('canvas')
    return !!(c.getContext('webgl2') || c.getContext('webgl'))
  } catch {
    return false
  }
}

/** 博物馆式布光：暖色主光（投影）+ 冷色补光 + 逆光勾边 + 环境反射 */
function buildScene(w: number, h: number) {
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, preserveDrawingBuffer: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.setSize(w, h)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.05
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFShadowMap
  host.value!.appendChild(renderer.domElement)

  scene = new THREE.Scene()
  const pm = new THREE.PMREMGenerator(renderer)
  envRT = pm.fromScene(new RoomEnvironment(), 0.04)
  scene.environment = envRT.texture
  scene.environmentIntensity = 0.55
  pm.dispose()

  scene.add(new THREE.HemisphereLight(0xfff1dc, 0x2a1d12, 0.5))
  const key = new THREE.DirectionalLight(0xffe6c4, 2.4)
  key.position.set(1.8, 5, 3.5)
  key.castShadow = true
  key.shadow.mapSize.set(2048, 2048)
  key.shadow.radius = 6
  key.shadow.bias = -0.0005
  Object.assign(key.shadow.camera, { left: -2, right: 2, top: 2, bottom: -2, near: 0.1, far: 20 })
  scene.add(key)
  const rim = new THREE.DirectionalLight(0xffc98a, 1.6)
  rim.position.set(-3, 2.5, -3)
  scene.add(rim)
  const fill = new THREE.DirectionalLight(0xbfd4ff, 0.35)
  fill.position.set(-3, 1, 2)
  scene.add(fill)

  // 柔和地面投影 + 展台光晕
  const ground = new THREE.Mesh(new THREE.PlaneGeometry(20, 20), new THREE.ShadowMaterial({ opacity: 0.42 }))
  ground.rotation.x = -Math.PI / 2
  ground.receiveShadow = true
  scene.add(ground)
  const c = document.createElement('canvas')
  c.width = c.height = 256
  const g = c.getContext('2d')!
  const gr = g.createRadialGradient(128, 128, 0, 128, 128, 128)
  gr.addColorStop(0, 'rgba(201,164,92,0.28)')
  gr.addColorStop(1, 'rgba(201,164,92,0)')
  g.fillStyle = gr
  g.fillRect(0, 0, 256, 256)
  const glowTex = new THREE.CanvasTexture(c)
  const glow = new THREE.Mesh(new THREE.PlaneGeometry(3.2, 3.2), new THREE.MeshBasicMaterial({ map: glowTex, transparent: true, depthWrite: false }))
  glow.rotation.x = -Math.PI / 2
  glow.position.y = 0.001
  scene.add(glow)

  camera = new THREE.PerspectiveCamera(30, w / h, 0.01, 100)
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08
  controls.autoRotate = autoRotate.value
  controls.autoRotateSpeed = 1.2
  controls.maxPolarAngle = Math.PI * 0.62
  controls.addEventListener('start', () => { if (controls) controls.autoRotate = false })
  controls.addEventListener('end', () => { if (controls) controls.autoRotate = autoRotate.value })
}

/** 把模型居中、立在地面上，并按视口自动取景 */
function frame(obj: THREE.Object3D) {
  const box = new THREE.Box3().setFromObject(obj)
  const size = box.getSize(new THREE.Vector3())
  const s = 1.6 / Math.max(size.x, size.y, size.z, 1e-6)
  obj.scale.multiplyScalar(s)
  obj.updateMatrixWorld(true)
  const b2 = new THREE.Box3().setFromObject(obj)
  const c2 = b2.getCenter(new THREE.Vector3())
  obj.position.sub(new THREE.Vector3(c2.x, b2.min.y, c2.z))
  obj.updateMatrixWorld(true)
  const b3 = new THREE.Box3().setFromObject(obj)
  const sz = b3.getSize(new THREE.Vector3())
  const tgt = b3.getCenter(new THREE.Vector3())
  const cam = camera!
  const vf = (cam.fov * Math.PI) / 360
  const hf = Math.atan(Math.tan(vf) * cam.aspect * props.fitWidth)
  const dep = Math.max(sz.x, sz.z) / 2
  const dist = (Math.max(sz.y / 2 / Math.tan(vf), dep / Math.tan(hf)) + dep) * 1.32
  const yaw = 0.35, pitch = 0.14
  home.target.copy(tgt)
  home.pos.set(tgt.x + dist * Math.sin(yaw) * Math.cos(pitch), tgt.y + dist * Math.sin(pitch), tgt.z + dist * Math.cos(yaw) * Math.cos(pitch))
  controls!.minDistance = dist * 0.25
  controls!.maxDistance = dist * 3
  resetView()
}

function resetView() {
  if (!camera || !controls) return
  camera.position.copy(home.pos)
  controls.target.copy(home.target)
  camera.lookAt(home.target)
  controls.update()
}

function toggleRotate() {
  autoRotate.value = !autoRotate.value
  if (controls) controls.autoRotate = autoRotate.value
}

async function toggleFullscreen() {
  try {
    if (document.fullscreenElement) await document.exitFullscreen()
    else await root.value?.requestFullscreen()
  } catch { /* ignore */ }
}
function onFsChange() { isFullscreen.value = document.fullscreenElement === root.value }

function onResize() {
  if (!renderer || !camera || !host.value) return
  const w = host.value.clientWidth || 1, h = host.value.clientHeight || 1
  renderer.setSize(w, h)
  camera.aspect = w / h
  camera.updateProjectionMatrix()
}

function loop() {
  raf = requestAnimationFrame(loop)
  if (!renderer || !scene || !camera) return
  controls?.update()
  renderer.render(scene, camera)
}

function disposeAll() {
  cancelAnimationFrame(raf)
  raf = 0
  ro?.disconnect()
  ro = null
  controls?.dispose()
  if (scene) {
    scene.traverse((o: any) => {
      if (o.geometry) o.geometry.dispose()
      const mats = o.material ? (Array.isArray(o.material) ? o.material : [o.material]) : []
      mats.forEach((m: any) => {
        Object.values(m).forEach((v: any) => { if (v && v.isTexture) v.dispose() })
        m.dispose?.()
      })
    })
  }
  envRT?.dispose()
  renderer?.dispose()
  renderer?.forceContextLoss?.()
  renderer = null; scene = null; camera = null; controls = null; envRT = null; model = null
  if (host.value) host.value.innerHTML = ''
}

async function load() {
  const token = ++loadToken
  disposeAll()
  progress.value = 0
  errMsg.value = ''
  if (!props.url) { state.value = 'error'; errMsg.value = '未配置模型地址'; return }
  if (!webglOk()) { state.value = 'nowebgl'; return }
  state.value = 'loading'
  const w = host.value!.clientWidth || 800, h = host.value!.clientHeight || 600
  buildScene(w, h)
  ro = new ResizeObserver(onResize)
  ro.observe(host.value!)
  loop()
  try {
    const gltf = await new GLTFLoader().loadAsync(props.url, (e: ProgressEvent) => {
      if (token === loadToken && e.total) progress.value = (e.loaded / e.total) * 100
    })
    if (token !== loadToken || !scene) return
    model = gltf.scene
    model.traverse((o: any) => {
      if (o.isMesh) {
        o.castShadow = true
        o.receiveShadow = false
        const mats = Array.isArray(o.material) ? o.material : [o.material]
        mats.forEach((m: any) => { if (m.map) m.map.anisotropy = 8 })
      }
    })
    scene.add(model)
    frame(model)
    state.value = 'ready'
  } catch (e: any) {
    if (token !== loadToken) return
    console.warn('[GltfViewer] load failed', e)
    errMsg.value = e?.message ? String(e.message).slice(0, 80) : ''
    state.value = 'error'
  }
}

watch(() => props.url, () => load())
onMounted(() => { document.addEventListener('fullscreenchange', onFsChange); load() })
onBeforeUnmount(() => { loadToken++; document.removeEventListener('fullscreenchange', onFsChange); disposeAll() })
defineExpose({ reload: load, resetView })
</script>

<style scoped>
.gv { position: relative; width: 100%; height: 100%; min-height: 320px; overflow: hidden; border-radius: inherit;
  background: radial-gradient(ellipse at 50% 42%, #3a2a1a 0%, #1d150e 50%, #0b0806 100%); }
.gv.fs { border-radius: 0; }
.gv-host { position: absolute; inset: 0; }
.gv-host :deep(canvas) { display: block; width: 100% !important; height: 100% !important; outline: none; }
.gv-overlay { position: absolute; inset: 0; z-index: 3; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; text-align: center; padding: 20px;
  background-image: repeating-radial-gradient(circle at 50% 50%, rgba(201,164,92,.04) 0 1px, transparent 1px 38px); }
.gv-ring { position: relative; width: 120px; height: 120px; display: grid; place-items: center; margin-bottom: 8px; }
.gv-ring .ring-svg { position: absolute; inset: 0; transform: rotate(-90deg); }
.gv-ring .track { fill: none; stroke: rgba(201,164,92,.12); stroke-width: 3; }
.gv-ring .bar { fill: none; stroke: var(--gold); stroke-width: 3; stroke-linecap: round; stroke-dasharray: 327; transition: stroke-dashoffset .4s var(--ease); filter: drop-shadow(0 0 6px rgba(201,164,92,.7)); }
.gv-title { font-size: 18px; color: var(--gold-light); letter-spacing: .15em; }
.gv-sub { color: var(--text-3); font-size: 13px; max-width: 360px; }
.gv-badges { position: absolute; top: 14px; left: 14px; z-index: 4; display: flex; gap: 8px; flex-wrap: wrap; }
.gv-badges .chip { backdrop-filter: blur(8px); background-color: rgba(10,14,12,.6); }
.gv-badges .chip.gold { color: #1a140a; background: var(--gold); border-color: var(--gold); }
.gv-tools { position: absolute; z-index: 4; top: 14px; right: 14px; display: flex; gap: 6px; }
.tool { display: inline-flex; align-items: center; gap: 4px; height: 30px; padding: 0 10px; border-radius: 999px; cursor: pointer; font-size: 12px;
  background: rgba(10,14,12,.62); border: 1px solid var(--line); color: var(--text-2); backdrop-filter: blur(8px); transition: all .2s; }
.tool:hover { color: var(--gold-light); border-color: var(--line-strong); }
.tool.on { color: #1a140a; background: var(--gold); border-color: var(--gold); }
.gv-tips { position: absolute; z-index: 4; bottom: 34px; left: 50%; transform: translateX(-50%); display: flex; gap: 14px; align-items: center;
  padding: 7px 16px; border-radius: 999px; background: rgba(10,14,12,.6); border: 1px solid var(--line); backdrop-filter: blur(10px); color: var(--text-3); font-size: 12px; white-space: nowrap; }
.gv-tips b { color: var(--gold-light); font-weight: 500; margin-right: 3px; }
.gv-credit { position: absolute; z-index: 4; right: 12px; bottom: 8px; font-size: 11px; color: var(--text-3); text-shadow: 0 1px 2px #000; max-width: 70%; text-align: right; }
.gv-credit a { color: var(--gold-light); text-decoration: none; }
.gv-credit a:hover { text-decoration: underline; }
.fade-enter-active, .fade-leave-active { transition: opacity .6s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
@media (max-width: 640px) { .tool span { display: none; } .gv-tips { display: none; } }
</style>
