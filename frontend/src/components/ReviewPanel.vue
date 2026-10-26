<template>
  <div class="rp">
    <div class="rp-side">
      <div class="score">
        <div class="big serif">{{ summary?.avgRating ?? '—' }}</div>
        <el-rate :model-value="summary?.avgRating || 0" disabled />
        <div class="muted">{{ summary?.count ?? 0 }} 条评价</div>
      </div>
      <div class="bars">
        <div v-for="s in [5, 4, 3, 2, 1]" :key="s" class="bar">
          <span>{{ s }}星</span>
          <div class="track"><i :style="{ width: pct(s) + '%' }"></i></div>
          <em>{{ starCount(s) }}</em>
        </div>
      </div>
      <div class="write">
        <template v-if="auth.isLogin">
          <el-rate v-model="form.rating" />
          <el-input v-model="form.content" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="分享你的观展感受…" />
          <el-button type="primary" :loading="posting" @click="submit" style="width: 100%">发表评价</el-button>
        </template>
        <div v-else class="login-tip">
          <router-link :to="{ path: '/login', query: { redirect: $route.fullPath } }">登录</router-link> 后即可发表评价
        </div>
      </div>
    </div>
    <div class="rp-list">
      <div v-for="r in list" :key="r.id" class="rv">
        <span class="av">{{ (r.nickname || '游').slice(0, 1) }}</span>
        <div class="rv-body">
          <div class="rv-head">
            <b>{{ r.nickname || '匿名观众' }}</b>
            <el-rate :model-value="r.rating" disabled size="small" />
            <span class="muted t">{{ r.createdAt?.slice(0, 16) }}</span>
          </div>
          <p>{{ r.content }}</p>
          <div v-if="r.reply" class="reply"><b>馆方回复：</b>{{ r.reply }}</div>
          <button class="like" @click="like(r)"><el-icon><Pointer /></el-icon>{{ r.likeCount || 0 }}</button>
        </div>
      </div>
      <el-empty v-if="!list.length" :image-size="70" description="还没有评价，来写第一条吧" />
      <el-pagination v-if="total > 8" small layout="prev, pager, next" :total="total" :page-size="8" v-model:current-page="page" @current-change="loadList" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { reviewApi } from '@/api'
import type { Review } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{ targetType: string; targetId: number }>()
const emit = defineEmits<{ (e: 'changed'): void }>()
const auth = useAuthStore()
const summary = ref<{ count: number; avgRating: number; stars: Record<string, number> }>()
const list = ref<Review[]>([])
const total = ref(0)
const page = ref(1)
const posting = ref(false)
const form = reactive({ rating: 5, content: '' })

function starCount(s: number) { return summary.value?.stars?.[s] ?? summary.value?.stars?.[String(s)] ?? 0 }
function pct(s: number) { const c = summary.value?.count || 0; return c ? Math.round((starCount(s) / c) * 100) : 0 }
async function loadList() {
  const p = await reviewApi.target(props.targetType, props.targetId, page.value, 8)
  list.value = p.records
  total.value = p.total
}
async function loadSummary() { summary.value = await reviewApi.summary(props.targetType, props.targetId) }
async function submit() {
  if (!form.content.trim()) { ElMessage.warning('请填写评价内容'); return }
  posting.value = true
  try {
    const r = await reviewApi.publish({ targetType: props.targetType, targetId: props.targetId, rating: form.rating, content: form.content })
    ElMessage.success(r.status === 0 ? '提交成功，审核通过后展示' : '评价已发布')
    form.content = ''
    await Promise.all([loadList(), loadSummary()])
    emit('changed')
  } finally {
    posting.value = false
  }
}
async function like(r: Review) {
  await reviewApi.like(r.id)
  r.likeCount = (r.likeCount || 0) + 1
}
onMounted(() => { loadList(); loadSummary() })
</script>
<style scoped>
.rp { display: grid; grid-template-columns: 300px 1fr; gap: 36px; padding-top: 6px; }
.score { text-align: center; padding: 18px; border-radius: 14px; background: radial-gradient(circle at 50% 0%, rgba(201,164,92,.14), transparent 70%); border: 1px solid var(--line); }
.big { font-size: 52px; font-weight: 900; color: var(--gold-light); line-height: 1.1; }
.bars { margin: 18px 0; display: flex; flex-direction: column; gap: 8px; }
.bar { display: flex; align-items: center; gap: 10px; font-size: 12px; color: var(--text-3); }
.track { flex: 1; height: 6px; border-radius: 6px; background: rgba(201,164,92,.08); overflow: hidden; }
.track i { display: block; height: 100%; background: var(--grad-gold); border-radius: 6px; transition: width .8s var(--ease); }
.bar em { font-style: normal; width: 22px; text-align: right; }
.write { display: flex; flex-direction: column; gap: 10px; }
.login-tip { text-align: center; padding: 18px; border: 1px dashed var(--line-strong); border-radius: 12px; color: var(--text-3); }
.rv { display: flex; gap: 14px; padding: 16px 0; border-bottom: 1px dashed var(--line); }
.av { flex: none; width: 40px; height: 40px; border-radius: 50%; display: grid; place-items: center; background: linear-gradient(135deg, var(--patina-500), var(--patina-900)); color: var(--gold-pale); font-family: var(--font-serif); border: 1px solid var(--line-strong); }
.rv-body { flex: 1; min-width: 0; }
.rv-head { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.rv-head .t { margin-left: auto; font-size: 12px; }
.rv p { color: var(--text-2); margin-top: 6px; }
.reply { margin-top: 8px; padding: 8px 12px; border-radius: 8px; background: rgba(111,174,149,.07); color: var(--text-2); font-size: 13px; }
.reply b { color: #9fd4bd; font-weight: 500; }
.like { margin-top: 8px; background: none; border: 1px solid var(--line); color: var(--text-3); border-radius: 999px; padding: 2px 12px; cursor: pointer; display: inline-flex; align-items: center; gap: 4px; font-size: 12px; }
.like:hover { color: var(--gold-light); border-color: var(--line-strong); }
@media (max-width: 900px) { .rp { grid-template-columns: 1fr; } }
</style>
