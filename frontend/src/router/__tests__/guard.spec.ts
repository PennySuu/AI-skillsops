import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { router } from '@/router'
import { useAuthStore } from '@/stores/authStore'

describe('router guards', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    await router.push('/login')
  })

  it('should redirect to login with returnUrl when auth required', async () => {
    await router.push('/market')
    expect(router.currentRoute.value.fullPath).toContain('/login?returnUrl=')
  })

  it('should redirect non-admin user to 403 for admin route', async () => {
    const authStore = useAuthStore()
    authStore.profile = {
      userId: 1,
      username: 'user_a',
      role: 'USER',
      expiresInSeconds: 1800,
    }
    await router.push('/workspace/reviews')
    expect(router.currentRoute.value.path).toBe('/403')
  })

  it('should allow admin user to access admin route', async () => {
    const authStore = useAuthStore()
    authStore.profile = {
      userId: 2,
      username: 'admin_a',
      role: 'ADMIN',
      expiresInSeconds: 1800,
    }
    await router.push('/workspace/reviews')
    expect(router.currentRoute.value.path).toBe('/workspace/reviews')
  })
})
