<template>
  <div>
    <PageHeader eyebrow="COLLECTION" title="文物展厅" sub="青铜神器、黄金重器、玉石礼器……按分类、祭祀坑、材质与年代检索三星堆珍藏。" bg="/static/images/artifacts/dalirenxiang.jpg">
      <div class="search">
        <el-input v-model="q.keyword" size="large" placeholder="搜索文物名称、别名或编号，如“神树”“纵目”" clearable @keyup.enter="reload" @clear="reload">
          <template #prefix><el-icon><Search /></el-icon></template>
          <template #append><el-button type="primary" @click="reload">检索</el-button></template>
        </el-input>
      </div>
    </PageHeader>

    <div class="container body">
      <div class="filters panel">
        <div class="frow">
          <span class="fl">祭祀坑</span>
          <div class="pills">
            <button :class="{ on: !q.pitId }" @click="setF('pitId', undefined)">全部</button>
            <button v-for="p in pits" :key="p.value" :class="{ on: q.pitId === p.value }" @click="setF('pitId', p.value)">{{ p.label }}</button>
          </div>
        </div>
        <div class="frow">
          <span class="fl">筛选</span>
          <div class="selects">
            <el-select v-model="q.categoryId" placeholder="文物分类" clearable @change="reload" style="width: 170px">
              <el-option v-for="o in categories" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="q.materialId" placeholder="材质" clearable @change="reload" style="width: 130px">
              <el-option v-for="o in materials" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-select v-model="q.periodId" placeholder="年代 / 文化期" clearable @change="reload" style="width: 210px">
              <el-option v-for="o in periods" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
            <el-checkbox v-model="treasureOnly" @change="reload">仅看镇馆之宝</el-checkbox>
          </div>
          <div class="sort">
            <el-radio-group v-model="q.orderBy" size="small" @change="reload">
              <el-radio-button value="">默认</el-radio-button>
              <el-radio-button value="hot">热度</el-radio-button>
              <el-radio-button value="new">最新</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </div>

      <div class="result-bar">
        <span>共 <b class="gold-text">{{ total }}</b> 件文物</span>
        <el-button v-if="hasFilter" link type="primary" @click="resetAll"><el-icon><RefreshLeft /></el-icon>清空条件</el-button>
      </div>

      <div v-loading="loading" class="grid">
        <ArtifactCard v-for="a in list" :key="a.id" :a="a" class="fade-up" />
      </div>
      <el-empty v-if="!loading && !list.length" description="没有找到符合条件的文物" />
      <div class="pager" v-if="total > q.size!">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="q.size" v-model:current-page="q.current" @current-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import ArtifactCard from '@/components/ArtifactCard.vue'
import { artifactApi, type ArtifactQuery } from '@/api'
import type { Artifact } from '@/api/types'
import { loadDict, type Option } from '@/utils/dicts'

const route = useRoute()
const q = reactive<ArtifactQuery>({ current: 1, size: 12, keyword: (route.query.keyword as string) || '', orderBy: '' })
const treasureOnly = ref(false)
const list = ref<Artifact[]>([])
const total = ref(0)
const loading = ref(false)
const pits = ref<Option[]>([])
const categories = ref<Option[]>([])
const materials = ref<Option[]>([])
const periods = ref<Option[]>([])
const hasFilter = computed(() => !!(q.keyword || q.pitId || q.categoryId || q.materialId || q.periodId || treasureOnly.value))

async function load() {
  loading.value = true
  try {
    const p = await artifactApi.search({ ...q, isTreasure: treasureOnly.value ? 1 : undefined, orderBy: q.orderBy || undefined })
    list.value = p.records
    total.value = p.total
  } finally {
    loading.value = false
  }
}
function reload() { q.current = 1; load() }
function setF(k: 'pitId', v: number | undefined) { q[k] = v; reload() }
function resetAll() {
  Object.assign(q, { keyword: '', pitId: undefined, categoryId: undefined, materialId: undefined, periodId: undefined })
  treasureOnly.value = false
  reload()
}
onMounted(async () => {
  load()
  loadDict('pits').then((o) => (pits.value = o.map((x) => ({ ...x, label: x.label.split(' ')[1] || x.label }))))
  loadDict('categories').then((o) => (categories.value = o))
  loadDict('materials').then((o) => (materials.value = o))
  loadDict('periods').then((o) => (periods.value = o))
})
</script>

<style scoped>
.search { max-width: 640px; }
.search :deep(.el-input-group__append) { background: transparent; box-shadow: none; padding: 0; }
.search :deep(.el-input-group__append .el-button) { height: 40px; border-radius: 0 8px 8px 0; margin: 0; padding: 0 26px; color: #1a140a; background-image: linear-gradient(135deg, #ecd39c, #c9a45c 55%, #a5823f); letter-spacing: .2em; font-weight: 600; }
.body { padding-top: 30px; padding-bottom: 90px; }
.filters { padding: 18px 22px; display: flex; flex-direction: column; gap: 14px; }
.frow { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.fl { color: var(--gold); font-family: var(--font-serif); letter-spacing: .2em; font-size: 14px; flex: none; width: 56px; }
.pills { display: flex; flex-wrap: wrap; gap: 8px; }
.pills button { height: 30px; padding: 0 14px; border-radius: 999px; border: 1px solid var(--line); background: rgba(201,164,92,.03); color: var(--text-2); cursor: pointer; font-size: 13px; transition: all .25s; }
.pills button:hover { border-color: var(--line-strong); color: var(--gold-light); }
.pills button.on { background: var(--grad-gold); color: #1a140a; border-color: transparent; font-weight: 600; }
.selects { display: flex; gap: 12px; flex-wrap: wrap; align-items: center; flex: 1; }
.sort { margin-left: auto; }
.result-bar { display: flex; align-items: center; justify-content: space-between; margin: 26px 2px 16px; color: var(--text-3); }
.result-bar b { font-size: 20px; font-family: var(--font-serif); margin: 0 2px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(270px, 1fr)); gap: 22px; min-height: 200px; }
.pager { display: flex; justify-content: center; margin-top: 40px; }
@media (max-width: 768px) { .sort { margin-left: 0; } .fl { width: auto; } }
</style>
