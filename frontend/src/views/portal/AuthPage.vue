<template>
  <div class="auth">
    <div class="art">
      <div class="art-bg"></div>
      <div class="art-txt">
        <router-link to="/" class="brand"><SunLogo :size="44" /><span class="serif">三星堆 · 数字孪生</span></router-link>
        <h1 class="serif">与三千年前的<br />古蜀文明对话</h1>
        <p>3D 高斯泼溅 · 知识图谱 · GraphRAG 智能导览</p>
      </div>
    </div>
    <div class="form-side">
      <div class="card fade-up">
        <div class="switch">
          <button :class="{ on: isLogin }" @click="$router.replace({ path: '/login', query: $route.query })">登录</button>
          <button :class="{ on: !isLogin }" @click="$router.replace({ path: '/register', query: $route.query })">注册</button>
        </div>
        <h2 class="serif">{{ isLogin ? '欢迎回来' : '创建游客账号' }}</h2>
        <p class="muted">{{ isLogin ? '登录后可预约参观、收藏文物、保存问答记录' : '注册后即可使用预约、收藏与评价等观众服务' }}</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent="submit">
          <el-form-item label="用户名" prop="username"><el-input v-model="form.username" placeholder="4-20 位字母、数字或下划线" :prefix-icon="User" /></el-form-item>
          <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password placeholder="6-32 位密码" :prefix-icon="Lock" @keyup.enter="submit" /></el-form-item>
          <template v-if="!isLogin">
            <el-form-item label="昵称"><el-input v-model="form.nickname" placeholder="选填" /></el-form-item>
            <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" placeholder="选填" maxlength="11" /></el-form-item>
          </template>
          <el-button type="primary" native-type="submit" :loading="loading" class="submit">{{ isLogin ? '登 录' : '注 册' }}</el-button>
        </el-form>
        <div v-if="isLogin" class="demo">
          <span class="muted">演示账号：</span>
          <button v-for="d in demos" :key="d.u" @click="fill(d.u, d.p)">{{ d.label }}</button>
        </div>
        <router-link to="/" class="back"><el-icon><Back /></el-icon>返回首页</router-link>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import SunLogo from '@/components/SunLogo.vue'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isLogin = computed(() => route.name === 'login')
const loading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ username: '', password: '', nickname: '', phone: '' })
const demos = [
  { u: 'admin', p: 'admin123', label: '管理员' },
  { u: 'curator', p: 'curator123', label: '馆员' },
  { u: 'visitor', p: 'visitor123', label: '游客' }
]
const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, ...(isLogin.value ? [] : [{ pattern: /^\w{4,20}$/, message: '4-20 位字母、数字或下划线', trigger: 'blur' }])],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 32, message: '6-32 位', trigger: 'blur' }],
  phone: [{ pattern: /^$|^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }]
}))
function fill(u: string, p: string) { form.username = u; form.password = p }
async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    if (!isLogin.value) {
      await authApi.register({ username: form.username, password: form.password, nickname: form.nickname || undefined, phone: form.phone || undefined })
      ElMessage.success('注册成功，已为你自动登录')
    }
    const u = await auth.login(form.username, form.password)
    if (isLogin.value) ElMessage.success(`欢迎，${u.nickname || u.username}`)
    const redirect = route.query.redirect as string | undefined
    const staff = u.roles.includes('ADMIN') || u.roles.includes('CURATOR')
    router.replace(redirect || (staff ? '/admin' : '/'))
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.auth { display: grid; grid-template-columns: 1.2fr 1fr; min-height: 100vh; background: var(--bg-1); }
.art { position: relative; overflow: hidden; }
.art-bg { position: absolute; inset: 0; background: url('/static/images/artifacts/jinmianzhao.jpg') center / cover; animation: heroZoom 30s ease-in-out infinite alternate; }
@keyframes heroZoom { from { transform: scale(1.02); } to { transform: scale(1.12); } }
.art::after { content: ''; position: absolute; inset: 0; background: linear-gradient(90deg, rgba(6,8,7,.3), rgba(6,8,7,.6) 70%, var(--bg-1)), linear-gradient(0deg, rgba(6,8,7,.9), transparent 50%); }
.art-txt { position: absolute; z-index: 2; left: 56px; bottom: 64px; right: 56px; }
.brand { position: fixed; top: 28px; left: 40px; display: flex; align-items: center; gap: 10px; color: var(--gold-light); font-size: 18px; letter-spacing: .16em; z-index: 3; }
.art h1 { font-size: clamp(32px, 3.6vw, 54px); line-height: 1.35; letter-spacing: .14em; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
.art p { color: var(--text-2); margin-top: 14px; letter-spacing: .3em; }
.form-side { display: flex; align-items: center; justify-content: center; padding: 40px 24px; background: radial-gradient(600px 400px at 70% 20%, rgba(36,57,45,.4), transparent 70%); }
.card { width: min(420px, 100%); }
.switch { display: inline-flex; padding: 4px; border-radius: 999px; border: 1px solid var(--line); background: rgba(0,0,0,.25); margin-bottom: 28px; }
.switch button { height: 34px; padding: 0 24px; border-radius: 999px; border: 0; background: none; color: var(--text-3); cursor: pointer; font-family: var(--font-serif); letter-spacing: .2em; font-size: 14px; }
.switch button.on { background: var(--grad-gold); color: #1a140a; }
.card h2 { font-size: 30px; letter-spacing: .16em; color: var(--text-1); }
.card > p { margin: 8px 0 26px; font-size: 13.5px; }
.submit { width: 100%; height: 48px; font-size: 16px; letter-spacing: .4em; margin-top: 8px; border-radius: 12px; }
.demo { margin-top: 20px; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 13px; }
.demo button { border: 1px solid var(--line); background: rgba(201,164,92,.05); color: var(--gold-light); border-radius: 999px; padding: 3px 14px; cursor: pointer; font-size: 12.5px; }
.demo button:hover { border-color: var(--gold); }
.back { display: inline-flex; align-items: center; gap: 6px; margin-top: 28px; color: var(--text-3); font-size: 13px; }
@media (max-width: 900px) { .auth { grid-template-columns: 1fr; } .art { display: none; } .brand { position: static; } }
</style>
