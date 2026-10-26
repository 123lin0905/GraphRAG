<template>
  <div v-if="cfg" class="cp">
    <div class="cp-head">
      <div class="cp-icon"><el-icon :size="22"><component :is="cfg.icon" /></el-icon></div>
      <div>
        <h2 class="serif">{{ cfg.title }}</h2>
        <p class="muted">{{ cfg.desc || `${cfg.group} · ${cfg.title}数据维护` }}</p>
      </div>
    </div>
    <div class="panel panel-pad"><CrudTable :config="cfg" /></div>
  </div>
  <el-result v-else icon="warning" title="页面不存在" sub-title="未找到对应的管理模块" />
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CrudTable from '@/components/CrudTable.vue'
import { CRUD_MAP } from '@/admin/configs'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cfg = computed(() => {
  const c = CRUD_MAP[route.params.key as string]
  if (c?.roles && !c.roles.some((r) => auth.roles.includes(r))) {
    ElMessage.warning('该模块仅限系统管理员访问')
    router.replace('/admin/dashboard')
    return undefined
  }
  return c
})
</script>
<style scoped>
.cp-head { display: flex; align-items: center; gap: 14px; margin-bottom: 18px; }
.cp-icon { width: 48px; height: 48px; border-radius: 14px; display: grid; place-items: center; color: var(--gold-light); background: radial-gradient(circle at 30% 30%, rgba(201,164,92,.28), rgba(201,164,92,.05)); border: 1px solid var(--line-strong); }
.cp-head h2 { font-size: 22px; letter-spacing: .14em; color: var(--text-1); }
.cp-head p { font-size: 13px; }
</style>
