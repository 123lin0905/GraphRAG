import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    auth?: boolean
    roles?: string[]
    dark?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/PortalLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/portal/Home.vue'), meta: { title: '首页' } },
      { path: 'artifacts', name: 'artifacts', component: () => import('@/views/portal/ArtifactList.vue'), meta: { title: '文物展厅' } },
      { path: 'artifacts/:id', name: 'artifact', component: () => import('@/views/portal/ArtifactDetail.vue'), meta: { title: '文物详情' } },
      { path: 'hall3d', name: 'hall3d', component: () => import('@/views/portal/Hall3D.vue'), meta: { title: '3D数字展厅' } },
      { path: 'graph', name: 'graph', component: () => import('@/views/portal/GraphExplorer.vue'), meta: { title: '知识图谱' } },
      { path: 'guide', name: 'guide', component: () => import('@/views/portal/GuideChat.vue'), meta: { title: '智能导览问答' } },
      { path: 'routes', name: 'routes', component: () => import('@/views/portal/RouteList.vue'), meta: { title: '导览路线' } },
      { path: 'routes/:id', name: 'route', component: () => import('@/views/portal/RouteDetail.vue'), meta: { title: '路线详情' } },
      { path: 'reservation', name: 'reservation', component: () => import('@/views/portal/Reservation.vue'), meta: { title: '参观预约' } },
      { path: 'me', name: 'me', component: () => import('@/views/portal/Mine.vue'), meta: { title: '我的', auth: true } }
    ]
  },
  { path: '/screen', name: 'screen', component: () => import('@/views/portal/BigScreen.vue'), meta: { title: '数据大屏' } },
  { path: '/login', name: 'login', component: () => import('@/views/portal/AuthPage.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'register', component: () => import('@/views/portal/AuthPage.vue'), meta: { title: '注册' } },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { auth: true, roles: ['ADMIN', 'CURATOR'] },
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'admin-dashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '数据大屏' } },
      { path: 'configs', name: 'admin-configs', component: () => import('@/views/admin/SystemConfig.vue'), meta: { title: '系统配置', roles: ['ADMIN'] } },
      { path: 'crud/:key', name: 'admin-crud', component: () => import('@/views/admin/CrudPage.vue'), meta: { title: '数据管理' } }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'notfound', component: () => import('@/views/portal/NotFound.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, _from, saved) {
    if (saved) return saved
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const needAuth = to.matched.some((r) => r.meta.auth)
  if (needAuth && !auth.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const roleReq = [...to.matched].reverse().find((r) => r.meta.roles)?.meta.roles
  if (roleReq && !roleReq.some((r) => auth.roles.includes(r))) {
    ElMessage.warning('当前账号无权访问该页面')
    return auth.isStaff ? { path: '/admin/dashboard' } : { path: '/' }
  }
  return true
})

router.afterEach((to) => {
  document.title = `${to.meta.title ? to.meta.title + ' · ' : ''}三星堆数字孪生与智能导览`
})

export default router
