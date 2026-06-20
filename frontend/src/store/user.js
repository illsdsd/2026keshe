import { defineStore } from 'pinia'

/**
 * 用户登录状态：token + 用户信息，持久化到 localStorage。
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('cl_token') || '',
    user: JSON.parse(localStorage.getItem('cl_user') || 'null')
  }),
  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    setAuth(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem('cl_token', token)
      localStorage.setItem('cl_user', JSON.stringify(user))
    },
    setUser(user) {
      this.user = user
      localStorage.setItem('cl_user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('cl_token')
      localStorage.removeItem('cl_user')
    }
  }
})
