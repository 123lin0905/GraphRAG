<template>
  <div class="sh" :class="[align]">
    <div v-if="eyebrow" class="eyebrow"><i></i>{{ eyebrow }}<i></i></div>
    <h2 class="title">{{ title }}</h2>
    <p v-if="sub" class="sub">{{ sub }}</p>
  </div>
</template>
<script setup lang="ts">
withDefaults(defineProps<{ title: string; eyebrow?: string; sub?: string; align?: 'center' | 'left' }>(), { align: 'center' })
</script>
<style scoped>
.sh { margin-bottom: 36px; }
.sh.center { text-align: center; }
.eyebrow { display: inline-flex; align-items: center; gap: 12px; color: var(--gold); font-size: 12px; letter-spacing: .5em; text-transform: uppercase; margin-bottom: 12px; }
.left .eyebrow i:first-child { display: none; }
.eyebrow i { display: block; width: 36px; height: 1px; background: linear-gradient(90deg, transparent, var(--gold)); }
.eyebrow i:last-child { transform: scaleX(-1); }
.title { font-size: clamp(26px, 3vw, 38px); font-weight: 900; letter-spacing: .12em; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; line-height: 1.3; }
.sub { margin-top: 10px; color: var(--text-3); font-size: 15px; letter-spacing: .05em; }
</style>
