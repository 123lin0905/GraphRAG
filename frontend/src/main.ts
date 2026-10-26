import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as Icons from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@fontsource/noto-serif-sc/600.css'
import '@fontsource/noto-serif-sc/900.css'
import './styles/global.css'
import './styles/element.css'
import App from './App.vue'
import router from './router'

document.documentElement.classList.add('dark')

// 已由 axios 拦截器提示过的错误、用户取消的确认框，不再作为未处理异常抛到控制台
window.addEventListener('unhandledrejection', (e) => {
  const r = e.reason
  if (r === 'cancel' || r === 'close' || (r && typeof r === 'object' && (r as { handled?: boolean }).handled)) e.preventDefault()
})

const app = createApp(App)
for (const [name, comp] of Object.entries(Icons)) app.component(name, comp)
app.config.errorHandler = (err) => {
  if (err && typeof err === 'object' && (err as { handled?: boolean }).handled) return
  console.error(err)
}
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
