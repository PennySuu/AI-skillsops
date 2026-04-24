<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, NText } from 'naive-ui'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref('')
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

function validate(): string | null {
  if (!form.username || !form.password) {
    return '用户名和密码不能为空'
  }
  if (form.password.length < 8) {
    return '密码长度至少 8 位'
  }
  if (form.password !== form.confirmPassword) {
    return '两次密码输入不一致'
  }
  return null
}

async function submit(): Promise<void> {
  const msg = validate()
  if (msg) {
    error.value = msg
    return
  }
  loading.value = true
  error.value = ''
  try {
    await authStore.register(form.username, form.password)
    await router.push('/market')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <n-card
    title="注册"
    style="max-width: 420px; margin: 0 auto"
  >
    <n-form>
      <n-form-item label="用户名">
        <n-input
          v-model:value="form.username"
          placeholder="3-32 位字母数字或下划线"
        />
      </n-form-item>
      <n-form-item label="密码">
        <n-input
          v-model:value="form.password"
          type="password"
          show-password-on="click"
        />
      </n-form-item>
      <n-form-item label="确认密码">
        <n-input
          v-model:value="form.confirmPassword"
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
          注册
        </n-button>
      </n-form-item>
      <n-text>
        已有账号？
        <router-link to="/login">
          去登录
        </router-link>
      </n-text>
    </n-form>
  </n-card>
</template>
