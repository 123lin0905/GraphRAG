<template>
  <div ref="el" class="echart" :style="{ height }"></div>
</template>
<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import * as echarts from 'echarts'

const props = withDefaults(defineProps<{ option: Record<string, any>; height?: string; notMerge?: boolean }>(), {
  height: '320px',
  notMerge: true
})
const emit = defineEmits<{ (e: 'chart-click', p: any): void; (e: 'ready', c: echarts.ECharts): void }>()
const el = ref<HTMLDivElement>()
const chart = shallowRef<echarts.ECharts>()
let ro: ResizeObserver | undefined

onMounted(async () => {
  // 等待容器完成布局（如 Tab 切换、过渡动画中），避免力导向图以错误尺寸居中
  for (let i = 0; i < 20 && el.value && el.value.clientWidth < 10; i++) await new Promise((r) => setTimeout(r, 50))
  await new Promise((r) => requestAnimationFrame(() => r(null)))
  if (!el.value) return
  chart.value = echarts.init(el.value, undefined, { renderer: 'canvas' })
  chart.value.setOption(props.option, props.notMerge)
  chart.value.on('click', (p) => emit('chart-click', p))
  emit('ready', chart.value)
  ro = new ResizeObserver(() => chart.value?.resize())
  ro.observe(el.value)
})
watch(
  () => props.option,
  (o) => chart.value?.setOption(o, props.notMerge),
  { deep: false }
)
onBeforeUnmount(() => {
  ro?.disconnect()
  chart.value?.dispose()
})
defineExpose({ chart })
</script>
<style scoped>
.echart { width: 100%; }
</style>
