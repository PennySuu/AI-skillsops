<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, NText } from 'naive-ui'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref('')
const form = reactive({
  username: '',
  password: '',
})

async function submit(): Promise<void> {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    const returnUrl = typeof route.query.returnUrl === 'string' ? route.query.returnUrl : '/market'
    await router.push(returnUrl)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <n-card
    title="登录"
    style="max-width: 420px; margin: 0 auto"
  >
    <n-form>
      <n-form-item label="用户名">
        <n-input
          v-model:value="form.username"
          placeholder="请输入用户名"
        />
      </n-form-item>
      <n-form-item label="密码">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
        />
      </n-form-item>
      <n-text
        v-if="error"
        type="error"
      >
        {{ error }}
      </n-text>
      <n-form-item>
        <n-button
          type="primary"
          :loading="loading"
          @click="submit"
        >
          登录
        </n-button>
      </n-form-item>
      <n-text>
        没有账号？
        <router-link to="/register">
          去注册
        </router-link>
      </n-text>
    </n-form>
  </n-card>
</template>
