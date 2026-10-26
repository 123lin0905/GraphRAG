<template>
  <section class="ph-hero">
    <div class="ph-bg" :style="bg ? { backgroundImage: `url(${bg})` } : undefined"></div>
    <div class="container ph-inner fade-up">
      <div class="eyebrow">{{ eyebrow }}</div>
      <h1>{{ title }}</h1>
      <p v-if="sub">{{ sub }}</p>
      <div class="extra"><slot /></div>
    </div>
  </section>
</template>
<script setup lang="ts">
defineProps<{ title: string; eyebrow?: string; sub?: string; bg?: string }>()
</script>
<style scoped>
.ph-hero { position: relative; padding: calc(var(--header-h) + 54px) 0 46px; overflow: hidden; border-bottom: 1px solid var(--line); }
.ph-bg { position: absolute; inset: 0; background-size: cover; background-position: center 35%; opacity: .28; filter: saturate(.8);
  mask-image: linear-gradient(180deg, #000 30%, transparent); -webkit-mask-image: linear-gradient(180deg, #000 30%, transparent); }
.ph-hero::before { content: ''; position: absolute; inset: 0; background:
  radial-gradient(800px 300px at 15% 0%, rgba(61,92,74,.35), transparent 70%),
  radial-gradient(600px 260px at 85% 20%, rgba(201,164,92,.12), transparent 70%); }
.ph-inner { position: relative; }
.eyebrow { color: var(--gold); letter-spacing: .5em; font-size: 12px; margin-bottom: 10px; }
h1 { font-size: clamp(30px, 4vw, 46px); font-weight: 900; letter-spacing: .14em; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
p { margin-top: 10px; color: var(--text-2); max-width: 720px; font-size: 15px; }
.extra:empty { display: none; }
.extra { margin-top: 24px; }
</style>
