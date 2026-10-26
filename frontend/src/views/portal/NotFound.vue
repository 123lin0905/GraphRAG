<template>
  <div class="nf">
    <SunLogo :size="96" spin />
    <h1 class="serif gold-text">404</h1>
    <p>这件“文物”似乎还埋在祭祀坑里，尚未出土。</p>
    <router-link to="/" class="btn-gold">返回首页</router-link>
  </div>
</template>
<script setup lang="ts">
import SunLogo from '@/components/SunLogo.vue'
</script>
<style scoped>
.nf { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; text-align: center; background: radial-gradient(circle at 50% 40%, rgba(36,57,45,.4), var(--bg-1) 60%); }
h1 { font-size: 96px; font-weight: 900; letter-spacing: .2em; line-height: 1; }
p { color: var(--text-2); margin-bottom: 12px; }
</style>
