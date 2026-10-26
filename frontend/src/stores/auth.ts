import { defineStore } from 'pinia'
import { authApi } from '@/api'
import type { UserInfo } from '@/api/types'
import { APP_CONFIG } from '@/config'

function loadUser(): UserInfo | null {
  try {
    const raw = localStorage.getItem(APP_CONFIG.userKey)
    return raw ? (JSON.parse(raw) as UserInfo) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(APP_CONFIG.tokenKey) || '',
    user: loadUser() as UserInfo | null
  }),
  getters: {
    isLogin: (s) => !!s.token,
    roles: (s) => s.user?.roles || [],
    isAdmin(): boolean {
      return this.roles.includes('ADMIN')
    },
    isStaff(): boolean {
      return this.roles.includes('ADMIN') || this.roles.includes('CURATOR')
    },
    displayName: (s) => s.user?.nickname || s.user?.username || '游客'
  },
  actions: {
    async login(username: string, password: string) {
      const vo = await authApi.login(username, password)
      this.token = vo.token
      this.user = vo.user
      localStorage.setItem(APP_CONFIG.tokenKey, vo.token)
      localStorage.setItem(APP_CONFIG.userKey, JSON.stringify(vo.user))
      return vo.user
    },
    async fetchMe() {
      if (!this.token) return null
      try {
        const u = await authApi.me()
        this.user = u
        localStorage.setItem(APP_CONFIG.userKey, JSON.stringify(u))
        return u
      } catch {
        return null
      }
    },
    logout() {
      authApi.logout().catch(() => undefined)
      this.token = ''
      this.user = null
      localStorage.removeItem(APP_CONFIG.tokenKey)
      localStorage.removeItem(APP_CONFIG.userKey)
    }
  }
})
