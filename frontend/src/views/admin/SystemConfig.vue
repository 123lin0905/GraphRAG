<template>
  <div>
    <div class="cp-head">
      <div class="cp-icon"><el-icon :size="22"><Tools /></el-icon></div>
      <div><h2 class="serif">系统配置</h2><p class="muted">大模型（OpenAI 兼容协议）、GraphRAG 检索与预约参数，数据库配置优先于 application.yml，保存后立即生效</p></div>
    </div>
    <div class="grid" v-loading="loading">
      <div class="panel panel-pad llm">
        <div class="h"><el-icon><MagicStick /></el-icon>大模型服务</div>
        <div class="status" :class="{ ok: status?.configured }">
          <span class="dot"></span>
          <div><b>{{ status?.mode || '—' }}</b><small>当前模型：{{ status?.model || '—' }}</small></div>
        </div>
        <el-form label-position="top">
          <el-form-item label="服务地址 base_url">
            <el-select v-model="vals['llm.base_url']" filterable allow-create default-first-option style="width: 100%">
              <el-option v-for="p in PRESETS" :key="p.url" :label="`${p.name} · ${p.url}`" :value="p.url" />
            </el-select>
          </el-form-item>
          <el-form-item label="模型 model"><el-input v-model="vals['llm.model']" placeholder="deepseek-chat / qwen-plus / gpt-4o-mini" /></el-form-item>
          <el-form-item label="API Key">
            <el-input v-model="vals['llm.api_key']" type="password" show-password :placeholder="keyMasked ? `已配置：${keyMasked}（留空不修改）` : '未配置，问答将使用离线兜底模式'" />
          </el-form-item>
          <div class="two">
            <el-form-item label="温度 temperature"><el-slider v-model="temp" :min="0" :max="1.5" :step="0.1" show-input :show-input-controls="false" /></el-form-item>
            <el-form-item label="最大生成 Token"><el-input-number v-model="maxTokens" :min="128" :max="8192" :step="128" style="width: 100%" /></el-form-item>
          </div>
        </el-form>
        <div class="acts">
          <el-button :loading="testing" @click="test"><el-icon><Connection /></el-icon>测试连通性</el-button>
          <el-button type="primary" :loading="saving" @click="save('LLM')"><el-icon><Check /></el-icon>保存大模型配置</el-button>
        </div>
        <div v-if="testResult" class="test" :class="{ fail: !testResult.success }">
          <b>{{ testResult.success ? '连接成功' : '连接失败' }}</b>
          <span v-if="testResult.latencyMs"> · {{ testResult.latencyMs }} ms</span>
          <p>{{ testResult.reply || testResult.message }}</p>
        </div>
      </div>

      <div class="col">
        <div v-for="g in otherGroups" :key="g.group" class="panel panel-pad">
          <div class="h"><el-icon><component :is="g.icon" /></el-icon>{{ g.title }}</div>
          <el-form label-position="top">
            <el-form-item v-for="c in g.items" :key="c.configKey" :label="c.description || c.configKey">
              <el-input v-model="vals[c.configKey]"><template #prepend>{{ c.configKey }}</template></el-input>
            </el-form-item>
          </el-form>
          <el-button type="primary" plain @click="save(g.group)">保存</el-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi, qaApi } from '@/api'

const PRESETS = [
  { name: 'DeepSeek', url: 'https://api.deepseek.com' },
  { name: '通义千问', url: 'https://dashscope.aliyuncs.com/compatible-mode/v1' },
  { name: 'Moonshot', url: 'https://api.moonshot.cn/v1' },
  { name: 'OpenAI', url: 'https://api.openai.com/v1' }
]
const list = ref<any[]>([])
const vals = reactive<Record<string, string>>({})
const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const status = ref<any>()
const testResult = ref<any>()
const keyMasked = ref('')
const temp = computed({ get: () => Number(vals['llm.temperature'] || 0.3), set: (v) => (vals['llm.temperature'] = String(v)) })
const maxTokens = computed({ get: () => Number(vals['llm.max_tokens'] || 1024), set: (v) => (vals['llm.max_tokens'] = String(v)) })
const otherGroups = computed(() => {
  const meta: Record<string, { title: string; icon: string }> = { RAG: { title: 'GraphRAG 检索参数', icon: 'Share' }, RESERVATION: { title: '预约参数', icon: 'Tickets' }, SYSTEM: { title: '系统参数', icon: 'Setting' } }
  return Object.keys(meta).map((g) => ({ group: g, ...meta[g], items: list.value.filter((c) => c.configGroup === g) })).filter((g) => g.items.length)
})
async function load() {
  loading.value = true
  try {
    list.value = await adminApi.configs()
    list.value.forEach((c) => (vals[c.configKey] = c.valueType === 'SECRET' ? '' : c.configValue ?? ''))
    keyMasked.value = list.value.find((c) => c.configKey === 'llm.api_key')?.configValue || ''
    status.value = await qaApi.llmStatus()
  } finally {
    loading.value = false
  }
}
async function save(group: string) {
  saving.value = true
  try {
    const payload: Record<string, string> = {}
    list.value.filter((c) => c.configGroup === group).forEach((c) => {
      const v = vals[c.configKey]
      if (c.valueType === 'SECRET' && !v) return
      payload[c.configKey] = v ?? ''
    })
    await adminApi.saveConfigs(payload)
    ElMessage.success('配置已保存并生效')
    load()
  } finally {
    saving.value = false
  }
}
async function test() {
  testing.value = true
  try {
    const r = await adminApi.testLlm()
    testResult.value = { success: !!(r.configured && r.reply), ...r }
  } catch (e: any) {
    testResult.value = { success: false, message: e?.message || '请求失败' }
  } finally {
    testing.value = false
  }
}
onMounted(load)
</script>
<style scoped>
.cp-head { display: flex; align-items: center; gap: 14px; margin-bottom: 18px; }
.cp-icon { width: 48px; height: 48px; border-radius: 14px; display: grid; place-items: center; color: var(--gold-light); background: radial-gradient(circle at 30% 30%, rgba(201,164,92,.28), rgba(201,164,92,.05)); border: 1px solid var(--line-strong); }
.cp-head h2 { font-size: 22px; letter-spacing: .14em; }
.cp-head p { font-size: 13px; }
.grid { display: grid; grid-template-columns: 1.1fr 1fr; gap: 18px; align-items: start; }
.col { display: flex; flex-direction: column; gap: 18px; }
.h { display: flex; align-items: center; gap: 8px; font-family: var(--font-serif); color: var(--gold-light); font-size: 17px; letter-spacing: .12em; margin-bottom: 16px; }
.status { display: flex; gap: 12px; align-items: center; padding: 14px; border-radius: 12px; background: rgba(217,164,65,.07); border: 1px solid rgba(217,164,65,.25); margin-bottom: 18px; }
.status.ok { background: rgba(111,174,149,.08); border-color: rgba(111,174,149,.3); }
.status .dot { width: 10px; height: 10px; border-radius: 50%; background: #d9a441; box-shadow: 0 0 10px #d9a441; }
.status.ok .dot { background: var(--jade); box-shadow: 0 0 10px var(--jade); }
.status b { display: block; color: var(--text-1); font-weight: 500; }
.status small { color: var(--text-3); }
.two { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
.acts { display: flex; gap: 10px; justify-content: flex-end; }
.test { margin-top: 14px; padding: 12px 14px; border-radius: 10px; background: rgba(111,174,149,.08); border: 1px solid rgba(111,174,149,.3); color: #9fd4bd; font-size: 13px; }
.test.fail { background: rgba(196,98,75,.08); border-color: rgba(196,98,75,.3); color: #eda08d; }
.test p { color: var(--text-2); margin-top: 4px; word-break: break-all; }
:deep(.el-input-group__prepend) { background: rgba(201,164,92,.06); color: var(--text-3); font-family: ui-monospace, monospace; font-size: 12px; box-shadow: 0 0 0 1px rgba(201,164,92,.2) inset; }
@media (max-width: 1100px) { .grid { grid-template-columns: 1fr; } }
</style>
