import { setActivePinia, createPinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useAuthStore } from '@/stores/authStore'

vi.mock('@/api/modules/auth', () => ({
  login: vi.fn(async ({ username }: { username: string }) => ({
    userId: 1,
    username,
    role: 'USER',
    expiresInSeconds: 1800,
  })),
  register: vi.fn(async ({ username }: { username: string }) => ({
    userId: 2,
    username,
    role: 'USER',
    expiresInSeconds: 1800,
  })),
  logout: vi.fn(async () => undefined),
}))

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should set authenticated profile after login', async () => {
    const authStore = useAuthStore()
    await authStore.login('demo_user', 'Passw0rd!')
    expect(authStore.isAuthenticated).toBe(true)
    expect(authStore.profile?.username).toBe('demo_user')
    expect(authStore.role).toBe('USER')
  })

  it('should clear profile after logout', async () => {
    const authStore = useAuthStore()
    await authStore.register('new_user', 'Passw0rd!')
    expect(authStore.isAuthenticated).toBe(true)
    await authStore.logout()
    expect(authStore.isAuthenticated).toBe(false)
    expect(authStore.profile).toBeNull()
  })
})
