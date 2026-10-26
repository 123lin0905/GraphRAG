<template>
  <svg :width="size" :height="size" viewBox="0 0 64 64" class="sun-logo" :class="{ spin }" aria-hidden="true">
    <defs>
      <linearGradient id="sl-g" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="#F3E3BB" />
        <stop offset=".5" stop-color="#C9A45C" />
        <stop offset="1" stop-color="#8E6B30" />
      </linearGradient>
    </defs>
    <circle cx="32" cy="32" r="28" fill="none" stroke="url(#sl-g)" stroke-width="2.4" />
    <circle cx="32" cy="32" r="22" fill="none" stroke="url(#sl-g)" stroke-width=".8" opacity=".5" />
    <g class="rays" stroke="url(#sl-g)" stroke-width="3" stroke-linecap="round" fill="none">
      <path v-for="i in 5" :key="i" :transform="`rotate(${i * 72} 32 32)`" d="M32 25 C 33 17, 40 11, 47 11" />
    </g>
    <circle cx="32" cy="32" r="7.5" fill="url(#sl-g)" />
    <circle cx="32" cy="32" r="3" fill="#0b0e0c" opacity=".55" />
  </svg>
</template>
<script setup lang="ts">
withDefaults(defineProps<{ size?: number; spin?: boolean }>(), { size: 36, spin: false })
</script>
<style scoped>
.sun-logo { flex: none; filter: drop-shadow(0 0 10px rgba(201, 164, 92, 0.25)); }
.spin .rays { transform-origin: 32px 32px; animation: rot 40s linear infinite; }
@keyframes rot { to { transform: rotate(360deg); } }
</style>
