<template>
  <div class="kg-wrap">
    <EChart :option="option" :height="height" @chart-click="onClick" />
    <div v-if="!graph || !graph.nodes?.length" class="kg-empty">
      <SunLogo :size="48" spin />
      <span>{{ emptyText }}</span>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import EChart from './EChart.vue'
import SunLogo from './SunLogo.vue'
import type { GraphNode, GraphVO } from '@/api/types'
import { ENTITY_COLORS } from '@/utils/format'

const props = withDefaults(
  defineProps<{
    graph?: GraphVO | null
    height?: string
    highlight?: string[]
    centerId?: string | number | null
    mini?: boolean
    showLegend?: boolean
    repulsion?: number
    emptyText?: string
    zoom?: number
  }>(),
  { height: '520px', highlight: () => [], mini: false, showLegend: true, repulsion: 260, emptyText: '暂无图谱数据', zoom: 1 }
)
const emit = defineEmits<{ (e: 'node-click', n: GraphNode): void }>()

const option = computed(() => {
  const g = props.graph
  if (!g) return {}
  const cats = (g.categories || []).map((c) => ({ name: c.name, itemStyle: { color: ENTITY_COLORS[c.code] || '#C9A45C' } }))
  const hl = new Set((props.highlight || []).map(String))
  const center = props.centerId != null ? String(props.centerId) : null
  const hasHl = hl.size > 0
  const nodes = g.nodes.map((n) => {
    const code = g.categories?.[n.category]?.code || n.entityType
    const color = ENTITY_COLORS[code] || '#C9A45C'
    const isHl = hl.has(n.id)
    const isCenter = center === n.id
    const base = props.mini ? Math.max(10, n.symbolSize * 0.6) : n.symbolSize
    return {
      ...n,
      symbolSize: isCenter ? base * 1.45 : isHl ? base * 1.2 : base,
      itemStyle: {
        color: {
          type: 'radial', x: 0.35, y: 0.3, r: 0.9,
          colorStops: [{ offset: 0, color: '#fff8e6' }, { offset: 0.25, color }, { offset: 1, color: shade(color) }]
        },
        borderColor: isCenter || isHl ? '#F3E3BB' : 'rgba(0,0,0,.4)',
        borderWidth: isCenter || isHl ? 2.5 : 1,
        shadowBlur: isCenter || isHl ? 26 : 10,
        shadowColor: isCenter || isHl ? 'rgba(243,227,187,.75)' : 'rgba(0,0,0,.5)',
        opacity: hasHl && !isHl ? 0.25 : 1
      },
      label: {
        show: !props.mini || isCenter || n.hop === 0 || (g.nodes.length <= 25 && n.symbolSize >= 28),
        opacity: hasHl && !isHl ? 0.3 : 1
      }
    }
  })
  const links = g.links.map((l) => {
    const isHl = hl.has(l.source) && hl.has(l.target)
    return {
      ...l,
      lineStyle: {
        width: isHl ? 3 : 1 + (l.value || 1) * 0.4,
        color: isHl ? '#E8CB8A' : 'source',
        opacity: hasHl ? (isHl ? 0.95 : 0.08) : 0.35,
        curveness: 0.12
      },
      label: { show: isHl || (!props.mini && g.links.length < 60), formatter: l.name }
    }
  })
  return {
    backgroundColor: 'transparent',
    tooltip: {
      backgroundColor: 'rgba(14,19,16,.95)',
      borderColor: 'rgba(201,164,92,.35)',
      textStyle: { color: '#efe8d8', fontSize: 12 },
      extraCssText: 'max-width:300px;white-space:normal;border-radius:10px;box-shadow:0 10px 30px rgba(0,0,0,.5);',
      formatter: (p: any) => {
        if (p.dataType === 'edge') return `<b style="color:#E8CB8A">${p.data.name}</b>`
        const d = p.data
        const desc = d.description ? `<div style="margin-top:6px;color:#bdb4a2;line-height:1.6">${String(d.description).slice(0, 90)}${d.description.length > 90 ? '…' : ''}</div>` : ''
        return `<b style="font-size:14px;color:#E8CB8A">${d.name}</b> <span style="color:#8a8473">· ${g.categories?.[d.category]?.name || ''}</span>${desc}`
      }
    },
    legend: props.showLegend && !props.mini
      ? [{ data: cats.map((c) => c.name), bottom: 8, left: 'center', textStyle: { color: '#bdb4a2', fontSize: 12 }, itemWidth: 10, itemHeight: 10, icon: 'circle', itemGap: 14 }]
      : undefined,
    animationDuration: 1200,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        roam: true,
        zoom: props.zoom,
        draggable: true,
        data: nodes,
        links,
        categories: cats,
        force: { repulsion: props.mini ? props.repulsion * 0.55 : props.repulsion, edgeLength: props.mini ? [40, 90] : [80, 200], gravity: g.nodes.length > 40 ? 0.2 : 0.06, friction: 0.12 },
        edgeSymbol: ['none', 'arrow'],
        edgeSymbolSize: [0, 6],
        label: { position: 'right', color: '#efe8d8', fontSize: props.mini ? 11 : 12, fontFamily: 'Noto Serif SC, serif', textBorderColor: '#0b0e0c', textBorderWidth: 3 },
        edgeLabel: { fontSize: 10, color: '#c9a45c', textBorderColor: '#0b0e0c', textBorderWidth: 2 },
        emphasis: { focus: 'adjacency', lineStyle: { width: 3, opacity: 0.9 }, label: { show: true } },
        blur: { itemStyle: { opacity: 0.15 }, lineStyle: { opacity: 0.05 } },
        labelLayout: { hideOverlap: g.nodes.length > 40 },
        scaleLimit: { min: 0.3, max: 4 }
      }
    ]
  }
})

function shade(hex: string) {
  const n = parseInt(hex.slice(1), 16)
  const r = Math.round(((n >> 16) & 255) * 0.45)
  const g = Math.round(((n >> 8) & 255) * 0.45)
  const b = Math.round((n & 255) * 0.45)
  return `rgb(${r},${g},${b})`
}
function onClick(p: any) {
  if (p.dataType === 'node') emit('node-click', p.data as GraphNode)
}
</script>
<style scoped>
.kg-wrap { position: relative; }
.kg-empty { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; color: var(--text-3); letter-spacing: .2em; pointer-events: none; }
</style>
