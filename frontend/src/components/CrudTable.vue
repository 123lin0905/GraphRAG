<template>
  <div class="crud">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="tb-left">
        <el-input v-if="config.keyword !== false" v-model="query.keyword" :placeholder="config.keywordPlaceholder || '关键词搜索'" clearable style="width: 220px" @keyup.enter="search" @clear="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <template v-for="f in filterFields" :key="f.prop">
          <el-date-picker v-if="f.type === 'date'" v-model="filters[f.prop]" type="date" value-format="YYYY-MM-DD" :placeholder="f.label" style="width: 150px" @change="search" />
          <el-select v-else v-model="filters[f.prop]" :placeholder="f.label" clearable filterable style="width: 150px" @change="search">
            <el-option v-for="op in optionsOf(f)" :key="String(op.value)" :label="op.label" :value="op.value" />
          </el-select>
        </template>
        <el-button @click="search"><el-icon><Search /></el-icon>查询</el-button>
        <el-button @click="resetQuery"><el-icon><RefreshLeft /></el-icon>重置</el-button>
      </div>
      <div class="tb-right">
        <el-button v-for="a in config.toolbarActions || []" :key="a.label" :type="a.type" @click="runToolbar(a)"><el-icon v-if="a.icon"><component :is="a.icon" /></el-icon>{{ a.label }}</el-button>
        <el-button v-if="canRemove && selection.length" type="danger" plain @click="batchRemove"><el-icon><Delete /></el-icon>批量删除 ({{ selection.length }})</el-button>
        <el-button v-if="config.create !== false" type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增</el-button>
        <el-tooltip content="刷新"><el-button circle @click="load"><el-icon><Refresh /></el-icon></el-button></el-tooltip>
      </div>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="rows" row-key="id" stripe class="tbl" @selection-change="(s: any[]) => (selection = s)" :max-height="tableHeight">
      <el-table-column v-if="canRemove" type="selection" width="44" fixed="left" />
      <el-table-column prop="id" label="ID" width="64" />
      <el-table-column v-for="f in tableFields" :key="f.prop" :prop="f.prop" :label="f.label" :width="f.width" :min-width="f.minWidth || (f.width ? undefined : 120)" :show-overflow-tooltip="f.ellipsis || f.type === 'textarea'">
        <template #default="{ row }">
          <template v-if="f.type === 'image'">
            <el-image v-if="row[f.prop]" :src="row[f.prop]" fit="cover" class="cell-img" :preview-src-list="[row[f.prop]]" preview-teleported>
              <template #error><div class="img-err"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <span v-else class="muted">—</span>
          </template>
          <el-rate v-else-if="f.type === 'rate'" :model-value="row[f.prop]" disabled size="small" />
          <el-tag v-else-if="f.tags && row[f.prop] !== null && row[f.prop] !== undefined" :type="f.tags[row[f.prop]] || 'info'" size="small" effect="dark" round class="tag">{{ display(f, row) }}</el-tag>
          <span v-else :class="{ muted: display(f, row) === '—' }">{{ display(f, row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" :width="actionWidth">
        <template #default="{ row }">
          <div class="ops">
            <el-button link type="primary" @click="openView(row)">查看</el-button>
            <el-button v-if="config.edit !== false" link type="primary" @click="openForm(row)">编辑</el-button>
            <template v-for="a in config.rowActions || []" :key="a.label">
              <el-button v-if="!a.show || a.show(row)" link :type="a.type || 'primary'" @click="runRow(a, row)">{{ a.label }}</el-button>
            </template>
            <el-button v-if="canRemove" link type="danger" @click="remove(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <span class="muted">共 {{ total }} 条记录</span>
      <el-pagination background layout="sizes, prev, pager, next, jumper" :total="total" v-model:page-size="query.size" v-model:current-page="query.current" :page-sizes="[10, 20, 50, 100]" @current-change="load" @size-change="search" />
    </div>

    <!-- 表单 -->
    <el-dialog v-model="formVisible" :title="(editing ? '编辑' : '新增') + config.title" :width="config.dialogWidth || '720px'" destroy-on-close top="6vh">
      <el-form ref="formRef" :model="form" label-position="top" class="form-grid">
        <el-form-item v-for="f in formFields" :key="f.prop" :label="f.label" :prop="f.prop" :class="{ span2: f.span === 2 || f.type === 'textarea' || f.type === 'json' }"
          :rules="f.required ? [{ required: true, message: `请填写${f.label}`, trigger: ['blur', 'change'] }] : []">
          <el-input v-if="!f.type || f.type === 'text' || f.type === 'image'" v-model="form[f.prop]" :placeholder="f.placeholder || (f.type === 'image' ? '图片 URL，如 /static/images/...' : '')" clearable>
            <template v-if="f.type === 'image' && form[f.prop]" #append><el-image :src="form[f.prop]" class="pv" fit="cover" /></template>
          </el-input>
          <el-input v-else-if="f.type === 'password'" v-model="form[f.prop]" type="password" show-password />
          <el-input v-else-if="f.type === 'textarea' || f.type === 'json'" v-model="form[f.prop]" type="textarea" :autosize="{ minRows: 2, maxRows: 8 }" />
          <el-input-number v-else-if="f.type === 'number'" v-model="form[f.prop]" :min="f.min" :max="f.max" :step="f.step || 1" :precision="f.precision" controls-position="right" style="width: 100%" />
          <el-select v-else-if="f.type === 'select'" v-model="form[f.prop]" filterable clearable style="width: 100%" :placeholder="`请选择${f.label}`">
            <el-option v-if="f.prop === 'parentId'" label="（顶级）" :value="0" />
            <el-option v-for="op in optionsOf(f)" :key="String(op.value)" :label="op.label" :value="op.value" />
          </el-select>
          <el-switch v-else-if="f.type === 'switch'" v-model="form[f.prop]" :active-value="1" :inactive-value="0" />
          <el-date-picker v-else-if="f.type === 'date'" v-model="form[f.prop]" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          <el-date-picker v-else-if="f.type === 'datetime'" v-model="form[f.prop]" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
          <el-rate v-else-if="f.type === 'rate'" v-model="form[f.prop]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer v-model="viewVisible" :title="config.title + '详情'" size="560px">
      <div v-if="viewRow" class="view">
        <div class="v-item"><label>ID</label><div>{{ viewRow.id }}</div></div>
        <div v-for="f in config.fields.filter((x) => x.form !== false || x.table !== false)" :key="f.prop" class="v-item" :class="{ wide: f.type === 'textarea' || f.type === 'json' }">
          <label>{{ f.label }}</label>
          <div v-if="f.type === 'image' && viewRow[f.prop]"><el-image :src="viewRow[f.prop]" class="v-img" fit="cover" /></div>
          <pre v-else-if="f.type === 'json'" class="json">{{ pretty(viewRow[f.prop]) }}</pre>
          <div v-else class="v-val">{{ display(f, viewRow) }}</div>
        </div>
        <div class="v-item" v-if="viewRow.createdAt"><label>创建时间</label><div>{{ viewRow.createdAt }}</div></div>
        <div class="v-item" v-if="viewRow.updatedAt"><label>更新时间</label><div>{{ viewRow.updatedAt }}</div></div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { http, type PageResult } from '@/api/http'
import type { CrudConfig, CrudField, RowAction, ToolbarAction } from '@/admin/types'
import { clearDict, loadDict, type Option } from '@/utils/dicts'

const props = defineProps<{ config: CrudConfig }>()
const rows = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const selection = ref<any[]>([])
const query = reactive({ current: 1, size: 10, keyword: '' })
const filters = reactive<Record<string, any>>({})
const dicts = reactive<Record<string, Option[]>>({})
const formVisible = ref(false)
const viewVisible = ref(false)
const viewRow = ref<any>()
const editing = ref<any>(null)
const form = reactive<Record<string, any>>({})
const formRef = ref<FormInstance>()
const saving = ref(false)
const tableHeight = ref(560)

const tableFields = computed(() => props.config.fields.filter((f) => f.table !== false))
const formFields = computed(() => props.config.fields.filter((f) => f.form !== false))
const filterFields = computed(() => props.config.fields.filter((f) => f.filter))
const canRemove = computed(() => props.config.remove !== false)
const actionWidth = computed(() => {
  const n = 1 + (props.config.edit !== false ? 1 : 0) + (canRemove.value ? 1 : 0) + (props.config.rowActions?.length || 0)
  return Math.max(130, n * 52 + 24)
})

function optionsOf(f: CrudField): Option[] { return f.options || (f.dict ? dicts[f.dict] || [] : []) }
function display(f: CrudField, row: any): string {
  if (f.formatter) { const s = f.formatter(row); if (s) return s }
  const v = row[f.prop]
  if (v === null || v === undefined || v === '') return '—'
  const opts = optionsOf(f)
  if (opts.length) {
    const hit = opts.find((o) => String(o.value) === String(v))
    if (hit) return hit.label.replace(/^\s*└\s*/, '').replace(/^　└ /, '')
  }
  if (f.type === 'switch') return v === 1 ? '是' : '否'
  if (f.type === 'number' && typeof v === 'number') return v.toLocaleString('zh-CN', { maximumFractionDigits: 4 })
  return String(v)
}
function pretty(v: any) { if (!v) return '—'; try { return JSON.stringify(JSON.parse(v), null, 2) } catch { return String(v) } }

async function load() {
  loading.value = true
  try {
    const params: Record<string, any> = { ...query }
    for (const [k, v] of Object.entries(filters)) if (v !== undefined && v !== null && v !== '') params[k] = v
    if (!params.keyword) delete params.keyword
    const url = props.config.pageUrl || `${props.config.api}/page`
    const p = await http.get<PageResult<any>>(url, params)
    rows.value = p.records
    total.value = p.total
  } finally {
    loading.value = false
  }
}
function search() { query.current = 1; load() }
function resetQuery() { query.keyword = ''; Object.keys(filters).forEach((k) => delete filters[k]); search() }
const ctx = { reload: () => load() }

async function loadDicts() {
  const keys = [...new Set(props.config.fields.map((f) => f.dict).filter(Boolean) as string[])]
  await Promise.all(keys.map(async (k) => (dicts[k] = await loadDict(k))))
}
function openForm(row?: any) {
  editing.value = row || null
  Object.keys(form).forEach((k) => delete form[k])
  for (const f of formFields.value) form[f.prop] = row ? row[f.prop] ?? null : f.default ?? (f.type === 'switch' ? 0 : null)
  formVisible.value = true
}
function openView(row: any) { viewRow.value = row; viewVisible.value = true }
async function save() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const body = { ...form }
    if (editing.value) await http.put(`${props.config.api}/${editing.value.id}`, body)
    else await http.post(props.config.api, body)
    ElMessage.success('保存成功')
    formVisible.value = false
    clearDict()
    load()
  } finally {
    saving.value = false
  }
}
async function remove(row: any) {
  await ElMessageBox.confirm(`确定删除该条${props.config.title}记录（ID ${row.id}）？`, '删除确认', { type: 'warning', confirmButtonText: '删除' })
  await http.del(`${props.config.api}/${row.id}`)
  ElMessage.success('已删除')
  load()
}
async function batchRemove() {
  await ElMessageBox.confirm(`确定删除选中的 ${selection.value.length} 条记录？`, '批量删除', { type: 'warning' })
  await http.del(`${props.config.api}/batch`, { params: { ids: selection.value.map((r) => r.id).join(',') } })
  ElMessage.success('已删除')
  load()
}
async function runRow(a: RowAction, row: any) {
  try {
    if (a.confirm) await ElMessageBox.confirm(typeof a.confirm === 'function' ? a.confirm(row) : a.confirm, a.label, { type: 'warning' })
    await a.handler(row, ctx)
  } catch { /* 取消 */ }
}
async function runToolbar(a: ToolbarAction) { try { await a.handler(ctx) } catch { /* 取消 */ } }
function fitHeight() { tableHeight.value = Math.max(360, window.innerHeight - 330) }

watch(() => props.config.key, () => {
  query.current = 1; query.keyword = ''
  Object.keys(filters).forEach((k) => delete filters[k])
  loadDicts(); load()
})
onMounted(() => { fitHeight(); window.addEventListener('resize', fitHeight); loadDicts(); load() })
onBeforeUnmount(() => window.removeEventListener('resize', fitHeight))
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 16px; }
.tb-left, .tb-right { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
.tb-left .el-button + .el-button, .tb-right .el-button + .el-button { margin-left: 0; }
.tbl { border-radius: 12px; overflow: hidden; border: 1px solid var(--line); }
.cell-img { width: 56px; height: 42px; border-radius: 6px; border: 1px solid var(--line); display: block; }
.img-err { width: 100%; height: 100%; display: grid; place-items: center; color: var(--text-4); background: #141a16; }
.tag { border: 0; }
.ops { display: flex; flex-wrap: wrap; gap: 2px 10px; }
.ops .el-button + .el-button { margin-left: 0; }
.pager { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; flex-wrap: wrap; gap: 10px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 0 18px; }
.form-grid .span2 { grid-column: span 2; }
.pv { width: 32px; height: 24px; border-radius: 4px; }
.view { display: grid; grid-template-columns: 1fr 1fr; gap: 14px 18px; }
.v-item { min-width: 0; }
.v-item.wide { grid-column: span 2; }
.v-item label { display: block; font-size: 12px; color: var(--text-3); letter-spacing: .1em; margin-bottom: 4px; }
.v-val { color: var(--text-1); word-break: break-word; white-space: pre-wrap; }
.v-img { width: 100%; max-height: 220px; border-radius: 10px; border: 1px solid var(--line); }
.json { margin: 0; padding: 12px; border-radius: 10px; background: rgba(0,0,0,.35); border: 1px solid var(--line); color: #c9d8c2; font-size: 12px; max-height: 320px; overflow: auto; white-space: pre-wrap; word-break: break-all; }
@media (max-width: 760px) { .form-grid, .view { grid-template-columns: 1fr; } .form-grid .span2, .v-item.wide { grid-column: span 1; } }
</style>
