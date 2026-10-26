<template>
  <div>
    <PageHeader eyebrow="RESERVATION" title="参观预约" sub="实名预约、分时入馆。选择日期与时段，提交后凭预约单号核销入馆。" bg="/static/images/artifacts/jinmianzhao.jpg" />
    <div class="container body">
      <section class="main panel panel-pad">
        <h3 class="h"><span>01</span>选择参观日期</h3>
        <div class="dates">
          <button v-for="d in dates" :key="d.date" class="date" :class="{ on: form.visitDate === d.date, off: d.closed }" :disabled="d.closed" @click="pickDate(d.date)">
            <small>{{ d.week }}</small>
            <b class="serif">{{ d.day }}</b>
            <small>{{ d.closed ? '闭馆' : d.month + '月' }}</small>
          </button>
        </div>

        <h3 class="h"><span>02</span>选择入馆时段</h3>
        <div class="slots" v-loading="slotLoading">
          <button v-for="s in slots" :key="s.timeSlot" class="slot" :class="{ on: form.timeSlot === s.timeSlot, full: s.remaining <= 0 }" :disabled="s.remaining <= 0" @click="form.timeSlot = s.timeSlot">
            <b>{{ s.timeSlot }}</b>
            <div class="bar"><i :style="{ width: Math.min(100, (s.booked / s.capacity) * 100) + '%' }"></i></div>
            <small>{{ s.remaining > 0 ? `余 ${s.remaining} / ${s.capacity}` : '已约满' }}</small>
          </button>
        </div>

        <h3 class="h"><span>03</span>填写预约信息</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form">
          <el-form-item label="联系人" prop="contactName"><el-input v-model="form.contactName" placeholder="真实姓名" /></el-form-item>
          <el-form-item label="手机号" prop="contactPhone"><el-input v-model="form.contactPhone" placeholder="11 位手机号" maxlength="11" /></el-form-item>
          <el-form-item label="参观人数" prop="visitorCount"><el-input-number v-model="form.visitorCount" :min="1" :max="10" style="width: 100%" /></el-form-item>
          <el-form-item label="意向导览路线">
            <el-select v-model="form.routeId" placeholder="可选" clearable style="width: 100%">
              <el-option v-for="r in routes" :key="r.id" :label="r.name" :value="r.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="人工讲解"><el-switch v-model="needGuide" active-text="需要" inactive-text="不需要" /></el-form-item>
          <el-form-item label="备注" class="span2"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="如：携带老人儿童、需要轮椅等" /></el-form-item>
        </el-form>
        <div class="submit">
          <div class="sum">
            <span>{{ form.visitDate }}</span><i></i><span>{{ form.timeSlot || '未选时段' }}</span><i></i><span>{{ form.visitorCount }} 人</span>
          </div>
          <button class="btn-gold" :disabled="submitting" @click="submit"><el-icon><Tickets /></el-icon>{{ auth.isLogin ? '提交预约' : '登录后预约' }}</button>
        </div>
      </section>

      <aside class="side">
        <div class="panel panel-pad notice">
          <h3><el-icon><InfoFilled /></el-icon>参观须知</h3>
          <ul>
            <li>开放时间：09:00 - 17:00（16:00 停止入馆），每周一闭馆（法定节假日除外）。</li>
            <li>每个时段限额预约，每单最多 10 人，需提前至少 1 天预约。</li>
            <li>入馆时出示预约单号，由工作人员核销。</li>
            <li>馆内禁止触摸文物、禁止使用闪光灯拍照。</li>
            <li>如行程变更，请在“我的 → 我的预约”中及时取消。</li>
          </ul>
        </div>
        <div class="panel panel-pad tip">
          <SunLogo :size="40" />
          <div><b class="serif">先线上，后线下</b><p>预约前可先在 3D 数字展厅与 AI 导览中“预习”重点文物，参观更有收获。</p></div>
        </div>
      </aside>
    </div>

    <el-dialog v-model="okVisible" width="440px" :show-close="true" class="no-head">
      <div class="ok" v-if="result">
        <div class="ok-icon"><el-icon :size="34"><Select /></el-icon></div>
        <h2 class="serif">预约成功</h2>
        <div class="ticket">
          <div class="qr"><i v-for="i in 441" :key="i" :class="{ b: qrBit(i) }"></i></div>
          <div class="tk-info">
            <div><label>预约单号</label><b>{{ result.reservationNo }}</b></div>
            <div><label>参观时间</label><b>{{ result.visitDate }} {{ result.timeSlot }}</b></div>
            <div><label>人数</label><b>{{ result.visitorCount }} 人</b></div>
          </div>
        </div>
        <el-button type="primary" @click="$router.push('/me?tab=reservation')">查看我的预约</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import SunLogo from '@/components/SunLogo.vue'
import { reservationApi, routeApi } from '@/api'
import type { Reservation, Route, Slot } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { today } from '@/utils/format'

const auth = useAuthStore()
const router = useRouter()
const WEEK = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const dates = Array.from({ length: 10 }, (_, i) => {
  const s = today(i + 1)
  const d = new Date(s + 'T00:00:00')
  return { date: s, day: d.getDate(), month: d.getMonth() + 1, week: WEEK[d.getDay()], closed: d.getDay() === 1 }
})
const form = reactive({ museumId: 1, visitDate: dates.find((d) => !d.closed)!.date, timeSlot: '', visitorCount: 2, contactName: auth.user?.nickname || '', contactPhone: auth.user?.phone || '', routeId: undefined as number | undefined, remark: '' })
const needGuide = ref(false)
const slots = ref<Slot[]>([])
const routes = ref<Route[]>([])
const slotLoading = ref(false)
const submitting = ref(false)
const okVisible = ref(false)
const result = ref<Reservation>()
const formRef = ref<FormInstance>()
const rules: FormRules = {
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}
async function pickDate(d: string) {
  form.visitDate = d
  form.timeSlot = ''
  slotLoading.value = true
  try { slots.value = await reservationApi.slots(d) } finally { slotLoading.value = false }
}
/** 演示用伪二维码（21×21，含三个定位角），仅作视觉示意 */
function qrBit(i: number) {
  const x = (i - 1) % 21, y = Math.floor((i - 1) / 21)
  for (const [ox, oy] of [[0, 0], [14, 0], [0, 14]]) {
    const dx = x - ox, dy = y - oy
    if (dx >= -1 && dx <= 7 && dy >= -1 && dy <= 7) {
      if (dx < 0 || dy < 0 || dx > 6 || dy > 6) return false
      const d = Math.max(Math.abs(dx - 3), Math.abs(dy - 3))
      return d === 3 || d <= 1
    }
  }
  const seed = (result.value?.reservationNo || 'x').split('').reduce((a, c) => (a * 31 + c.charCodeAt(0)) % 1000003, 7)
  const h = Math.sin(x * 12.9898 + y * 78.233 + seed) * 43758.5453
  return h - Math.floor(h) > 0.52
}
async function submit() {
  if (!auth.isLogin) { router.push({ path: '/login', query: { redirect: '/reservation' } }); return }
  if (!form.timeSlot) { ElMessage.warning('请选择入馆时段'); return }
  await formRef.value?.validate()
  submitting.value = true
  try {
    result.value = await reservationApi.create({ ...form, needGuide: needGuide.value ? 1 : 0 })
    okVisible.value = true
    pickDate(form.visitDate)
  } finally {
    submitting.value = false
  }
}
onMounted(() => { pickDate(form.visitDate); routeApi.list().then((r) => (routes.value = r)) })
</script>
<style scoped>
.body { display: grid; grid-template-columns: 1fr 340px; gap: 22px; padding: 34px 0 90px; align-items: start; }
.h { display: flex; align-items: center; gap: 12px; font-size: 18px; letter-spacing: .12em; margin: 8px 0 16px; color: var(--text-1); }
.h span { font-size: 13px; color: var(--gold); border: 1px solid var(--line-strong); border-radius: 8px; padding: 0 8px; font-family: ui-monospace, monospace; letter-spacing: 0; }
.h:not(:first-child) { margin-top: 30px; }
.dates { display: grid; grid-template-columns: repeat(10, 1fr); gap: 8px; }
.date { display: flex; flex-direction: column; align-items: center; padding: 10px 0; border-radius: 12px; border: 1px solid var(--line); background: rgba(201,164,92,.02); color: var(--text-2); cursor: pointer; transition: all .25s; }
.date b { font-size: 24px; color: var(--text-1); line-height: 1.3; }
.date small { font-size: 11.5px; color: var(--text-3); }
.date:hover:not(:disabled) { border-color: var(--line-strong); }
.date.on { background: var(--grad-gold); border-color: transparent; }
.date.on b, .date.on small { color: #1a140a; }
.date.off { opacity: .35; cursor: not-allowed; }
.slots { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; min-height: 80px; }
.slot { padding: 14px; border-radius: 12px; border: 1px solid var(--line); background: rgba(201,164,92,.02); color: var(--text-1); cursor: pointer; text-align: left; transition: all .25s; }
.slot b { font-family: var(--font-serif); font-size: 16px; letter-spacing: .05em; }
.slot .bar { height: 4px; border-radius: 4px; background: rgba(201,164,92,.1); margin: 10px 0 6px; overflow: hidden; }
.slot .bar i { display: block; height: 100%; background: var(--grad-gold); }
.slot small { color: var(--text-3); font-size: 12px; }
.slot.on { border-color: var(--gold); background: rgba(201,164,92,.12); box-shadow: 0 0 0 3px rgba(201,164,92,.12); }
.slot.full { opacity: .4; cursor: not-allowed; }
.form { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0 18px; }
.form .span2 { grid-column: span 3; }
.submit { display: flex; align-items: center; justify-content: space-between; gap: 18px; margin-top: 10px; padding-top: 20px; border-top: 1px solid var(--line); flex-wrap: wrap; }
.sum { display: flex; align-items: center; gap: 10px; color: var(--gold-light); font-family: var(--font-serif); letter-spacing: .08em; }
.sum i { width: 4px; height: 4px; transform: rotate(45deg); background: var(--gold-deep); }
.btn-gold:disabled { opacity: .6; }
.side { display: flex; flex-direction: column; gap: 16px; position: sticky; top: calc(var(--header-h) + 16px); }
.notice h3 { font-size: 17px; color: var(--gold-light); display: flex; align-items: center; gap: 8px; letter-spacing: .12em; }
.notice ul { padding-left: 18px; margin: 12px 0 0; color: var(--text-2); font-size: 13px; line-height: 2; }
.tip { display: flex; gap: 14px; align-items: flex-start; }
.tip b { color: var(--gold-light); letter-spacing: .12em; }
.tip p { color: var(--text-3); font-size: 13px; margin-top: 4px; }
.ok { text-align: center; }
.ok-icon { width: 64px; height: 64px; border-radius: 50%; margin: 0 auto; display: grid; place-items: center; background: var(--grad-gold); color: #1a140a; box-shadow: 0 0 0 8px rgba(201,164,92,.15); }
.ok h2 { margin: 16px 0; letter-spacing: .2em; color: var(--gold-light); }
.ticket { display: flex; gap: 18px; align-items: center; padding: 18px; border: 1px dashed var(--line-strong); border-radius: 14px; margin-bottom: 18px; text-align: left; }
.qr { display: grid; grid-template-columns: repeat(21, 1fr); grid-template-rows: repeat(21, 1fr); width: 110px; height: 110px; padding: 7px; background: #f3e3bb; border-radius: 8px; flex: none; }
.qr i.b { background: #1a140a; }
.tk-info { display: flex; flex-direction: column; gap: 6px; }
.tk-info label { display: block; font-size: 11px; color: var(--text-3); }
.tk-info b { font-family: ui-monospace, monospace; color: var(--text-1); font-weight: 500; }
@media (max-width: 1000px) { .body { grid-template-columns: 1fr; } .side { position: static; } .dates { grid-template-columns: repeat(5, 1fr); } .slots { grid-template-columns: repeat(2, 1fr); } .form { grid-template-columns: 1fr; } .form .span2 { grid-column: span 1; } }
</style>
