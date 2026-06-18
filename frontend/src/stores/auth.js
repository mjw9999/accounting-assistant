import { defineStore } from 'pinia'
import { login as loginApi } from '../api/modules'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('paa_token') || '',
    user: JSON.parse(localStorage.getItem('paa_user') || 'null')
  }),
  actions: {
    async login(payload) {
      const res = await loginApi(payload)
      this.token = res.data.token
      this.user = {
        id: res.data.id,
        username: res.data.username,
        displayName: res.data.displayName,
        avatarUrl: res.data.avatarUrl,
        role: res.data.role
      }
      localStorage.setItem('paa_token', this.token)
      localStorage.setItem('paa_user', JSON.stringify(this.user))
    },
    updateUser(patch) {
      this.user = { ...this.user, ...patch }
      localStorage.setItem('paa_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('paa_token')
      localStorage.removeItem('paa_user')
    }
  }
})
