<template>
  <router-link :to="`/artifacts/${a.id}`" class="acard">
    <BronzeImage :src="a.coverUrl" :alt="a.name" ratio="4 / 3.4">
      <div class="badges">
        <span v-if="a.isTreasure" class="chip treasure"><el-icon><Star /></el-icon>镇馆之宝</span>
        <span v-if="a.hasModel" class="chip jade">3DGS</span>
      </div>
      <div class="shade"></div>
    </BronzeImage>
    <div class="body">
      <div class="no">{{ a.artifactNo }}</div>
      <h3 class="name ellipsis">{{ a.name }}</h3>
      <div class="meta">
        <span v-if="a.pitName">{{ a.pitName }}</span>
        <i v-if="a.pitName && a.materialName"></i>
        <span v-if="a.materialName">{{ a.materialName }}</span>
        <i v-if="a.grade"></i>
        <span v-if="a.grade">{{ a.grade }}文物</span>
      </div>
      <div class="foot">
        <span><el-icon><View /></el-icon>{{ fmtNum(a.viewCount) }}</span>
        <span><el-icon><Collection /></el-icon>{{ a.favoriteCount ?? 0 }}</span>
        <span class="go">探索 <el-icon><Right /></el-icon></span>
      </div>
    </div>
  </router-link>
</template>
<script setup lang="ts">
import type { Artifact } from '@/api/types'
import BronzeImage from './BronzeImage.vue'
import { fmtNum } from '@/utils/format'
defineProps<{ a: Artifact }>()
</script>
<style scoped>
.acard { display: block; border-radius: var(--radius); overflow: hidden; background: linear-gradient(180deg, #131a15, #0d110f); border: 1px solid var(--line);
  transition: transform .45s var(--ease), box-shadow .45s var(--ease), border-color .45s; color: var(--text-1); position: relative; }
.acard:hover { transform: translateY(-6px); border-color: var(--line-strong); box-shadow: 0 24px 50px rgba(0,0,0,.5), 0 0 0 1px rgba(201,164,92,.18), 0 0 40px rgba(201,164,92,.08); color: var(--text-1); }
.acard:hover :deep(img) { transform: scale(1.06); }
.badges { position: absolute; top: 12px; left: 12px; display: flex; gap: 6px; z-index: 2; }
.treasure { background: rgba(20, 14, 6, .7); backdrop-filter: blur(6px); }
.badges .chip.jade { background: rgba(8, 20, 15, .7); backdrop-filter: blur(6px); }
.shade { position: absolute; inset: auto 0 0 0; height: 40%; background: linear-gradient(transparent, rgba(13,17,15,.95)); }
.body { padding: 4px 18px 16px; margin-top: -18px; position: relative; }
.no { font-size: 11px; letter-spacing: .2em; color: var(--gold-deep); font-family: ui-monospace, monospace; }
.name { font-size: 19px; font-weight: 700; margin: 2px 0 6px; letter-spacing: .08em; }
.meta { display: flex; align-items: center; gap: 8px; color: var(--text-3); font-size: 12.5px; flex-wrap: wrap; min-height: 20px; }
.meta i { width: 3px; height: 3px; border-radius: 50%; background: var(--gold-deep); }
.foot { display: flex; align-items: center; gap: 14px; margin-top: 12px; padding-top: 12px; border-top: 1px dashed rgba(201,164,92,.14); color: var(--text-3); font-size: 12px; }
.foot span { display: inline-flex; align-items: center; gap: 4px; }
.foot .go { margin-left: auto; color: var(--gold); letter-spacing: .1em; transition: gap .3s; }
.acard:hover .go { gap: 8px; }
</style>
