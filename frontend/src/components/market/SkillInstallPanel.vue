<script setup lang="ts">
import { computed, ref } from 'vue'
import { NAlert, NButton, NCard, NInput, NText, useMessage } from 'naive-ui'
import { issueInstallCommand } from '@/api/modules/market'

const props = defineProps<{
  skillId: number
  offline?: boolean
}>()

const message = useMessage()
const loading = ref(false)
const command = ref('')

const canCopy = computed(() => command.value.length > 0)

function buildIdempotencyKey(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

async function handleGenerate(): Promise<void> {
  if (props.offline) {
    message.warning('该技能已下架，无法生成安装命令')
    return
  }
  loading.value = true
  try {
    const response = await issueInstallCommand(props.skillId, buildIdempotencyKey())
    command.value = response.command
  } catch (error) {
    const text = error instanceof Error ? error.message : '生成安装命令失败'
    message.error(text)
  } finally {
    loading.value = false
  }
}

async function handleCopy(): Promise<void> {
  if (!canCopy.value) {
    return
  }
  try {
    await navigator.clipboard.writeText(command.value)
    message.success('复制成功：命令为短时签名链接，请勿分享给他人')
  } catch {
    message.error('复制失败，请手动复制命令')
  }
}
</script>

<template>
  <n-card title="安装命令">
    <n-alert
      v-if="offline"
      type="warning"
      :show-icon="false"
      style="margin-bottom: 12px"
    >
      该技能已下架，不可生成安装命令。
    </n-alert>
    <n-input
      :value="command"
      readonly
      placeholder="点击“生成命令”后可复制"
      data-test-command-input="true"
    />
    <div :class="$style.actions">
      <n-button
        type="primary"
        :disabled="offline"
        :loading="loading"
        data-test-generate-command="true"
        @click="handleGenerate"
      >
        生成命令
      </n-button>
      <n-button
        :disabled="!canCopy"
        data-test-copy-command="true"
        @click="handleCopy"
      >
        复制命令
      </n-button>
    </div>
    <n-text depth="3">
      平台不会自动执行安装脚本，请在本地终端粘贴执行。
    </n-text>
  </n-card>
</template>

<style module lang="scss">
.actions {
  margin: 12px 0;
  display: flex;
  gap: 8px;
}
</style>
