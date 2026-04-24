<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NButton, NCard, NDataTable, NEmpty, NTag, useMessage } from 'naive-ui'
import { getMyInstalls, issueInstallCommand } from '@/api/modules/market'
import type { UserInstallDTO } from '@/types/api'

const message = useMessage()
const loading = ref(false)
const rows = ref<UserInstallDTO[]>([])

function buildIdempotencyKey(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

async function loadRows(): Promise<void> {
  loading.value = true
  try {
    const response = await getMyInstalls({ page: 0, size: 20 })
    rows.value = response.items
  } finally {
    loading.value = false
  }
}

async function handleCopyLatest(row: UserInstallDTO): Promise<void> {
  if (row.offline) {
    message.warning('该技能已下架，无法复制最新安装命令')
    return
  }
  try {
    const result = await issueInstallCommand(row.skillId, buildIdempotencyKey())
    await navigator.clipboard.writeText(result.command)
    message.success('复制成功：请勿分享该短时签名命令')
  } catch (error) {
    const text = error instanceof Error ? error.message : '复制失败，请稍后重试'
    message.error(text)
  }
}

const columns = [
  { title: '技能', key: 'skillName' },
  { title: '已装版本', key: 'installedVersion' },
  { title: '最新版本', key: 'latestVersion' },
  {
    title: '状态',
    key: 'state',
    render: (row: UserInstallDTO) => {
      if (row.offline) {
        return '已下架'
      }
      if (row.updateAvailable) {
        return '可更新'
      }
      return '最新'
    },
  },
]

onMounted(() => {
  void loadRows()
})
</script>

<template>
  <n-card title="我的安装">
    <n-data-table
      :loading="loading"
      :columns="columns"
      :data="rows"
      :pagination="{ pageSize: 10 }"
    >
      <template #empty>
        <n-empty description="暂无安装记录" />
      </template>
    </n-data-table>
    <div
      v-for="row in rows"
      :key="row.skillId"
      :class="$style.rowActions"
    >
      <n-tag
        v-if="row.offline"
        type="warning"
      >
        已下架
      </n-tag>
      <n-tag
        v-else-if="row.updateAvailable"
        type="success"
      >
        可更新
      </n-tag>
      <n-button
        size="small"
        tertiary
        :disabled="row.offline"
        @click="handleCopyLatest(row)"
      >
        复制最新命令
      </n-button>
    </div>
  </n-card>
</template>

<style module lang="scss">
.rowActions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
