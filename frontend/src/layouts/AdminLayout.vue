<template>
  <div class="admin" :class="{ collapsed }">
    <aside class="side">
      <router-link to="/admin/dashboard" class="logo">
        <SunLogo :size="34" />
        <div v-show="!collapsed" class="logo-t"><b class="serif">数字孪生管理平台</b><small>SANXINGDUI ADMIN</small></div>
      </router-link>
      <el-scrollbar class="menu-scroll">
        <el-menu :default-active="active" :collapse="collapsed" router :collapse-transition="false" unique-opened>
          <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon><template #title>数据大屏</template></el-menu-item>
          <template v-for="g in groups" :key="g.name">
            <el-sub-menu :index="g.name">
              <template #title><el-icon><component :is="g.icon" /></el-icon><span>{{ g.name }}</span></template>
              <el-menu-item v-for="c in g.items" :key="c.key" :index="`/admin/crud/${c.key}`"><el-icon><component :is="c.icon" /></el-icon><template #title>{{ c.title }}</template></el-menu-item>
              <el-menu-item v-if="g.name === '系统管理' && auth.isAdmin" index="/admin/configs"><el-icon><Tools /></el-icon><template #title>系统配置</template></el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>
      <div class="side-foot" v-show="!collapsed">
        <span class="dot"></span>系统运行中 · v1.0
      </div>
    </aside>

    <section class="main">
      <header class="top">
        <button class="fold" @click="collapsed = !collapsed"><el-icon :size="18"><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon></button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>管理后台</el-breadcrumb-item>
          <el-breadcrumb-item v-if="crumbGroup">{{ crumbGroup }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ crumbTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="top-r">
          <router-link to="/" class="front"><el-icon><House /></el-icon>返回前台</router-link>
          <el-dropdown @command="onCmd">
            <div class="user">
              <span class="avatar serif">{{ auth.displayName.slice(0, 1) }}</span>
              <div class="u-t"><b>{{ auth.displayName }}</b><small>{{ auth.isAdmin ? '系统管理员' : '馆员' }}</small></div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="me" icon="User">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" icon="SwitchButton" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <div class="content">
        <router-view v-slot="{ Component }">
          <transition name="route-fade" mode="out-in"><component :is="Component" :key="route.fullPath" /></transition>
        </router-view>
      </div>
    </section>
  </div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SunLogo from '@/components/SunLogo.vue'
import { useAuthStore } from '@/stores/auth'
import { CRUD_MAP, MENU_GROUPS } from '@/admin/configs'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const collapsed = ref(window.innerWidth < 900)
const groups = computed(() =>
  MENU_GROUPS.map((g) => ({ ...g, items: g.items.filter((c) => !c.roles || c.roles.some((r) => auth.roles.includes(r))) })).filter((g) => g.items.length)
)
const active = computed(() => route.path)
const cfg = computed(() => (route.params.key ? CRUD_MAP[route.params.key as string] : undefined))
const crumbGroup = computed(() => cfg.value?.group || (route.name === 'admin-configs' ? '系统管理' : ''))
const crumbTitle = computed(() => cfg.value?.title || (route.meta.title as string))
function onCmd(c: string) {
  if (c === 'me') router.push('/me')
  else { auth.logout(); router.push('/login') }
}
</script>
<style scoped>
.admin { display: flex; min-height: 100vh; background: radial-gradient(1200px 600px at 100% 0%, rgba(36,57,45,.22), transparent 60%), var(--bg-1); }
.side { width: 236px; flex: none; position: sticky; top: 0; height: 100vh; display: flex; flex-direction: column; border-right: 1px solid var(--line);
  background: linear-gradient(180deg, #0f1512, #0a0d0b); transition: width .3s var(--ease); }
.collapsed .side { width: 64px; }
.logo { height: 64px; display: flex; align-items: center; gap: 10px; padding: 0 15px; border-bottom: 1px solid var(--line); overflow: hidden; white-space: nowrap; }
.logo-t { display: flex; flex-direction: column; line-height: 1.2; }
.logo-t b { font-size: 15px; letter-spacing: .1em; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
.logo-t small { font-size: 9px; letter-spacing: .3em; color: var(--text-3); }
.menu-scroll { flex: 1; }
.side :deep(.el-menu) { padding: 8px; }
.side :deep(.el-menu-item), .side :deep(.el-sub-menu__title) { height: 42px; line-height: 42px; border-radius: 10px; margin: 2px 0; }
.side :deep(.el-menu--inline .el-menu-item) { height: 38px; line-height: 38px; padding-left: 46px !important; font-size: 13px; }
.side :deep(.el-menu-item.is-active) { background: linear-gradient(90deg, rgba(201,164,92,.2), rgba(201,164,92,.04)); color: var(--gold-light); position: relative; }
.side :deep(.el-menu-item.is-active)::before { content: ''; position: absolute; left: 0; top: 10px; bottom: 10px; width: 3px; border-radius: 3px; background: var(--gold); }
.side :deep(.el-sub-menu.is-active > .el-sub-menu__title) { color: var(--gold-light); }
.side :deep(.el-menu--collapse) { width: 64px; padding: 8px 4px; }
.side-foot { padding: 14px 18px; border-top: 1px solid var(--line); color: var(--text-4); font-size: 12px; display: flex; align-items: center; gap: 8px; }
.side-foot .dot { width: 7px; height: 7px; border-radius: 50%; background: var(--jade); box-shadow: 0 0 8px var(--jade); }
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.top { height: 64px; display: flex; align-items: center; gap: 16px; padding: 0 24px; border-bottom: 1px solid var(--line); background: rgba(10,13,11,.7); backdrop-filter: blur(12px); position: sticky; top: 0; z-index: 20; }
.fold { background: none; border: 1px solid var(--line); color: var(--gold-light); width: 34px; height: 34px; border-radius: 10px; cursor: pointer; display: grid; place-items: center; }
.top :deep(.el-breadcrumb__inner) { color: var(--text-3) !important; }
.top :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) { color: var(--gold-light) !important; }
.top-r { margin-left: auto; display: flex; align-items: center; gap: 18px; }
.front { display: inline-flex; align-items: center; gap: 6px; color: var(--text-2); font-size: 13px; padding: 6px 12px; border: 1px solid var(--line); border-radius: 999px; }
.front:hover { color: var(--gold-light); border-color: var(--line-strong); }
.user { display: flex; align-items: center; gap: 10px; cursor: pointer; outline: none; color: var(--text-3); }
.avatar { width: 34px; height: 34px; border-radius: 50%; display: grid; place-items: center; background: var(--grad-gold); color: #1a140a; }
.u-t { display: flex; flex-direction: column; line-height: 1.25; }
.u-t b { font-size: 13px; color: var(--text-1); font-weight: 500; }
.u-t small { font-size: 11px; color: var(--text-3); }
.content { padding: 22px 24px 30px; flex: 1; min-width: 0; }
@media (max-width: 700px) { .u-t, .front { display: none; } .content { padding: 14px; } }
</style>
