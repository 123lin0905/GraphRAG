<template>
  <div class="portal" :class="{ immersive: route.name === 'hall3d' }">
    <header class="hdr" :class="{ solid: scrolled || route.name !== 'home' }">
      <div class="hdr-inner">
        <router-link to="/" class="brand">
          <SunLogo :size="34" />
          <div class="brand-txt">
            <b class="serif">三星堆 · 数字孪生</b>
            <small>SANXINGDUI DIGITAL TWIN</small>
          </div>
        </router-link>
        <nav class="nav">
          <router-link v-for="n in navs" :key="n.to" :to="n.to" class="nav-a" :class="{ active: isActive(n.to) }">
            {{ n.label }}
          </router-link>
        </nav>
        <div class="hdr-right">
          <router-link to="/guide" class="ask-btn"><el-icon><ChatDotRound /></el-icon><span>问导览</span></router-link>
          <template v-if="auth.isLogin">
            <el-dropdown trigger="click" @command="onCmd">
              <div class="user">
                <span class="avatar">{{ auth.displayName.slice(0, 1) }}</span>
                <span class="uname">{{ auth.displayName }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="me" icon="User">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="auth.isStaff" command="admin" icon="Monitor">管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" icon="SwitchButton" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <router-link v-else :to="{ path: '/login', query: { redirect: route.fullPath } }" class="btn-ghost btn-sm">登录 / 注册</router-link>
          <button class="burger" @click="drawer = true" aria-label="菜单"><el-icon :size="22"><Menu /></el-icon></button>
        </div>
      </div>
    </header>

    <el-drawer v-model="drawer" direction="rtl" size="260px" :with-header="false">
      <div class="m-nav">
        <SunLogo :size="40" />
        <router-link v-for="n in navs" :key="n.to" :to="n.to" @click="drawer = false" :class="{ active: isActive(n.to) }">{{ n.label }}</router-link>
        <router-link v-if="auth.isLogin" to="/me" @click="drawer = false">个人中心</router-link>
        <router-link v-if="auth.isStaff" to="/admin" @click="drawer = false">管理后台</router-link>
      </div>
    </el-drawer>

    <main>
      <router-view v-slot="{ Component }">
        <transition name="route-fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>

    <footer v-if="!['hall3d', 'guide', 'graph'].includes(route.name as string)" class="ftr">
      <div class="container ftr-inner">
        <div class="ftr-brand">
          <SunLogo :size="44" />
          <div>
            <div class="serif ftr-title">沉睡三千年 · 一醒惊天下</div>
            <p class="muted">基于 3D 高斯泼溅与 GraphRAG 的三星堆文物数字孪生与智能导览系统</p>
          </div>
        </div>
        <div class="ftr-cols">
          <div>
            <h4>探索</h4>
            <router-link to="/artifacts">文物展厅</router-link>
            <router-link to="/hall3d">3D 数字展厅</router-link>
            <router-link to="/graph">知识图谱</router-link>
          </div>
          <div>
            <h4>服务</h4>
            <router-link to="/guide">智能导览问答</router-link>
            <router-link to="/routes">导览路线</router-link>
            <router-link to="/reservation">参观预约</router-link>
          </div>
          <div>
            <h4>技术</h4>
            <span>3D Gaussian Splatting</span>
            <span>Knowledge Graph · GraphRAG</span>
            <span>Vue 3 · Spring Boot 3</span>
          </div>
        </div>
      </div>
      <div class="container ftr-bottom">
        <span>© {{ new Date().getFullYear() }} 三星堆文物数字孪生与智能导览系统 · 毕业设计作品</span>
        <span>文物图片为 AI 生成示意图，仅供演示</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SunLogo from '@/components/SunLogo.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const scrolled = ref(false)
const drawer = ref(false)
const navs = [
  { to: '/', label: '首页' },
  { to: '/artifacts', label: '文物展厅' },
  { to: '/hall3d', label: '3D数字展厅' },
  { to: '/graph', label: '知识图谱' },
  { to: '/guide', label: '智能导览' },
  { to: '/routes', label: '导览路线' },
  { to: '/reservation', label: '参观预约' }
]
function isActive(to: string) {
  return to === '/' ? route.path === '/' : route.path.startsWith(to)
}
function onScroll() { scrolled.value = window.scrollY > 40 }
function onCmd(c: string) {
  if (c === 'me') router.push('/me')
  else if (c === 'admin') router.push('/admin')
  else if (c === 'logout') { auth.logout(); router.push('/') }
}
onMounted(() => { window.addEventListener('scroll', onScroll, { passive: true }); onScroll(); auth.fetchMe() })
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.portal { min-height: 100vh; background:
  radial-gradient(1200px 600px at 100% 0%, rgba(36,57,45,.25), transparent 60%),
  radial-gradient(900px 500px at 0% 100%, rgba(201,164,92,.05), transparent 60%), var(--bg-1); }
.hdr { position: fixed; top: 0; left: 0; right: 0; z-index: 100; height: var(--header-h); transition: background .4s, border-color .4s, backdrop-filter .4s; border-bottom: 1px solid transparent; }
.hdr.solid { background: rgba(8, 11, 9, .78); backdrop-filter: blur(16px) saturate(1.2); border-bottom-color: var(--line); }
.hdr-inner { height: 100%; width: min(1440px, calc(100% - 40px)); margin: 0 auto; display: flex; align-items: center; gap: 28px; }
.brand { display: flex; align-items: center; gap: 10px; color: var(--text-1); flex: none; }
.brand-txt { display: flex; flex-direction: column; line-height: 1.15; }
.brand-txt b { font-size: 17px; letter-spacing: .14em; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
.brand-txt small { font-size: 9px; letter-spacing: .32em; color: var(--text-3); margin-top: 3px; }
.nav { display: flex; gap: 4px; flex: 1; justify-content: center; }
.nav-a { position: relative; padding: 8px 14px; color: var(--text-2); font-size: 14.5px; letter-spacing: .1em; font-family: var(--font-serif); font-weight: 600; transition: color .25s; white-space: nowrap; }
.nav-a::after { content: ''; position: absolute; left: 50%; bottom: 2px; width: 0; height: 2px; background: var(--grad-gold); transform: translateX(-50%); transition: width .3s var(--ease); border-radius: 2px; }
.nav-a:hover { color: var(--gold-light); }
.nav-a.active { color: var(--gold-light); }
.nav-a.active::after { width: 22px; }
.hdr-right { display: flex; align-items: center; gap: 12px; flex: none; }
.ask-btn { display: inline-flex; align-items: center; gap: 6px; height: 34px; padding: 0 14px; border-radius: 999px; color: #1a140a; background: var(--grad-gold); font-size: 13px; font-weight: 600; letter-spacing: .08em; box-shadow: 0 6px 20px rgba(201,164,92,.25); }
.ask-btn:hover { color: #1a140a; filter: brightness(1.08); }
.user { display: flex; align-items: center; gap: 8px; cursor: pointer; color: var(--text-2); padding: 4px 8px; border-radius: 999px; border: 1px solid var(--line); outline: none; }
.avatar { width: 26px; height: 26px; border-radius: 50%; display: grid; place-items: center; background: linear-gradient(135deg, var(--patina-500), var(--patina-700)); color: var(--gold-pale); font-size: 13px; font-family: var(--font-serif); }
.uname { font-size: 13px; max-width: 90px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.burger { display: none; background: none; border: 0; color: var(--gold-light); cursor: pointer; }
.m-nav { display: flex; flex-direction: column; gap: 4px; padding: 10px 0; }
.m-nav a { padding: 12px 10px; color: var(--text-2); font-family: var(--font-serif); letter-spacing: .1em; border-bottom: 1px solid var(--line); }
.m-nav a.active { color: var(--gold-light); }
main { min-height: 100vh; }
.ftr { border-top: 1px solid var(--line); background: linear-gradient(180deg, #0a0d0b, #060807); padding: 56px 0 22px; position: relative; overflow: hidden; }
.ftr::before { content: ''; position: absolute; inset: 0; background-image: var(--noise); opacity: .05; pointer-events: none; }
.ftr-inner { display: flex; justify-content: space-between; gap: 40px; flex-wrap: wrap; position: relative; }
.ftr-brand { display: flex; gap: 16px; align-items: flex-start; max-width: 420px; }
.ftr-title { font-size: 20px; letter-spacing: .2em; color: var(--gold-light); margin-bottom: 6px; }
.ftr-cols { display: flex; gap: 64px; flex-wrap: wrap; }
.ftr-cols div { display: flex; flex-direction: column; gap: 8px; font-size: 13px; }
.ftr-cols h4 { font-size: 14px; color: var(--gold); letter-spacing: .2em; margin-bottom: 4px; }
.ftr-cols a, .ftr-cols span { color: var(--text-3); }
.ftr-cols a:hover { color: var(--gold-light); }
.ftr-bottom { display: flex; justify-content: space-between; flex-wrap: wrap; gap: 8px; margin-top: 40px; padding-top: 18px; border-top: 1px solid var(--line); color: var(--text-4); font-size: 12px; position: relative; }
@media (max-width: 1180px) { .nav-a { padding: 8px 9px; font-size: 13.5px; letter-spacing: .04em; } .brand-txt small { display: none; } }
@media (max-width: 1020px) { .nav { display: none; } .burger { display: block; } .hdr-inner { justify-content: space-between; } }
@media (max-width: 560px) { .ask-btn, .uname { display: none; } .brand-txt b { font-size: 15px; } .hdr-inner { gap: 10px; width: calc(100% - 24px); } .hdr-right { gap: 8px; } .hdr-right .btn-sm { padding: 0 12px; } }
</style>
