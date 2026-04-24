import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

type AppRouteMeta = {
  requiresAuth?: boolean
  roles?: Array<'USER' | 'ADMIN'>
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/market',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/market',
      name: 'market',
      component: () => import('@/views/MarketView.vue'),
      meta: { requiresAuth: true } satisfies AppRouteMeta,
    },
    {
      path: '/skills/:skillId',
      name: 'skillDetail',
      component: () => import('@/views/SkillDetailView.vue'),
      meta: { requiresAuth: true } satisfies AppRouteMeta,
    },
    {
      path: '/workspace/published',
      name: 'workspacePublished',
      component: () => import('@/views/WorkspacePublishedView.vue'),
      meta: { requiresAuth: true } satisfies AppRouteMeta,
    },
    {
      path: '/workspace/reviews',
      name: 'workspaceReviews',
      component: () => import('@/views/WorkspaceReviewsView.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] } satisfies AppRouteMeta,
    },
    {
      path: '/workspace/installed',
      name: 'workspaceInstalled',
      component: () => import('@/views/WorkspaceInstalledView.vue'),
      meta: { requiresAuth: true } satisfies AppRouteMeta,
    },
    {
      path: '/403',
      name: 'forbidden',
      component: () => import('@/views/ForbiddenView.vue'),
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const routeMeta = (to.meta ?? {}) as AppRouteMeta
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

export { router }
