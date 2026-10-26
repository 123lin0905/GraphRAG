<template>
  <div class="page">
    <div class="container">
      <section class="profile panel">
        <div class="avatar serif">{{ auth.displayName.slice(0, 1) }}</div>
        <div class="p-info">
          <h1>{{ auth.displayName }}</h1>
          <div class="p-meta">
            <span>@{{ auth.user?.username }}</span>
            <span v-for="r in auth.roles" :key="r" class="chip">{{ ROLE[r] || r }}</span>
            <span v-if="auth.user?.createdAt" class="muted">注册于 {{ auth.user.createdAt.slice(0, 10) }}</span>
          </div>
        </div>
        <div class="p-stats">
          <div><b class="serif">{{ favs.length }}</b><span>收藏</span></div>
          <div><b class="serif">{{ reservations.length }}</b><span>预约</span></div>
          <div><b class="serif">{{ reviews.length }}</b><span>评价</span></div>
        </div>
        <div class="p-actions">
          <el-button v-if="auth.isStaff" type="primary" @click="$router.push('/admin')"><el-icon><Monitor /></el-icon>管理后台</el-button>
          <el-button @click="pwdVisible = true"><el-icon><Lock /></el-icon>修改密码</el-button>
        </div>
      </section>

      <section class="panel tabs">
        <el-tabs v-model="tab">
          <el-tab-pane label="我的收藏" name="favorite">
            <div class="fav-grid" v-if="favs.length"><ArtifactCard v-for="a in favs" :key="a.id" :a="a" /></div>
            <el-empty v-else description="还没有收藏文物"><router-link to="/artifacts" class="btn-ghost btn-sm">去文物展厅看看</router-link></el-empty>
          </el-tab-pane>
          <el-tab-pane label="我的预约" name="reservation">
            <div class="res-list" v-if="reservations.length">
              <div v-for="r in reservations" :key="r.id" class="res" :class="r.status.toLowerCase()">
                <div class="res-date"><b class="serif">{{ r.visitDate.slice(8, 10) }}</b><small>{{ r.visitDate.slice(0, 7) }}</small></div>
                <div class="res-info">
                  <div class="res-no">{{ r.reservationNo }}</div>
                  <div class="res-t">{{ r.timeSlot }} · {{ r.visitorCount }} 人 · {{ r.contactName }} {{ r.contactPhone }}</div>
                  <div v-if="r.checkInTime" class="muted">核销时间：{{ r.checkInTime }}</div>
                </div>
                <el-tag :type="RESERVATION_STATUS[r.status]?.type">{{ RESERVATION_STATUS[r.status]?.label || r.status }}</el-tag>
                <el-button v-if="r.status === 'PENDING' || r.status === 'CONFIRMED'" size="small" @click="cancel(r.id)">取消预约</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无预约"><router-link to="/reservation" class="btn-ghost btn-sm">立即预约</router-link></el-empty>
          </el-tab-pane>
          <el-tab-pane label="我的评价" name="review">
            <div v-for="r in reviews" :key="r.id" class="rv">
              <div class="rv-h"><span class="chip">{{ TARGET[r.targetType] || r.targetType }}</span><b>{{ r.targetName || '#' + r.targetId }}</b><el-rate :model-value="r.rating" disabled size="small" />
                <el-tag size="small" :type="r.status === 1 ? 'success' : r.status === 2 ? 'danger' : 'warning'">{{ ['待审核', '已发布', '已屏蔽'][r.status] }}</el-tag>
                <span class="muted t">{{ r.createdAt?.slice(0, 16) }}</span>
                <el-button link type="danger" size="small" @click="removeReview(r.id)">删除</el-button>
              </div>
              <p>{{ r.content }}</p>
              <div v-if="r.reply" class="reply">馆方回复：{{ r.reply }}</div>
            </div>
            <el-empty v-if="!reviews.length" description="还没有发表评价" />
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>

    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form :model="pwd" label-position="top">
        <el-form-item label="原密码"><el-input v-model="pwd.old" type="password" show-password /></el-form-item>
        <el-form-item label="新密码（6-32 位）"><el-input v-model="pwd.new" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer><el-button @click="pwdVisible = false">取消</el-button><el-button type="primary" @click="changePwd">确认修改</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ArtifactCard from '@/components/ArtifactCard.vue'
import { authApi, favoriteApi, reservationApi, reviewApi } from '@/api'
import type { Artifact, Reservation, Review } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { RESERVATION_STATUS } from '@/utils/format'

const ROLE: Record<string, string> = { ADMIN: '系统管理员', CURATOR: '馆员', VISITOR: '游客' }
const TARGET: Record<string, string> = { ARTIFACT: '文物', ROUTE: '路线', MUSEUM: '博物馆', QA: '问答' }
const auth = useAuthStore()
const route = useRoute()
const tab = ref((route.query.tab as string) || 'favorite')
const favs = ref<Artifact[]>([])
const reservations = ref<Reservation[]>([])
const reviews = ref<Review[]>([])
const pwdVisible = ref(false)
const pwd = reactive({ old: '', new: '' })

async function load() {
  const [f, r, v] = await Promise.allSettled([favoriteApi.artifacts(), reservationApi.my(), reviewApi.my()])
  if (f.status === 'fulfilled') favs.value = f.value.records
  if (r.status === 'fulfilled') reservations.value = r.value.records
  if (v.status === 'fulfilled') reviews.value = v.value.records
}
async function cancel(id: number) {
  await ElMessageBox.confirm('确定取消该预约？', '取消预约', { type: 'warning' })
  await reservationApi.cancel(id)
  ElMessage.success('已取消')
  load()
}
async function removeReview(id: number) {
  await ElMessageBox.confirm('确定删除该评价？', '提示', { type: 'warning' })
  await reviewApi.remove(id)
  load()
}
async function changePwd() {
  if (pwd.new.length < 6) return ElMessage.warning('新密码至少 6 位')
  await authApi.changePassword(pwd.old, pwd.new)
  ElMessage.success('密码已修改')
  pwdVisible.value = false
}
onMounted(() => { auth.fetchMe(); load() })
</script>
<style scoped>
.profile { display: flex; align-items: center; gap: 24px; padding: 28px 32px; flex-wrap: wrap;
  background: radial-gradient(600px 200px at 0% 0%, rgba(201,164,92,.14), transparent 70%), var(--grad-panel); }
.avatar { width: 84px; height: 84px; border-radius: 50%; display: grid; place-items: center; font-size: 36px; color: #1a140a; background: var(--grad-gold); box-shadow: 0 0 0 6px rgba(201,164,92,.14); }
.p-info h1 { font-size: 28px; letter-spacing: .14em; }
.p-meta { display: flex; gap: 10px; align-items: center; margin-top: 6px; color: var(--text-3); flex-wrap: wrap; }
.p-stats { display: flex; gap: 32px; margin-left: auto; }
.p-stats div { text-align: center; }
.p-stats b { display: block; font-size: 28px; color: var(--gold-light); }
.p-stats span { color: var(--text-3); font-size: 12px; }
.p-actions { display: flex; gap: 10px; }
.tabs { margin-top: 22px; padding: 10px 26px 26px; min-height: 360px; }
.fav-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 18px; }
.res-list { display: flex; flex-direction: column; gap: 12px; }
.res { display: flex; align-items: center; gap: 18px; padding: 14px 18px; border-radius: 14px; border: 1px solid var(--line); background: rgba(201,164,92,.03); }
.res.cancelled { opacity: .55; }
.res-date { text-align: center; width: 64px; padding-right: 16px; border-right: 1px dashed var(--line-strong); }
.res-date b { display: block; font-size: 28px; color: var(--gold-light); line-height: 1.1; }
.res-date small { color: var(--text-3); font-size: 11px; }
.res-info { flex: 1; }
.res-no { font-family: ui-monospace, monospace; color: var(--text-1); }
.res-t { color: var(--text-2); font-size: 13px; }
.rv { padding: 14px 0; border-bottom: 1px dashed var(--line); }
.rv-h { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.rv-h .t { margin-left: auto; font-size: 12px; }
.rv p { color: var(--text-2); margin-top: 6px; }
.reply { margin-top: 6px; color: #9fd4bd; font-size: 13px; }
</style>
