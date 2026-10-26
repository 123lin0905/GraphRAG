<template>
  <div>
    <PageHeader eyebrow="GUIDED TOURS" title="导览路线" sub="经典国宝、面具主题、太阳崇拜、亲子研学与快速打卡 —— 选一条路线，跟随讲解词漫游古蜀。" bg="/static/images/artifacts/shentan.jpg">
      <div class="rec panel">
        <span class="rec-l"><el-icon><MagicStick /></el-icon>智能推荐</span>
        <el-select v-model="interests" multiple collapse-tags placeholder="兴趣标签" style="width: 260px">
          <el-option v-for="t in TAGS" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="minutes" placeholder="可用时长" style="width: 140px" clearable>
          <el-option v-for="m in [30, 60, 90, 120]" :key="m" :label="`${m} 分钟内`" :value="m" />
        </el-select>
        <el-button type="primary" @click="recommend">推荐路线</el-button>
        <el-button v-if="recommended" @click="reset">全部路线</el-button>
      </div>
    </PageHeader>
    <div class="container body">
      <div class="list" v-loading="loading">
        <router-link v-for="(r, i) in list" :key="r.id" :to="`/routes/${r.id}`" class="row panel fade-up" :style="{ animationDelay: i * 0.06 + 's' }">
          <div class="cover"><BronzeImage :src="r.coverUrl" :alt="r.name" ratio="16 / 10" /><span class="idx serif">{{ String(i + 1).padStart(2, '0') }}</span></div>
          <div class="txt">
            <div class="tags"><span class="chip">{{ ROUTE_TYPE[r.routeType] || r.routeType }}</span><span v-if="r.theme" class="chip jade">{{ r.theme }}</span></div>
            <h3>{{ r.name }}</h3>
            <p class="clamp-2">{{ r.description }}</p>
            <div class="meta">
              <span><el-icon><Timer /></el-icon>{{ r.durationMin }} 分钟</span>
              <span><el-icon><Location /></el-icon>{{ r.distanceM }} 米</span>
              <span><el-icon><TrendCharts /></el-icon>强度 {{ '●'.repeat(r.difficulty || 1) }}{{ '○'.repeat(3 - (r.difficulty || 1)) }}</span>
              <span><el-icon><User /></el-icon>{{ r.suitableCrowd }}</span>
              <span><el-icon><Flag /></el-icon>{{ r.useCount }} 人次使用</span>
            </div>
          </div>
          <div class="go"><span class="btn-ghost btn-sm">查看路线 <el-icon><Right /></el-icon></span></div>
        </router-link>
        <el-empty v-if="!loading && !list.length" description="没有匹配的路线，换个条件试试" />
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import BronzeImage from '@/components/BronzeImage.vue'
import { routeApi } from '@/api'
import type { Route } from '@/api/types'
import { ROUTE_TYPE } from '@/utils/format'

const TAGS = ['青铜', '面具', '黄金', '神树', '太阳', '祭祀', '亲子', '考古', '玉器']
const list = ref<Route[]>([])
const loading = ref(false)
const interests = ref<string[]>([])
const minutes = ref<number>()
const recommended = ref(false)
async function reset() { recommended.value = false; loading.value = true; try { list.value = await routeApi.list() } finally { loading.value = false } }
async function recommend() {
  loading.value = true
  try { list.value = await routeApi.recommend(interests.value.join(',') || undefined, minutes.value); recommended.value = true } finally { loading.value = false }
}
onMounted(reset)
</script>
<style scoped>
.rec { display: inline-flex; align-items: center; gap: 12px; padding: 12px 14px; flex-wrap: wrap; }
.rec-l { color: var(--gold-light); font-family: var(--font-serif); display: inline-flex; align-items: center; gap: 6px; letter-spacing: .1em; }
.rec .el-button + .el-button { margin-left: 0; }
.body { padding: 36px 0 90px; }
.list { display: flex; flex-direction: column; gap: 18px; min-height: 200px; }
.row { display: grid; grid-template-columns: 300px 1fr auto; gap: 26px; align-items: center; padding: 14px; color: var(--text-1); transition: transform .4s var(--ease), border-color .4s, box-shadow .4s; }
.row:hover { transform: translateY(-4px); border-color: var(--line-strong); box-shadow: var(--shadow-gold); color: var(--text-1); }
.cover { position: relative; border-radius: 12px; overflow: hidden; }
.idx { position: absolute; left: 12px; bottom: 6px; font-size: 40px; font-weight: 900; color: rgba(243,227,187,.85); text-shadow: 0 4px 20px rgba(0,0,0,.6); }
.tags { display: flex; gap: 8px; }
.txt h3 { font-size: 24px; letter-spacing: .12em; margin: 10px 0 8px; }
.txt p { color: var(--text-2); }
.meta { display: flex; gap: 18px; flex-wrap: wrap; margin-top: 14px; color: var(--text-3); font-size: 13px; }
.meta span { display: inline-flex; align-items: center; gap: 5px; }
.go { padding-right: 14px; }
@media (max-width: 900px) { .row { grid-template-columns: 1fr; } .go { display: none; } }
</style>
