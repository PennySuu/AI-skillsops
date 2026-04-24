import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as apiLogin, logout as apiLogout, register as apiRegister } from '@/api/modules/auth'
import type { AuthProfileDTO, UserRole } from '@/types/api'

export const useAuthStore = defineStore('auth', () => {
  const profile = ref<AuthProfileDTO | null>(null)

  const isAuthenticated = computed(() => profile.value !== null)
  const role = computed<UserRole | null>(() => profile.value?.role ?? null)

  async function login(username: string, password: string): Promise<void> {
    profile.value = await apiLogin({ username, password })
  }

  async function register(username: string, password: string): Promise<void> {
    profile.value = await apiRegister({ username, password })
  }

  async function logout(): Promise<void> {
    try {
      await apiLogout()
    } finally {
      profile.value = null
    }
  }

  return {
    profile,
    isAuthenticated,
    role,
    login,
    register,
    logout,
  }
})
