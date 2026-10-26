<template>
  <div class="bimg" :style="{ aspectRatio: ratio }">
    <img v-if="src && !failed" :src="src" :alt="alt" loading="lazy" @error="failed = true" @load="loaded = true" :class="{ loaded }" />
    <div v-else class="ph">
      <SunLogo :size="54" />
      <span class="serif">{{ alt || '图像待采集' }}</span>
    </div>
    <slot />
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import SunLogo from './SunLogo.vue'
const props = withDefaults(defineProps<{ src?: string | null; alt?: string; ratio?: string }>(), { ratio: '4 / 3' })
const failed = ref(false)
const loaded = ref(false)
watch(() => props.src, () => { failed.value = false; loaded.value = false })
</script>
<style scoped>
.bimg { container-type: inline-size; position: relative; overflow: hidden; background: radial-gradient(circle at 50% 40%, #1d2a21, #0b0e0c 70%); width: 100%; }
img { width: 100%; height: 100%; object-fit: cover; opacity: 0; transition: opacity .6s var(--ease), transform .8s var(--ease); }
img.loaded { opacity: 1; }
.ph { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; color: var(--text-3); font-size: 13px; letter-spacing: .2em;
  background-image: var(--noise), repeating-linear-gradient(45deg, rgba(201,164,92,.035) 0 2px, transparent 2px 14px); }
.ph :deep(svg) { opacity: .6; }
@container (max-width: 120px) { .ph span { display: none; } .ph :deep(svg) { width: 26px; height: 26px; } }
</style>
