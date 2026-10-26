<template>
  <div class="chat">
    <!-- 会话侧栏 -->
    <aside class="c-side panel" :class="{ open: sideOpen }">
      <button class="new-btn" @click="newChat"><el-icon><Plus /></el-icon>新建对话</button>
      <div class="llm">
        <span class="dot" :class="{ ok: llm?.configured }"></span>
        <div>
          <b>{{ llm?.configured ? llm.model : '离线兜底模式' }}</b>
          <small>{{ llm?.configured ? '大模型生成 · GraphRAG 增强' : '未配置 API Key · 基于图谱检索作答' }}</small>
        </div>
      </div>
      <div class="s-title">历史会话</div>
      <div class="s-list">
        <template v-if="auth.isLogin">
          <div v-for="s in sessions" :key="s.id" class="s-item" :class="{ on: s.id === sessionId }" @click="openSession(s.id)">
            <el-icon><ChatLineSquare /></el-icon>
            <span class="ellipsis">{{ s.title || '新对话' }}</span>
            <el-icon class="del" @click.stop="removeSession(s.id)"><Delete /></el-icon>
          </div>
          <div v-if="!sessions.length" class="s-empty">暂无历史会话</div>
        </template>
        <div v-else class="s-empty">
          <router-link :to="{ path: '/login', query: { redirect: '/guide' } }">登录</router-link> 后可保存与查看历史会话
        </div>
      </div>
      <div class="s-foot">
        <div class="flow">
          <span>实体链接</span><i></i><span>子图检索</span><i></i><span>LLM 生成</span>
        </div>
      </div>
    </aside>

    <!-- 主区 -->
    <section class="c-main">
      <button class="side-toggle" @click="sideOpen = !sideOpen"><el-icon><Operation /></el-icon></button>
      <div ref="scroller" class="msgs">
        <div v-if="!messages.length" class="welcome fade-up">
          <div class="w-logo"><SunLogo :size="72" spin /></div>
          <h1>你好，我是三星堆 AI 导览员</h1>
          <p>基于三星堆知识图谱与 GraphRAG 检索增强，为你讲述青铜神树、纵目面具与古蜀文明背后的故事。</p>
          <div class="suggest">
            <button v-for="q in suggestions" :key="q.text" @click="ask(q.text)">
              <el-icon><component :is="q.icon" /></el-icon><span>{{ q.text }}</span>
            </button>
          </div>
        </div>

        <div v-for="(m, i) in messages" :key="m.key" class="msg" :class="m.role">
          <div class="avatar">
            <SunLogo v-if="m.role === 'assistant'" :size="30" />
            <span v-else>{{ auth.displayName.slice(0, 1) }}</span>
          </div>
          <div class="bubble-wrap">
            <div class="bubble">
              <div v-if="m.role === 'assistant'" class="md" :class="{ typing: m.streaming }" v-html="renderMd(m.content || (m.streaming ? '' : '（无内容）'))"></div>
              <div v-else class="u-text">{{ m.content }}</div>
              <div v-if="m.streaming && !m.content" class="thinking"><i></i><i></i><i></i><span>{{ m.phase || '正在检索知识图谱…' }}</span></div>
            </div>

            <div v-if="m.role === 'assistant' && !m.streaming" class="m-foot">
              <span class="chip" :class="m.fallback ? 'red' : 'jade'">{{ m.fallback ? '离线兜底' : m.model || '大模型' }}</span>
              <span v-if="m.latencyMs" class="muted">{{ (m.latencyMs / 1000).toFixed(2) }}s</span>
              <span v-if="m.totalTokens" class="muted">{{ m.totalTokens }} tokens</span>
              <span class="grow"></span>
              <button class="fb" @click="copy(m.content)"><el-icon><DocumentCopy /></el-icon></button>
              <template v-if="m.messageId">
                <button class="fb" :class="{ on: m.feedback === 1 }" @click="feedback(m, 1)"><el-icon><CaretTop /></el-icon>有用</button>
                <button class="fb" :class="{ on: m.feedback === -1 }" @click="feedback(m, -1)"><el-icon><CaretBottom /></el-icon>无用</button>
              </template>
            </div>

            <!-- 检索依据 -->
            <div v-if="m.role === 'assistant' && (m.cited?.length || m.triples?.length)" class="evidence">
              <div class="cited">
                <span class="ev-l"><el-icon><Link /></el-icon>引用实体</span>
                <router-link v-for="c in m.cited" :key="c" :to="{ path: '/graph' }" class="chip" @click.prevent="gotoEntity(c)">{{ c }}</router-link>
              </div>
              <el-collapse>
                <el-collapse-item :name="i">
                  <template #title>
                    <span class="ev-title"><el-icon><Document /></el-icon>检索上下文 · {{ m.triples?.length || 0 }} 条三元组{{ m.references?.length ? ` · ${m.references.length} 篇文献` : '' }}</span>
                  </template>
                  <div class="ev-body">
                    <div v-if="m.subgraph?.nodes?.length" class="ev-graph"><KgGraph :graph="m.subgraph" height="260px" mini :show-legend="false" :zoom="0.75" /></div>
                    <div class="ev-lists">
                      <div class="ev-sec">
                        <h5>知识三元组</h5>
                        <ol><li v-for="t in m.triples?.slice(0, 24)" :key="t">{{ t }}</li></ol>
                      </div>
                      <div v-if="m.references?.length" class="ev-sec">
                        <h5>参考文献</h5>
                        <ul><li v-for="r in m.references" :key="r">《{{ r.replace(/^《|》$/g, '') }}》</li></ul>
                      </div>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>
        </div>
      </div>

      <div class="composer">
        <div class="comp-box">
          <el-input v-model="input" type="textarea" :autosize="{ minRows: 1, maxRows: 5 }" resize="none" placeholder="向导览员提问，例如：青铜神树与《山海经》有什么关系？（Enter 发送，Shift+Enter 换行）" @keydown.enter.exact.prevent="ask()" />
          <button v-if="!busy" class="send" :disabled="!input.trim()" @click="ask()"><el-icon :size="18"><Promotion /></el-icon></button>
          <button v-else class="send stop" @click="stop"><el-icon :size="18"><VideoPause /></el-icon></button>
        </div>
        <div class="hint">回答由知识图谱检索结果与大模型生成，史实部分请以博物馆官方资料为准</div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SunLogo from '@/components/SunLogo.vue'
import KgGraph from '@/components/KgGraph.vue'
import { kgApi, qaApi } from '@/api'
import { streamSse } from '@/api/http'
import type { GraphVO, QaSession } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { renderMd } from '@/utils/markdown'
import { parseJsonArray } from '@/utils/format'

interface ChatMsg {
  key: string
  role: 'user' | 'assistant'
  content: string
  streaming?: boolean
  phase?: string
  messageId?: number
  fallback?: boolean
  model?: string
  latencyMs?: number
  totalTokens?: number
  feedback?: number
  cited?: string[]
  triples?: string[]
  references?: string[]
  subgraph?: GraphVO
}

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const messages = ref<ChatMsg[]>([])
const sessions = ref<QaSession[]>([])
const sessionId = ref<number | undefined>(Number(sessionStorage.getItem('sxd_qa_sid')) || undefined)
const input = ref('')
const busy = ref(false)
const scroller = ref<HTMLElement>()
const sideOpen = ref(false)
const llm = ref<{ configured: boolean; model: string; mode: string }>()
let ctrl: AbortController | null = null

const suggestions = [
  { icon: 'Sunny', text: '青铜神树有什么象征意义？' },
  { icon: 'View', text: '纵目面具为什么眼睛凸出？' },
  { icon: 'Medal', text: '金杖上的图案代表什么？' },
  { icon: 'MapLocation', text: '二号祭祀坑出土了哪些文物？' },
  { icon: 'Reading', text: '三星堆与金沙遗址有什么关系？' },
  { icon: 'Guide', text: '第一次来参观，推荐看哪些文物？' }
]

function scrollDown() { nextTick(() => scroller.value?.scrollTo({ top: scroller.value.scrollHeight, behavior: 'smooth' })) }

async function ask(text?: string) {
  const q = (text ?? input.value).trim()
  if (!q || busy.value) return
  input.value = ''
  messages.value.push({ key: 'u' + Date.now(), role: 'user', content: q })
  const m = reactive<ChatMsg>({ key: 'a' + Date.now(), role: 'assistant', content: '', streaming: true, phase: '实体链接 · 检索知识子图…' })
  messages.value.push(m)
  scrollDown()
  busy.value = true
  ctrl = new AbortController()
  // 打字机缓冲：平滑输出
  let buffer = ''
  let doneFlag = false
  const timer = setInterval(() => {
    if (buffer.length) {
      const n = Math.max(1, Math.ceil(buffer.length / 12))
      m.content += buffer.slice(0, n)
      buffer = buffer.slice(n)
      scrollDown()
    } else if (doneFlag) {
      clearInterval(timer)
      m.streaming = false
      busy.value = false
    }
  }, 28)
  try {
    await streamSse('/qa/stream', { sessionId: sessionId.value, question: q }, (ev, data) => {
      if (ev === 'meta') {
        if (data.sessionId) { sessionId.value = data.sessionId; sessionStorage.setItem('sxd_qa_sid', String(data.sessionId)) }
        m.cited = data.citedEntities || []
        m.triples = data.triples || []
        m.references = data.references || []
        m.subgraph = data.subgraph
        m.phase = `已检索 ${m.triples?.length || 0} 条三元组，正在生成回答…`
      } else if (ev === 'delta') {
        buffer += data?.content ?? ''
      } else if (ev === 'done') {
        m.messageId = data.messageId
        m.fallback = !!data.fallback
        m.model = data.model
        m.latencyMs = data.latencyMs
        m.totalTokens = data.totalTokens
      } else if (ev === 'error') {
        buffer += `\n\n> ${data?.message || '问答服务异常'}`
      }
    }, ctrl.signal)
  } catch (e: any) {
    if (e?.name !== 'AbortError') buffer += (m.content || buffer) ? '\n\n> 连接中断' : '抱歉，问答服务暂时不可用，请稍后再试。'
  } finally {
    doneFlag = true
    ctrl = null
    if (auth.isLogin) loadSessions()
  }
}
function stop() { ctrl?.abort() }
function newChat() {
  stop()
  sessionId.value = undefined
  sessionStorage.removeItem('sxd_qa_sid')
  messages.value = []
  sideOpen.value = false
}
async function loadSessions() {
  try { sessions.value = await qaApi.sessions() } catch { sessions.value = [] }
}
async function openSession(id: number) {
  stop()
  sideOpen.value = false
  sessionId.value = id
  sessionStorage.setItem('sxd_qa_sid', String(id))
  const list = await qaApi.messages(id)
  messages.value = list.map((x) => {
    const ctx = (() => { try { return x.retrievedContext ? JSON.parse(x.retrievedContext) : {} } catch { return {} } })()
    const triples: string[] = Array.isArray(ctx) ? ctx.map((t: any) => t.triple || String(t)) : (ctx.triples || [])
    const refs: string[] = Array.isArray(ctx?.literature) ? ctx.literature.map((l: any) => l.title || String(l)) : []
    return {
      key: 'h' + x.id, role: x.role, content: x.content, messageId: x.role === 'assistant' ? x.id : undefined,
      fallback: x.isFallback === 1, model: x.modelName, latencyMs: x.latencyMs, totalTokens: x.totalTokens, feedback: x.feedback,
      cited: parseJsonArray(x.citedEntities), triples, references: refs
    } as ChatMsg
  })
  scrollDown()
}
async function removeSession(id: number) {
  await ElMessageBox.confirm('确定删除该会话？', '提示', { type: 'warning' })
  await qaApi.deleteSession(id)
  if (id === sessionId.value) newChat()
  loadSessions()
}
async function feedback(m: ChatMsg, v: 1 | -1) {
  if (!m.messageId) return
  await qaApi.feedback(m.messageId, v)
  m.feedback = v
  ElMessage.success('感谢反馈')
}
function copy(t: string) { navigator.clipboard?.writeText(t).then(() => ElMessage.success('已复制')) }
async function gotoEntity(name: string) {
  const r = await kgApi.search(name, 1)
  router.push({ path: '/graph', query: r[0] ? { focus: r[0].id } : {} })
}

onMounted(async () => {
  qaApi.llmStatus().then((s) => (llm.value = s)).catch(() => undefined)
  if (auth.isLogin) {
    await loadSessions()
    if (sessionId.value && sessions.value.some((s) => s.id === sessionId.value)) await openSession(sessionId.value)
  } else {
    sessionId.value = undefined
  }
  const q = route.query.q as string | undefined
  if (q) { router.replace({ query: {} }); newChat(); ask(q) }
})
onBeforeUnmount(stop)
</script>

<style scoped>
.chat { display: grid; grid-template-columns: 290px 1fr; gap: 16px; height: 100vh; padding: calc(var(--header-h) + 16px) 16px 16px; }
.c-side { display: flex; flex-direction: column; padding: 16px; min-height: 0; }
.new-btn { height: 44px; border-radius: 12px; border: 1px dashed var(--line-strong); background: rgba(201,164,92,.06); color: var(--gold-light); cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; font-family: var(--font-serif); letter-spacing: .14em; font-size: 15px; transition: all .25s; }
.new-btn:hover { background: rgba(201,164,92,.14); border-style: solid; }
.llm { display: flex; gap: 10px; align-items: center; margin: 14px 0 6px; padding: 12px; border-radius: 12px; background: rgba(0,0,0,.25); border: 1px solid var(--line); }
.llm .dot { width: 9px; height: 9px; border-radius: 50%; background: #d9a441; box-shadow: 0 0 10px #d9a441; flex: none; }
.llm .dot.ok { background: var(--jade); box-shadow: 0 0 10px var(--jade); }
.llm b { display: block; font-size: 13px; color: var(--text-1); font-weight: 500; }
.llm small { color: var(--text-3); font-size: 11.5px; }
.s-title { color: var(--text-3); font-size: 12px; letter-spacing: .3em; margin: 16px 4px 8px; }
.s-list { flex: 1; overflow: auto; display: flex; flex-direction: column; gap: 4px; }
.s-item { display: flex; align-items: center; gap: 8px; padding: 10px 12px; border-radius: 10px; cursor: pointer; color: var(--text-2); font-size: 13px; transition: background .2s; border: 1px solid transparent; }
.s-item:hover { background: rgba(201,164,92,.06); }
.s-item.on { background: rgba(201,164,92,.12); color: var(--gold-light); border-color: var(--line); }
.s-item span { flex: 1; }
.del { opacity: 0; color: var(--text-3); } .s-item:hover .del { opacity: 1; }
.s-empty { color: var(--text-3); font-size: 12.5px; padding: 14px 6px; text-align: center; }
.s-foot { border-top: 1px solid var(--line); padding-top: 12px; }
.flow { display: flex; align-items: center; justify-content: space-between; font-size: 11px; color: var(--text-3); }
.flow i { flex: 1; height: 1px; margin: 0 6px; background: linear-gradient(90deg, var(--gold-deep), transparent); }
.c-main { position: relative; display: flex; flex-direction: column; min-height: 0; border-radius: var(--radius); border: 1px solid var(--line);
  background: radial-gradient(900px 500px at 50% 0%, rgba(36,57,45,.3), transparent 70%), linear-gradient(180deg, #0e1310, #0a0d0b); overflow: hidden; }
.side-toggle { display: none; position: absolute; top: 12px; left: 12px; z-index: 3; width: 34px; height: 34px; border-radius: 10px; border: 1px solid var(--line); background: rgba(10,14,12,.8); color: var(--gold-light); }
.msgs { flex: 1; overflow: auto; padding: 30px max(24px, calc((100% - 860px) / 2)) 20px; scroll-behavior: smooth; }
.welcome { text-align: center; padding: 6vh 0 20px; }
.w-logo { display: inline-grid; place-items: center; width: 120px; height: 120px; border-radius: 50%; background: radial-gradient(circle, rgba(201,164,92,.18), transparent 70%); }
.welcome h1 { font-size: clamp(24px, 3vw, 34px); margin: 18px 0 10px; letter-spacing: .16em; background: var(--grad-gold); -webkit-background-clip: text; background-clip: text; color: transparent; }
.welcome p { color: var(--text-2); max-width: 560px; margin: 0 auto; }
.suggest { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 36px; text-align: left; }
.suggest button { display: flex; align-items: center; gap: 10px; padding: 14px 16px; border-radius: 14px; border: 1px solid var(--line); background: rgba(201,164,92,.03); color: var(--text-2); cursor: pointer; font-size: 13.5px; transition: all .3s var(--ease); }
.suggest button:hover { border-color: var(--line-strong); color: var(--gold-light); transform: translateY(-3px); background: rgba(201,164,92,.08); }
.suggest .el-icon { color: var(--gold); font-size: 18px; flex: none; }
.msg { display: flex; gap: 14px; margin-bottom: 26px; animation: fadeUp .5s var(--ease) both; }
.msg.user { flex-direction: row-reverse; }
.avatar { flex: none; width: 38px; height: 38px; border-radius: 50%; display: grid; place-items: center; background: #101611; border: 1px solid var(--line-strong); font-family: var(--font-serif); color: var(--gold-pale); }
.msg.user .avatar { background: linear-gradient(135deg, var(--patina-500), var(--patina-900)); }
.bubble-wrap { max-width: min(760px, 86%); min-width: 0; }
.msg.user .bubble-wrap { display: flex; flex-direction: column; align-items: flex-end; }
.bubble { padding: 14px 18px; border-radius: 16px; position: relative; }
.msg.assistant .bubble { background: linear-gradient(160deg, rgba(36,57,45,.4), rgba(17,22,19,.9)); border: 1px solid var(--line); border-top-left-radius: 4px; }
.msg.user .bubble { background: linear-gradient(135deg, #e8cb8a, #c9a45c 60%, #a5823f); color: #1a140a; border-top-right-radius: 4px; }
.u-text { white-space: pre-wrap; font-weight: 500; }
.md.typing > :last-child::after { content: ''; display: inline-block; width: 8px; height: 1.1em; background: var(--gold); margin-left: 3px; vertical-align: text-bottom; animation: blink .9s steps(2) infinite; }
@keyframes blink { 50% { opacity: 0; } }
.thinking { display: flex; align-items: center; gap: 5px; color: var(--text-3); font-size: 13px; }
.thinking i { width: 6px; height: 6px; border-radius: 50%; background: var(--gold); animation: bounce 1.2s infinite; }
.thinking i:nth-child(2) { animation-delay: .15s; } .thinking i:nth-child(3) { animation-delay: .3s; }
.thinking span { margin-left: 6px; }
@keyframes bounce { 0%, 80%, 100% { transform: scale(.6); opacity: .4; } 40% { transform: scale(1); opacity: 1; } }
.m-foot { display: flex; align-items: center; gap: 10px; margin-top: 8px; font-size: 12px; flex-wrap: wrap; }
.grow { flex: 1; }
.fb { display: inline-flex; align-items: center; gap: 3px; background: none; border: 1px solid transparent; color: var(--text-3); cursor: pointer; border-radius: 8px; padding: 2px 8px; font-size: 12px; }
.fb:hover, .fb.on { color: var(--gold-light); border-color: var(--line); }
.evidence { margin-top: 10px; border: 1px solid var(--line); border-radius: 12px; padding: 10px 14px 2px; background: rgba(0,0,0,.2); }
.cited { display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
.ev-l { display: inline-flex; align-items: center; gap: 4px; color: var(--text-3); font-size: 12px; margin-right: 4px; }
.cited .chip { cursor: pointer; }
.evidence :deep(.el-collapse) { border: 0; margin-top: 4px; }
.evidence :deep(.el-collapse-item__header) { height: 38px; border: 0; color: var(--text-3); font-size: 12.5px; }
.evidence :deep(.el-collapse-item__wrap) { border: 0; }
.ev-title { display: inline-flex; align-items: center; gap: 6px; }
.ev-body { display: grid; grid-template-columns: 260px 1fr; gap: 14px; }
.ev-graph { border-radius: 10px; border: 1px solid var(--line); background: rgba(0,0,0,.25); overflow: hidden; }
.ev-lists { max-height: 260px; overflow: auto; }
.ev-sec h5 { margin: 0 0 6px; color: var(--gold); font-size: 12.5px; letter-spacing: .1em; font-weight: 500; }
.ev-sec ol, .ev-sec ul { margin: 0 0 10px; padding-left: 20px; color: var(--text-2); font-size: 12.5px; line-height: 1.8; }
.composer { padding: 14px max(24px, calc((100% - 860px) / 2)) 16px; border-top: 1px solid var(--line); background: rgba(8,11,9,.6); backdrop-filter: blur(12px); }
.comp-box { display: flex; align-items: flex-end; gap: 10px; padding: 8px 8px 8px 6px; border-radius: 18px; border: 1px solid var(--line-strong); background: rgba(10,14,12,.9); box-shadow: 0 10px 30px rgba(0,0,0,.35); transition: border-color .2s, box-shadow .2s; }
.comp-box:focus-within { border-color: var(--gold); box-shadow: 0 0 0 4px rgba(201,164,92,.1), 0 10px 30px rgba(0,0,0,.35); }
.comp-box :deep(.el-textarea__inner) { background: transparent !important; box-shadow: none !important; font-size: 14.5px; padding: 8px 10px; }
.send { flex: none; width: 42px; height: 42px; border-radius: 13px; border: 0; cursor: pointer; background: var(--grad-gold); color: #1a140a; display: grid; place-items: center; transition: transform .2s, opacity .2s; }
.send:disabled { opacity: .35; cursor: not-allowed; }
.send:not(:disabled):hover { transform: scale(1.05); }
.send.stop { background: rgba(196,98,75,.85); color: #fff; }
.hint { text-align: center; color: var(--text-4); font-size: 11.5px; margin-top: 8px; }
@media (max-width: 900px) {
  .chat { grid-template-columns: 1fr; }
  .c-side { position: fixed; z-index: 50; top: calc(var(--header-h) + 8px); left: 8px; bottom: 8px; width: 280px; transform: translateX(-110%); transition: transform .35s var(--ease); background: #0e1310; }
  .c-side.open { transform: none; }
  .side-toggle { display: grid; place-items: center; }
  .suggest { grid-template-columns: 1fr; }
  .ev-body { grid-template-columns: 1fr; }
}
</style>
