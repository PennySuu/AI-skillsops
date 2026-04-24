import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

describe('router guards', () => {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/login', component: { template: '<div>login</div>' } },
      { path: '/market', component: { template: '<div>market</div>' }, meta: { requiresAuth: true } },
      { path: '/workspace/reviews', component: { template: '<div>reviews</div>' }, meta: { requiresAuth: true, roles: ['ADMIN'] } },
      { path: '/403', component: { template: '<div>403</div>' } },
    ],
  })

  router.beforeEach((to) => {
    const authStore = useAuthStore()
    const routeMeta = (to.meta ?? {}) as { requiresAuth?: boolean; roles?: Array<'USER' | 'ADMIN'> }
    const returnUrl = encodeURIComponent(to.fullPath)

    if (routeMeta.requiresAuth && !authStore.isAuthenticated) {
      return `/login?returnUrl=${returnUrl}`
    }

    if (routeMeta.roles && routeMeta.roles.length > 0) {
      const currentRole = authStore.role
      if (!currentRole || !routeMeta.roles.includes(currentRole)) {
        return '/403'
      }
    }
    return true
  })

  beforeEach(async () => {
    setActivePinia(createPinia())
    await router.replace('/login')
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
