<template>
  <div class="kpi">
    <div class="k-icon" :style="{ color, background: `radial-gradient(circle at 30% 30%, ${color}40, ${color}0d)`, borderColor: color + '55' }">
      <el-icon :size="22"><component :is="icon" /></el-icon>
    </div>
    <div class="k-txt">
      <div class="k-label">{{ label }}</div>
      <div class="k-val serif">{{ shown }}<small v-if="unit">{{ unit }}</small></div>
      <div v-if="sub" class="k-sub">{{ sub }}</div>
    </div>
    <svg class="k-deco" viewBox="0 0 100 40" preserveAspectRatio="none"><path :d="spark" fill="none" :stroke="color" stroke-width="1.5" opacity=".3" /></svg>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
const props = withDefaults(defineProps<{ label: string; value?: number | null; unit?: string; icon: string; color?: string; sub?: string; decimals?: number; format?: 'wan' | 'raw' }>(), { color: '#E0B866', decimals: 0, format: 'raw' })
const cur = ref(0)
const shown = computed(() => {
  if (props.value === undefined || props.value === null) return '—'
  const v = cur.value
  if (props.format === 'wan' && props.value >= 1e4) return (v / 1e4).toFixed(1) + '万'
  return v.toLocaleString('zh-CN', { minimumFractionDigits: props.decimals, maximumFractionDigits: props.decimals })
})
const spark = computed(() => {
  let s = 0
  for (const c of props.label) s += c.charCodeAt(0)
  const pts = Array.from({ length: 11 }, (_, i) => `${i * 10},${22 - Math.sin(i * 0.9 + s) * 8 - i * 0.9}`)
  return 'M' + pts.join(' L')
})
function animate() {
  const target = props.value || 0
  const start = performance.now()
  const from = 0
  const step = (t: number) => {
    const p = Math.min(1, (t - start) / 1400)
    const e = 1 - Math.pow(1 - p, 3)
    cur.value = from + (target - from) * e
    if (p < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}
watch(() => props.value, animate)
onMounted(animate)
</script>
<style scoped>
.kpi { position: relative; display: flex; gap: 14px; align-items: center; padding: 18px 18px; border-radius: 14px; border: 1px solid var(--line); overflow: hidden;
  background: linear-gradient(150deg, rgba(36,57,45,.32), rgba(13,17,15,.9) 60%); transition: transform .3s var(--ease), border-color .3s; }
.kpi:hover { transform: translateY(-3px); border-color: var(--line-strong); }
.kpi::before { content: ''; position: absolute; left: 0; top: 0; width: 40%; height: 1px; background: linear-gradient(90deg, var(--gold), transparent); }
.k-icon { width: 48px; height: 48px; flex: none; border-radius: 14px; display: grid; place-items: center; border: 1px solid; }
.k-txt { min-width: 0; position: relative; z-index: 1; }
.k-label { color: var(--text-3); font-size: 12.5px; letter-spacing: .12em; white-space: nowrap; }
.k-val { font-size: 28px; font-weight: 900; color: var(--text-1); line-height: 1.25; letter-spacing: .02em; white-space: nowrap; }
.k-val small { font-size: 13px; margin-left: 3px; color: var(--text-3); font-weight: 600; }
.k-sub { font-size: 11.5px; color: var(--text-4); }
.k-deco { position: absolute; right: 0; bottom: 6px; width: 46%; height: 34px; }
</style>
