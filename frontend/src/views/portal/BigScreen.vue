<template>
  <div class="screen-page">
    <div class="screen-bar">
      <router-link to="/" class="back"><el-icon><Back /></el-icon>返回首页</router-link>
      <span class="tip">三星堆数字孪生 · 运营数据大屏（公开展示）</span>
      <button class="fs" @click="toggleFs"><el-icon><FullScreen /></el-icon>{{ isFs ? '退出全屏' : '全屏展示' }}</button>
    </div>
    <Dashboard />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import Dashboard from '@/views/admin/Dashboard.vue'

const isFs = ref(false)
function onFs() { isFs.value = !!document.fullscreenElement }
function toggleFs() {
  if (document.fullscreenElement) document.exitFullscreen()
  else document.documentElement.requestFullscreen?.()
}
onMounted(() => document.addEventListener('fullscreenchange', onFs))
onBeforeUnmount(() => document.removeEventListener('fullscreenchange', onFs))
</script>

<style scoped>
.screen-page { min-height: 100vh; padding: 14px 22px 28px; background: var(--bg-0, #070908); }
.screen-bar { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.back, .fs { display: inline-flex; align-items: center; gap: 6px; padding: 7px 16px; border-radius: 999px; border: 1px solid var(--line); color: var(--gold-light); background: rgba(13,17,15,.8); font-size: 13px; cursor: pointer; font-family: inherit; }
.back:hover, .fs:hover { border-color: var(--gold); }
.tip { color: var(--text-3, #8a8a80); font-size: 12.5px; letter-spacing: .2em; }
@media (max-width: 760px) { .tip { display: none; } .screen-page { padding: 10px; } }
</style>
