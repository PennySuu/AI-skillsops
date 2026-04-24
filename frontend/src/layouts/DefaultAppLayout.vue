<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NConfigProvider,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NMessageProvider,
  NMenu,
  NSpace,
  NText,
  dateZhCN,
  zhCN,
} from 'naive-ui'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const userLabel = computed(() => authStore.profile?.username ?? '未登录')
const menuOptions = computed(() => {
  const common = [
    { label: '市场', key: '/market' },
    { label: '我的发布', key: '/workspace/published' },
    { label: '我的安装', key: '/workspace/installed' },
  ]
  if (authStore.role === 'ADMIN') {
    common.push({ label: '待审核', key: '/workspace/reviews' })
    common.push({ label: '分类管理', key: '/workspace/categories' })
    common.push({ label: '运营看板', key: '/workspace/ops' })
  }
  return common
})
const activeKey = computed(() => router.currentRoute.value.path)

async function handleLogout(): Promise<void> {
  await authStore.logout()
  await router.push('/login')
}

function handleMenuSelect(key: string): void {
  void router.push(key)
}
</script>

<template>
  <n-config-provider
    :locale="zhCN"
    :date-locale="dateZhCN"
  >
    <n-message-provider>
      <n-layout style="min-height: 100vh">
        <n-layout-header
          bordered
          :class="$style.shellHeader"
          style="height: 56px; padding: 0 24px; display: flex; align-items: center"
        >
          <div :class="$style.headerInner">
            <strong style="color: var(--skillsops-color-primary)">SkillsOps</strong>
            <n-space align="center">
              <n-text depth="3">
                {{ userLabel }}
              </n-text>
              <n-button
                v-if="authStore.isAuthenticated"
                tertiary
                type="primary"
                size="small"
                @click="handleLogout"
              >
                退出登录
              </n-button>
            </n-space>
          </div>
        </n-layout-header>
        <n-layout-content
          content-style="padding: 24px; max-width: 1200px; margin: 0 auto; width: 100%"
        >
          <n-menu
            mode="horizontal"
            :value="activeKey"
            :options="menuOptions"
            @update:value="handleMenuSelect"
          />
          <router-view />
        </n-layout-content>
      </n-layout>
    </n-message-provider>
  </n-config-provider>
</template>

<style module lang="scss">
.shellHeader {
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.headerInner {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
